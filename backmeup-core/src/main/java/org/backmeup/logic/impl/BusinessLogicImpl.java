package org.backmeup.logic.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.Connection;
import org.backmeup.job.JobManager;
import org.backmeup.logic.AuthorizationLogic;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.logic.ProfileLogic;
import org.backmeup.logic.SearchLogic;
import org.backmeup.logic.UserRegistration;
import org.backmeup.logic.impl.helper.BackUpJobCreationHelper;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.FileItem;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.Status;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.constants.DelayTimes;
import org.backmeup.model.dto.ActionProfileEntry;
import org.backmeup.model.dto.ExecutionTime;
import org.backmeup.model.dto.Job;
import org.backmeup.model.dto.JobCreationRequest;
import org.backmeup.model.dto.JobProtocolDTO;
import org.backmeup.model.dto.JobUpdateRequest;
import org.backmeup.model.dto.SourceProfileEntry;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.PluginUnavailableException;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.spi.Authorizable;
import org.backmeup.plugin.spi.Authorizable.AuthorizationType;
import org.backmeup.plugin.spi.InputBased;
import org.backmeup.plugin.spi.OAuthBased;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the BusinessLogic interface by delegating most operations to
 * following layers: - DataAccessLayer - JobManager - PluginLayer
 * 
 * If an error occurs within a method an exception will be thrown that must be
 * handled by the client of the business logic.
 * 
 * @author fschoeppl
 */
@ApplicationScoped // TODO PK is ApplicationScoped correct when all calls share the same connection? are multiple calls within the same application possible? 
public class BusinessLogicImpl implements BusinessLogic {

    private static final String UNKNOWN_SOURCE_SINK = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_SOURCE_SINK";
    private static final String SHUTTING_DOWN_BUSINESS_LOGIC = "org.backmeup.logic.impl.BusinessLogicImpl.SHUTTING_DOWN_BUSINESS_LOGIC";
    private static final String VALIDATION_OF_ACCESS_DATA_FAILED = "org.backmeup.logic.impl.BusinessLogicImpl.VALIDATION_OF_ACCESS_DATA_FAILED";
    private static final String UNKNOWN_ACTION = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_ACTION";
    private static final String ERROR_OCCURED = "org.backmeup.logic.impl.BusinessLogicImpl.ERROR_OCCURED";

    @Inject
    @Configuration(key="backmeup.callbackUrl")
    private String callbackUrl;

    @Inject
    @Configuration(key = "backmeup.index.host")
    private String indexHost; // TODO only for RabbitMQJobReceiver

    @Inject
    @Configuration(key = "backmeup.index.port")
    private Integer indexPort; // TODO only for RabbitMQJobReceiver
    
//    @Inject
//    private Keyserver keyserverClient; // TODO only for RabbitMQJobReceiver

    @Inject
    @Named("plugin")
    private Plugin plugins;

    // See setJobManager()
    private JobManager jobManager;

    @Inject
    private Connection conn;

    @Inject
    private Node esNode;

    // RabbitMQJobReceiver -------------------
    @Inject
    @Configuration(key="backmeup.message.queue.host")
    private String mqHost;

    @Inject
    @Configuration(key="backmeup.message.queue.name")
    private String mqName;

    @Inject
    @Configuration(key="backmeup.message.queue.receivers")
    private Integer numberOfJobWorker;

    @Inject
    @Configuration(key="backmeup.job.backupname")
    private String backupName;

    @Inject
    @Configuration(key="backmeup.job.temporaryDirectory")
    private String jobTempDir;

    @Inject
    private UserRegistration registration;
    
    @Inject
    private AuthorizationLogic authorization;

    @Inject
    private SearchLogic search;

    @Inject
    private ProfileLogic profiles;

    @Inject
    private BackupLogic backupJobs;
    
    // ---------------------------------------

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // There seems to be a problem with weld (can't find resource bundle 
    // with getClass().getSimpleName()). Therefore use class name. 
    private final ResourceBundle textBundle = ResourceBundle.getBundle("BusinessLogicImpl");

    @PostConstruct
    public void startup() {

    }

    @Override
    public BackMeUpUser getUser(final String username) {
        return conn.txJoinReadOnly(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {

                return registration.getActiveUser(username);
            
            }
        });
    }

    @Override
    public BackMeUpUser deleteUser(final String username) {
        BackMeUpUser user = conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                
                BackMeUpUser u = registration.getExistingUser(username);
                authorization.unregister(u);
                backupJobs.deleteJobsOf(username);
                profiles.deleteProfilesOf(username);
                registration.delete(u); 
                return u;
                
            }
        });

        search.deleteIndexOf(user);
        return user;
    }

    @Override
    public BackMeUpUser changeUser(final String oldUsername, final String newUsername, final String oldPassword,
            final String newPassword, final String oldKeyRingPassword, final String newKeyRingPassword, final String newEmail) {
        return conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                
                BackMeUpUser user = registration.getActiveUser(oldUsername);
                registration.ensureNewValuesAvailable(user, newUsername, newEmail);
                authorization.authorize(user, oldPassword);
                authorization.updatePasswords(user, oldPassword, newPassword, oldKeyRingPassword, newKeyRingPassword);
                registration.updateValues(user, newUsername, newEmail);
                return user;
                
            }
        });
    }

    @Override
    public BackMeUpUser login(final String username, final String password) {
        return conn.txNewReadOnly(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                
                BackMeUpUser user = registration.getExistingUser(username);
                authorization.authorize(user, password);
                return user;
                
            }
        });
    }

    @Override
    public BackMeUpUser register(final String username, final String password, final String keyRingPassword, final String email) {
        return conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                
                BackMeUpUser user = registration.register(username, email);
                authorization.register(user, password, keyRingPassword);
                registration.sendVerificationEmailFor(user);
                return user;
                
            }
        });
    }

    @Override
    public void setUserProperty(final String username, final String key, final String value) {
        conn.txJoin(new Runnable() {
            @Override public void run() {

                BackMeUpUser user = registration.getActiveUser(username);
                user.setUserProperty(key, value);
            
            }
        });
    }

    @Override
    public void deleteUserProperty(final String username, final String key) {
        conn.txJoin(new Runnable() {
            @Override public void run() {

                BackMeUpUser user = registration.getActiveUser(username);
                user.deleteUserProperty(key); 
            
            }
        });
    }

    @Override
    public List<SourceSinkDescribable> getDatasources() {
        return plugins.getConnectedDatasources();
    }

    @Override
    public List<Profile> getDatasourceProfiles(final String username) {
        return conn.txNewReadOnly(new Callable<List<Profile>>() {
            @Override public List<Profile> call() {
                
                return profiles.getProfilesOf(username);
                
            }
        });
    }

    @Override
    public Profile deleteProfile(final String username, final Long profileId) {
        return conn.txNew(new Callable<Profile>() {
            @Override public Profile call() {
                
                return profiles.deleteProfile(profileId, username);
                
            }
        });
    }

    @Override
    public List<String> getDatasourceOptions(final String username, final Long profileId, final String keyRingPassword) {
        return conn.txJoinReadOnly(new Callable<List<String>>() {
            @Override public List<String> call() {
                
                Profile p = profiles.getExistingUserProfile(profileId, username);
                String profileDescription = p.getDescription();
                Datasource source = plugins.getDatasource(profileDescription);
                Properties accessData = authorization.fetchProfileAuthenticationData(p, keyRingPassword);
                return source.getAvailableOptions(accessData);
                
            }
        });
    }

    @Override
    public List<String> getStoredDatasourceOptions(final String username, final Long profileId, final Long jobId) {
        return conn.txJoinReadOnly(new Callable<List<String>>() {
            @Override public List<String> call() {

                registration.ensureUserIsActive(username);
                BackupJob job = backupJobs.getExistingUserJob(jobId, username);
                Set<ProfileOptions> sourceProfiles = job.getSourceProfiles();
                return profiles.getProfileOptions(profileId, sourceProfiles);
            
            }
        });
    }

	@Override
    public void changeProfile(final Long profileId, final Long jobId, final List<String> sourceOptions) {
		conn.txJoin(new Runnable() {
			@Override public void run() {

			    BackupJob backupjob = backupJobs.getExistingJob(jobId);
				Set<ProfileOptions> sourceProfiles = backupjob.getSourceProfiles();
                profiles.setProfileOptions(profileId, sourceProfiles, sourceOptions);
				
			}
		});
	}

    @Override
    public void deleteDatasourcePlugin(String name) {
        throw new UnsupportedOperationException("delete Datasource Plugin not implemented");
    }

    @Override
    public List<SourceSinkDescribable> getDatasinks() {
        return plugins.getConnectedDatasinks();
    }

    @Override
    public List<Profile> getDatasinkProfiles(final String username) {
        return conn.txNewReadOnly(new Callable<List<Profile>>() {
            @Override public List<Profile> call() {
                
                return profiles.getDatasinkProfilesOf(username);
                
            }
        });
    }

    @Override
    public void deleteDatasinkPlugin(String name) {
        throw new UnsupportedOperationException("delete Datasink Plugin not implemented");
    }

    @Override
    public List<ActionDescribable> getActions() {
        return plugins.getActions();
    }

    @Override
    public ActionProfile getStoredActionOptions(final String actionId, final Long jobId) {
        return conn.txJoinReadOnly(new Callable<ActionProfile>() {
            @Override public ActionProfile call() {

                return backupJobs.getJobActionOption(actionId, jobId);
            
            }
        });
    }

	@Override
	public List<String> getActionOptions(String actionId) {
		ActionDescribable action = plugins.getActionById(actionId);
		return action.getAvailableOptions();
	}

    @Override
    public void changeActionOptions(final String actionId, final Long jobId, final Map<String, String> actionOptions) {
        conn.txJoin(new Runnable() {
            @Override public void run() {

                backupJobs.updateJobActionOption(actionId, jobId, actionOptions);
            
            }
        });
    }

    @Override
    public void deleteActionPlugin(String name) {
        throw new UnsupportedOperationException("delete Action Plugin not implemented");
    }

    private List<ActionProfile> getActionProfilesFor(JobCreationRequest request) {
        List<ActionProfile> actions = new ArrayList<>();
        // TODO PK extract plugins
        for (ActionProfileEntry action : request.getActions()) {
            ActionDescribable ad = plugins.getActionById(action.getId());

            if (ad == null) {
                throw new IllegalArgumentException(String.format(
                        textBundle.getString(UNKNOWN_ACTION), action.getId()));
            }
            
            ActionProfile ap = new ActionProfile(ad.getId(), ad.getPriority());
            for (Map.Entry<String, String> entry : action.getOptions().entrySet()) {
                ap.addActionOption(entry.getKey(), entry.getValue());
            }
            actions.add(ap);
        }
        Collections.sort(actions);
        return actions;
    }

    @Override
    public ValidationNotes createBackupJob(String username, JobCreationRequest request) {
        try {
            conn.begin();
            BackMeUpUser user = registration.getActiveUser(username);
            authorization.authorize(user, request.getKeyRing());

            List<SourceProfileEntry> sourceProfiles = request.getSourceProfiles();
            Set<ProfileOptions> pos = profiles.getSourceProfilesOptionsFor(sourceProfiles);
            Profile sink = profiles.queryExistingProfile(request.getSinkProfileId());

            List<ActionProfile> actions = getActionProfilesFor(request);

            ExecutionTime execTime = BackUpJobCreationHelper.getExecutionTimeFor(request);

            conn.rollback();
            BackupJob job = jobManager.createBackupJob(user, pos, sink, actions,
                    execTime.getStart(), execTime.getDelay(), request.getKeyRing(), request.getJobTitle(), execTime.isReschedule(), request.getTimeExpression());
            ValidationNotes vn = validateBackupJob(username, job.getId(), request.getKeyRing());
            vn.setJob(job);
            return vn;
        } finally {
            conn.rollback();
        }
    }

    // Note: keyRing won't be overridden
    @Override
    public ValidationNotes updateBackupJob(final String username, final JobUpdateRequest updateRequest) {
        if (updateRequest == null) {
            throw new IllegalArgumentException("Update must not be null!");
        }
        if (updateRequest.getJobId() == null) {
            throw new IllegalArgumentException("JobId must not be null!");
        }
        
        final Boolean[] scheduleJob = { false };
        BackupJob j = conn.txNew(new Callable<BackupJob>() {
            @Override public BackupJob call() {

                BackMeUpUser user = registration.getActiveUser(username);
                authorization.authorize(user, updateRequest.getKeyRing());

                List<ActionProfile> requiredActions = getActionProfilesFor(updateRequest);
                Set<ProfileOptions> sourceProfiles = profiles.getSourceProfilesOptionsFor(updateRequest.getSourceProfiles());
                Profile sindProfile = profiles.queryExistingProfile(updateRequest.getSinkProfileId());

                BackupJob job = backupJobs.getExistingUserJob(updateRequest.getJobId(), username);

                // check if the interval has changed
                if (job.getTimeExpression().compareToIgnoreCase(updateRequest.getTimeExpression()) != 0) {
                    scheduleJob[0] = true;

                    // chage start date to now if interval has changed
                    job.setStart(new Date());
                }

                backupJobs.updatelJob(job, requiredActions, sourceProfiles, sindProfile, updateRequest);
                return job;
            
            }
        });

		if (scheduleJob[0]) {
			// add the updated job to the queue. (All old queue entrys get
			// invalid and will not be executed)
			jobManager.runBackUpJob(j);
		}

        ValidationNotes vn = validateBackupJob(username, j.getId(), updateRequest.getKeyRing());
        vn.setJob(j);
        return vn;
    }

    @Override
    public JobUpdateRequest getBackupJob(String username, final Long jobId) {
        return conn.txNewReadOnly(new Callable<JobUpdateRequest>() {
            @Override public JobUpdateRequest call() {

                return backupJobs.updateRequestFor(jobId);
                
            }
        });
    }
    
    @Override
    public Job getBackupJobFull(String username, final Long jobId) {
        return conn.txNewReadOnly(new Callable<Job>() {
            @Override public Job call() {
                
                return backupJobs.fullJobFor(jobId);
                
            }
        });
    }
    
//    @Override
//    public Job updateBackupJobFull(String username, Job job) {
//    	if (job == null) {
//            throw new IllegalArgumentException("Update must not be null!");
//        }
//
//        if (job.getJobId() == null) {
//            throw new IllegalArgumentException("JobId must not be null!");
//        }
//        
//        boolean scheduleJob = false;
//        
//        try {
//            conn.begin();
//            
//            BackMeUpUser user = registrationService.queryActivatedUser(username);
//            // TODO: Autorize update from backmeup-worker
//            // authorizationService.authorize(user, updateRequest.getKeyRing());
//            
//            BackupJob backupJob = getBackupJobDao().findById(job.getJobId());
//            if (backupJob == null || !backupJob.getUser().getUsername().equals(username)) {
//                throw new IllegalArgumentException(String.format(textBundle.getString(JOB_USER_MISSMATCH),
//                		job.getJobId(), username));
//            }
//            
//            conn.commit();
//            if(scheduleJob == true) {
//                // Add the updated job to the queue. 
//            	// (All old queue entrys get invalid and will not be executed)
//                jobManager.runBackUpJob(backupJob);
//                
//
//            }
//        } finally {
//            conn.rollback();
//        }
//        
//        return getBackupJobFull(username, job.getJobId());
//    }

    @Override
    public List<BackupJob> getJobs(final String username) {
        return conn.txNewReadOnly(new Callable<List<BackupJob>>() {
            @Override public List<BackupJob> call() {
                
                registration.ensureUserIsActive(username);
                return backupJobs.getBackupJobsOf(username);
                
            }
        });
    }

    @Override
    public void deleteJob(final String username, final Long jobId) {
        conn.txNew(new Runnable() {
            @Override public void run() {

                registration.ensureUserIsActive(username);
                backupJobs.deleteJob(username, jobId);
                
            }
        });
    }

    @Override
    public List<Status> getStatus(final String username, final Long jobIdOrNull) {
        return conn.txNewReadOnly(new Callable<List<Status>>() {
            @Override public List<Status> call() {
                
                registration.ensureUserIsActive(username);
                List<Status> status = backupJobs.getStatus(username, jobIdOrNull);

                if (status.size() > 0) {
                    Long newOrExistingId = status.get(0).getJob().getId();
                    addFileItemsToStatuses(status, newOrExistingId);
                }
                
                return status;
                
            }
        });
    }

    private void addFileItemsToStatuses(List<Status> status, Long jobId) {
        Set<FileItem> fileItems = search.getAllFileItems(jobId);
        for (Status stat : status) {
            stat.setFiles(fileItems);
        }
    }
    
    @Override
    public ProtocolDetails getProtocolDetails(String username, String fileId) {
        return search.getProtocolDetails(username, fileId);
    }

    @Override
    public ProtocolOverview getProtocolOverview(final String username, final String duration) {
        return conn.txNewReadOnly(new Callable<ProtocolOverview>() {
            @Override public ProtocolOverview call() {
                
                BackMeUpUser user = registration.getActiveUser(username);
                
                Date to = new Date();
                Date from = duration.equals("month") ? new Date(to.getTime() - DelayTimes.DELAY_MONTHLY) :
                    new Date(to.getTime() - DelayTimes.DELAY_WEEKLY);
                
                return backupJobs.getProtocolOverview(user, from, to);
                
            }
        });
    }
    
    @Override
    public void updateJobProtocol(final String username, final Long jobId, final JobProtocolDTO jobProtocol) {
    	conn.txNew(new Runnable() {
            @Override public void run() {
                
                BackMeUpUser user = registration.getActiveUser(username);
                BackupJob job = backupJobs.getExistingUserJob(jobId, username);
                backupJobs.createJobProtocol(user, job, jobProtocol);
                
            }
        });
    }
    
    @Override
    public void deleteJobProtocols(final String username) {
        conn.txNew(new Runnable() {
            @Override public void run() {
                
                registration.ensureUserIsActive(username);
                backupJobs.deleteProtocolsOf(username);
                
            }
        });
    }

    @Override
    public AuthRequest preAuth(String username, String uniqueDescIdentifier,
            String profileName, String keyRing) throws PluginException,
            InvalidCredentialsException {
        Authorizable auth = plugins.getAuthorizable(uniqueDescIdentifier);
        SourceSinkDescribable desc = plugins
                .getSourceSinkById(uniqueDescIdentifier);
        return preAuth(username, uniqueDescIdentifier, profileName, keyRing, auth, desc);
    }

    private AuthRequest preAuth(final String username, final String uniqueDescIdentifier, final String profileName, final String keyRing,
            final Authorizable auth, final SourceSinkDescribable desc) {
        return conn.txNew(new Callable<AuthRequest>() {
            @Override public AuthRequest call() {

                BackMeUpUser user = registration.getActiveUser(username);
                
                authorization.authorize(user, keyRing);

                AuthRequest ar = new AuthRequest();
                Properties p = new Properties();
                p.setProperty("callback", callbackUrl);
                switch (auth.getAuthType()) {
                case OAuth:
                    OAuthBased oauth = plugins.getOAuthBasedAuthorizable(uniqueDescIdentifier);
                    String redirectUrl = oauth.createRedirectURL(p, callbackUrl);
                    ar.setRedirectURL(redirectUrl);
                    // TODO Store all properties within keyserver & don't store them within the local database!
                    break;
                case InputBased:
                    InputBased ibased = plugins.getInputBasedAuthorizable(uniqueDescIdentifier);
                    ar.setRequiredInputs(ibased.getRequiredInputFields());
                    break;
                default:
                    throw new IllegalArgumentException("unknown enum value " + auth.getAuthType());
                }
                
                Profile profile = profiles.createNewProfile(user, uniqueDescIdentifier, profileName, desc.getType());
                authorization.initProfileAuthInformation(profile, p, keyRing);
                
                ar.setProfile(profile);
                return ar;

            }
        });
    }

    @Override
    public void postAuth(Long profileId, Properties props, String keyRing)
            throws PluginException, ValidationException, InvalidCredentialsException {
        try {
            if (keyRing == null) {
                throw new IllegalArgumentException("keyRing-Parameter cannot be null!");
            } else if (profileId == null) {
                throw new IllegalArgumentException("profileId-Parameter cannot be null!");
            } else if (props == null) {
                throw new IllegalArgumentException("properties-Parameter cannot be null!");
            }

            conn.begin();
            Profile p = profiles.queryExistingProfile(profileId);

            props.putAll(authorization.getProfileAuthInformation(p, keyRing));

            Authorizable auth = plugins.getAuthorizable(p.getDescription());
            if (auth.getAuthType() == AuthorizationType.InputBased) {
                InputBased inputBasedService = plugins.getInputBasedAuthorizable(p
                        .getDescription());
                if (inputBasedService.isValid(props)) {
                    String userId = auth.postAuthorize(props);

                    profiles.setIdentification(p, userId);
                    
                    authorization.overwriteProfileAuthInformation(p, props, keyRing);
                    conn.commit();
                    return;
                } else {
                    conn.rollback();
                    throw new ValidationException(ValidationExceptionType.AuthException,
                            textBundle.getString(VALIDATION_OF_ACCESS_DATA_FAILED));
                }
            } else {
                String userId = auth.postAuthorize(props);
                
                profiles.setIdentification(p, userId);
                
                authorization.overwriteProfileAuthInformation(p, props, keyRing);
                conn.commit();
            }
        } catch (PluginException pe) {
            logger.error("", pe);
            throw pe;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public long searchBackup(final String username, final String keyRingPassword, final String query) {
        try {

            return conn.txNew(new Callable<Long>() {
                @Override public Long call() {
                    
                    BackMeUpUser user = registration.getActiveUser(username);
                    authorization.authorize(user, keyRingPassword);
                    SearchResponse searchResp = search.createSearch(query, new String[0]);
                    return searchResp.getId();
                    
                }
            });

        } catch (RuntimeException t) {
            if (t instanceof BackMeUpException) {
                throw (BackMeUpException) t;
            }
            throw new BackMeUpException(textBundle.getString(ERROR_OCCURED), t);
        }
    }

    @Override
    public void deleteIndexForUser(final String username) {
        conn.txNewReadOnly(new Runnable() {
            @Override public void run() {

                BackMeUpUser user = registration.getActiveUser(username);
                search.deleteIndexOf(user);
                
            }
        });
    }

    @Override
    public void deleteIndexForJobAndTimestamp(final Long jobId, final Long timestamp) {
        conn.txNewReadOnly(new Runnable() {
            @Override public void run() {

                BackupJob job = backupJobs.getExistingJob(jobId);
                search.delete(job.getId(), timestamp);
                
            }
        });
    }

    @Override
    public SearchResponse queryBackup(final String username, final long searchId, final Map<String, List<String>> filters) {
        return conn.txNewReadOnly(new Callable<SearchResponse>() {
            @Override public SearchResponse call() {
                
                BackMeUpUser user = registration.getActiveUser(username);
                return search.runSearch(user, searchId, filters);

            }
        });
    }

    @Override
    public File getThumbnail(final String username, final String fileId) {
        return conn.txNewReadOnly(new Callable<File>() {
            @Override public File call() {
                
                BackMeUpUser user = registration.getActiveUser(username);
                return search.getThumbnailPathForFile(user, fileId);

            }
        });
    }

    @Override
    @PreDestroy
    public void shutdown() {
        logger.debug(textBundle.getString(SHUTTING_DOWN_BUSINESS_LOGIC));
        jobManager.shutdown();
        plugins.shutdown();
        esNode.close();
    }

    @Inject
    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
        this.jobManager.start();
    }

    @Override
    public Properties getMetadata(final String username, final Long profileId, final String keyRing) {
        return conn.txJoinReadOnly(new Callable<Properties>() {
            @Override public Properties call() {
                
                Profile profile = profiles.getExistingUserProfile(profileId, username);
                
                // TODO PK move to plugins
                SourceSinkDescribable ssd = plugins.getSourceSinkById(profile.getDescription());
                if (ssd == null) {
                    throw new IllegalArgumentException(String.format(
                            textBundle.getString(UNKNOWN_SOURCE_SINK), profile.getDescription()));
                }
                
                Properties accessData = authorization.getProfileAuthInformation(profile, keyRing);
                return ssd.getMetadata(accessData);
                
            }
        });
    }

    @Override
    public ValidationNotes validateProfile(final String username, final Long profileId, final String keyRing) {
        return conn.txJoinReadOnly(new Callable<ValidationNotes>() {
            @Override public ValidationNotes call() {

                String pluginName = null;
                try {
                    
                    Profile p = profiles.getExistingUserProfile(profileId, username);
                    pluginName = p.getDescription();
                    Validationable validator = plugins.getValidator(p.getDescription());
                    Properties accessData = authorization.getProfileAuthInformation(p, keyRing);
                    return validator.validate(accessData);
                    
                } catch (PluginUnavailableException pue) {
                    ValidationNotes notes = new ValidationNotes();
                    notes.addValidationEntry(ValidationExceptionType.NoValidatorAvailable, pluginName, pue);
                    return notes;
                } catch (Exception pe) {
                    ValidationNotes notes = new ValidationNotes();
                    notes.addValidationEntry(ValidationExceptionType.Error, pluginName, pe);
                    return notes;
                }
            
            }
        });
    }

    //TODO Add password parameter to get token from keyserver to validate the profile
    @Override
    public ValidationNotes validateBackupJob(final String username, final Long jobId, final String keyRing) {
        return conn.txNewReadOnly(new Callable<ValidationNotes>() {
            @Override public ValidationNotes call() {
                
                registration.ensureUserIsActive(username);

                BackupJob job = backupJobs.getExistingUserJob(jobId, username); 
                Set<ProfileOptions> sourceProfiles = job.getSourceProfiles();
                Long sinkProfileId = job.getSinkProfile().getProfileId();

                ValidationNotes notes = new ValidationNotes();
                try {
                    // plugin-level validation
                    for (ProfileOptions po : sourceProfiles) {
                        SourceSinkDescribable ssd = plugins.getSourceSinkById(po.getProfile().getDescription());
                        if (ssd == null) {
                            notes.addValidationEntry(ValidationExceptionType.PluginUnavailable, po.getProfile().getDescription());
                        }

                        // Validate source plug-in itself
                        notes.getValidationEntries().addAll(
                                validateProfile(username, po.getProfile().getProfileId(), keyRing)
                                .getValidationEntries());
                    }

                    // validate sink profile
                    notes.getValidationEntries().addAll(
                            validateProfile(username, sinkProfileId, keyRing)
                            .getValidationEntries());

                } catch (BackMeUpException bme) {
                    notes.addValidationEntry(ValidationExceptionType.Error,
                            bme);
                }
                return notes;

            }
        });
    }

    //TODO Store profile data within keyserver!
    @Override
    public void addProfileEntries(final Long profileId, final Properties entries, final String keyRing) {
        conn.txNew(new Runnable() {
            @Override public void run() {
                
                Profile profile = profiles.queryExistingProfile(profileId);
                authorization.appendProfileAuthInformation(profile, entries, keyRing);
                profiles.save(profile); // TODO why save, has not been changed?
                
            }
        });
    }

    @Override
    public BackMeUpUser verifyEmailAddress(final String verificationKey) {
        return conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                
                return registration.activateUserFor(verificationKey);
                
            }
        });
    }

    @Override
    public BackMeUpUser requestNewVerificationEmail(final String username) {
        return conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                
                return registration.requestNewVerificationEmail(username);
                
            }
        });
    }

    @Override
    public List<KeyserverLog> getKeysrvLogs(BackMeUpUser user) {
        return authorization.getLogs(user);
    }
}
