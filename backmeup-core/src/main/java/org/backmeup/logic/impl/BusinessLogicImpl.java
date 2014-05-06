package org.backmeup.logic.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.JobProtocolDao;
import org.backmeup.dal.ProfileDao;
import org.backmeup.dal.StatusDao;
import org.backmeup.dal.UserDao;
import org.backmeup.job.JobManager;
//import org.backmeup.job.impl.rabbitmq.RabbitMQJobReceiver;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.logic.AuthorizationLogic;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.logic.SearchLogic;
import org.backmeup.logic.UserRegistration;
import org.backmeup.logic.impl.helper.BackUpJobConverter;
import org.backmeup.logic.impl.helper.BackUpJobCreationHelper;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.ActionProfile.ActionProperty;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.FileItem;
import org.backmeup.model.JobProtocol;
import org.backmeup.model.JobProtocol.JobProtocolMember;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.ProtocolOverview.Activity;
import org.backmeup.model.ProtocolOverview.Entry;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.Status;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.constants.BackupJobStatus;
import org.backmeup.model.constants.DelayTimes;
import org.backmeup.model.dto.ActionProfileEntry;
import org.backmeup.model.dto.ExecutionTime;
import org.backmeup.model.dto.Job;
import org.backmeup.model.dto.JobCreationRequest;
import org.backmeup.model.dto.JobProtocolDTO;
import org.backmeup.model.dto.JobProtocolMemberDTO;
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

    private static final String JOB_USER_MISSMATCH = "org.backmeup.logic.impl.BusinessLogicImpl.JOB_USER_MISSMATCH";
    private static final String NO_SUCH_JOB = "org.backmeup.logic.impl.BusinessLogicImpl.NO_SUCH_JOB";
    private static final String UNKNOWN_JOB_WITH_ID = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_JOB_WITH_ID";
    private static final String UNKNOWN_SOURCE_SINK = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_SOURCE_SINK";
    private static final String USER_HAS_NO_PROFILE = "org.backmeup.logic.impl.BusinessLogicImpl.USER_HAS_NO_PROFILE";
    private static final String SHUTTING_DOWN_BUSINESS_LOGIC = "org.backmeup.logic.impl.BusinessLogicImpl.SHUTTING_DOWN_BUSINESS_LOGIC";
    private static final String VALIDATION_OF_ACCESS_DATA_FAILED = "org.backmeup.logic.impl.BusinessLogicImpl.VALIDATION_OF_ACCESS_DATA_FAILED";
    private static final String UNKNOWN_PROFILE = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_PROFILE";
    private static final String UNKNOWN_ACTION = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_ACTION";
    private static final String ERROR_OCCURED = "org.backmeup.logic.impl.BusinessLogicImpl.ERROR_OCCURED";
    private static final String NO_PROFILE_WITHIN_JOB = "org.backmeup.logic.impl.BusinessLogicImpl.NO_PROFILE_WITHIN_JOB";

    @Inject
    @Configuration(key="backmeup.callbackUrl")
    private String callbackUrl;

    @Inject
    @Configuration(key = "backmeup.index.host")
    private String indexHost; // TODO only for RabbitMQJobReceiver

    @Inject
    @Configuration(key = "backmeup.index.port")
    private Integer indexPort; // TODO only for RabbitMQJobReceiver
    
    @Inject
    private DataAccessLayer dal;

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

//    private List<RabbitMQJobReceiver> jobWorker;

    @Inject
    private UserRegistration registrationService;
    
    @Inject
    private AuthorizationLogic authorizationService;

    @Inject
    private SearchLogic searchService;
    
    // ---------------------------------------

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // There seems to be a problem with weld (can't find resource bundle 
    // with getClass().getSimpleName()). Therefore use class name. 
    private final ResourceBundle textBundle = ResourceBundle.getBundle("BusinessLogicImpl");

    @PostConstruct
    public void startup() {
        logger.info("Starting job workers");
//        try {
//            jobWorker = new ArrayList<>();
//            for (int i = 0; i < numberOfJobWorker; i++) {
//                RabbitMQJobReceiver rec = new RabbitMQJobReceiver(mqHost,
//                        mqName, indexHost, indexPort, backupName, jobTempDir,
//                        plugins, keyserverClient, dal);
//                rec.start();
//                jobWorker.add(rec);
//            }
//        } catch (Exception e) {
//            logger.error("Error while starting job receivers", e);
//        }
    }

    private ProfileDao getProfileDao() {
        return dal.createProfileDao();
    }

    private UserDao getUserDao() {
        return dal.createUserDao();
    }

    private BackupJobDao getBackupJobDao() {
        return dal.createBackupJobDao();
    }

    private StatusDao getStatusDao() {
        return dal.createStatusDao();
    }

    @Override
    public BackMeUpUser getUser(final String username) {
        return conn.txJoinReadOnly(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {

                return registrationService.queryActivatedUser(username);
            
            }
        });
    }

    @Override
    public BackMeUpUser deleteUser(final String username) {
        BackMeUpUser user = conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                BackMeUpUser u = registrationService.queryExistingUser(username);
                authorizationService.unregister(u);
                
                BackupJobDao jobDao = getBackupJobDao();
                StatusDao statusDao = getStatusDao();
                for (BackupJob job : jobDao.findByUsername(username)) {
                    for (Status status : statusDao.findByJobId(job.getId())) {
                        statusDao.delete(status);
                    }
                    jobDao.delete(job);
                }
                
                ProfileDao profileDao = getProfileDao();
                for (Profile p : profileDao.findProfilesByUsername(username)) {
                    profileDao.delete(p);
                }
                
                getUserDao().delete(u);
                return u;
            }
        });

        searchService.deleteIndexOf(user);
        return user;
    }

    @Override
    public BackMeUpUser changeUser(final String oldUsername, final String newUsername, final String oldPassword,
            final String newPassword, final String oldKeyRingPassword, final String newKeyRingPassword, final String newEmail) {
        return conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                BackMeUpUser user = registrationService.queryActivatedUser(oldUsername);
                registrationService.ensureNewValuesAvailable(user, newUsername, newEmail);
                
                authorizationService.authorize(user, oldPassword);

                authorizationService.updatePasswords(user, oldPassword, newPassword, oldKeyRingPassword, newKeyRingPassword);

                registrationService.updateValues(user, newUsername, newEmail);

                return user;
            }
        });
    }

    @Override
    public BackMeUpUser login(final String username, final String password) {
        return conn.txNewReadOnly(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                BackMeUpUser user = registrationService.queryExistingUser(username);
                authorizationService.authorize(user, password);
                return user;
            }
        });
    }

    @Override
    public BackMeUpUser register(final String username, final String password, final String keyRingPassword, final String email) {
        return conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                BackMeUpUser user = registrationService.register(username, email);
                authorizationService.register(user, password, keyRingPassword);
                registrationService.sendVerificationEmailFor(user);
                return user;
            }
        });
    }

    @Override
    public void setUserProperty(final String username, final String key, final String value) {
        conn.txJoin(new Runnable() {
            @Override public void run() {

                BackMeUpUser user = registrationService.queryActivatedUser(username);
                user.setUserProperty(key, value);
            
            }
        });
    }

    @Override
    public void deleteUserProperty(final String username, final String key) {
        conn.txJoin(new Runnable() {
            @Override public void run() {

                BackMeUpUser user = registrationService.queryActivatedUser(username);
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
                
                return getProfileDao().findDatasourceProfilesByUsername(username);
                
            }
        });
    }

    @Override
    public Profile deleteProfile(final String username, final Long profileId) {
        return conn.txNew(new Callable<Profile>() {
            @Override public Profile call() {
                
                return deleteProfileX(username, profileId);
                
            }
        });
    }

    private Profile deleteProfileX(String username, Long profileId) {
        // TODO PK move
        Profile profile = queryExistingUserProfile(profileId, username);
        getProfileDao().delete(profile);
        return profile;
    }

    private Profile queryExistingUserProfile(Long profileId, String username) {
        Profile profile = queryExistingProfile(profileId);
        if (!profile.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException();
        }
        return profile;
    }

    @Override
    public List<String> getDatasourceOptions(final String username, final Long profileId,
            final String keyRingPassword) {
        return conn.txJoinReadOnly(new Callable<List<String>>() {
            @Override public List<String> call() {
                
                Profile p = queryExistingUserProfile(profileId, username);
                Datasource source = plugins.getDatasource(p.getDescription());
                
                Properties accessData = authorizationService.fetchProfileAuthenticationData(p, keyRingPassword);
                
                return source.getAvailableOptions(accessData);
                
            }
        });
    }

    @Override
    public List<String> getStoredDatasourceOptions(final String username, final Long profileId, final Long jobId) {
        return conn.txJoinReadOnly(new Callable<List<String>>() {
            @Override public List<String> call() {

                registrationService.queryActivatedUser(username);
                BackupJob job = queryExistingUserJob(jobId, username);
                for (ProfileOptions po : job.getSourceProfiles()) {
                    if (po.getProfile().getProfileId().equals(profileId)) {
                        String[] options = po.getOptions();
                        if (options == null) {
                            return new ArrayList<>();
                        }
                        return Arrays.asList(options);
                    }
                }
                
                throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_PROFILE), profileId));
            
            }
        });
    }

    @Override
    public void changeProfile(final Long profileId, final Long jobId, final List<String> sourceOptions)
    {
        conn.txJoin(new Runnable() {
            @Override public void run() {

                ProfileDao pd = getProfileDao ();
                Profile p = pd.findById (profileId);
                if (p == null)
                {
                    throw new IllegalArgumentException (String.format (textBundle.getString(UNKNOWN_PROFILE), profileId));
                }
                
                BackupJobDao bd = getBackupJobDao ();
                BackupJob backupjob = bd.findById (jobId);
                
                Set<ProfileOptions> profileoptions = backupjob.getSourceProfiles ();
                for (ProfileOptions option : profileoptions)
                {
                    if (option.getProfile ().getProfileId ().equals(p.getProfileId ()))
                    {
                        String[] new_options = sourceOptions.toArray (new String[sourceOptions.size ()]);
                        option.setOptions (new_options);
                    }
                }
            
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
                
                return getProfileDao().findDatasinkProfilesByUsername(username);
                
            }
        });
    }

    @Override
    public void deleteDatasinkPlugin(String name) {
    }

    @Override
    public List<ActionDescribable> getActions() {
        List<ActionDescribable> actions = plugins.getActions();
        return actions;
    }

    @Override
    public ActionProfile getStoredActionOptions(final String actionId, final Long jobId) {
        return conn.txJoinReadOnly(new Callable<ActionProfile>() {
            @Override public ActionProfile call() {

                BackupJobDao jobDao = getBackupJobDao();
                BackupJob job = jobDao.findById(jobId);
                if (job == null) {
                    throw new IllegalArgumentException(String.format(textBundle.getString(NO_SUCH_JOB), jobId));
                }
                for (ActionProfile ap : job.getRequiredActions()) {
                    if (ap.getActionId().equals(actionId)) {
                        return ap;
                    }
                }
                throw new IllegalArgumentException(String.format(textBundle.getString(NO_PROFILE_WITHIN_JOB), jobId, actionId));
            
            }
        });
    }

	@Override
	public List<String> getActionOptions(String actionId) {
		ActionDescribable action = plugins.getActionById(actionId);
		return action.getAvailableOptions();
	}

    private void addActionProperties(ActionProfile ap, Map<String, String> keyValues) {
        for (Map.Entry<String, String> e : keyValues.entrySet()) {
            ActionProperty aprop = new ActionProperty(e.getKey(), e.getValue());
            aprop.setProfile(ap);
            ap.getActionOptions().add(aprop);
        }
    }

    @Override
    public void changeActionOptions(final String actionId, final Long jobId,
            final Map<String, String> actionOptions) {
        conn.txJoin(new Runnable() {
            @Override public void run() {

                BackupJobDao jobDao = getBackupJobDao();
                BackupJob job = jobDao.findById(jobId);
                if (job == null) {
                    throw new IllegalArgumentException(String.format(textBundle.getString(NO_SUCH_JOB), jobId));
                }
                for (ActionProfile ap : job.getRequiredActions()) {
                    if (ap.getActionId().equals(actionId)) {
                        ap.getActionOptions().clear();
                        addActionProperties(ap, actionOptions);
                    }
                }
            
            }
        });

    }

    @Override
    public void deleteActionPlugin(String name) {
        throw new UnsupportedOperationException("delete Action Plugin not implemented");
    }

    private List<ActionProfile> getActionProfilesFor(JobCreationRequest request) {
        List<ActionProfile> actions = new ArrayList<>();

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

    private Set<ProfileOptions> getSourceProfilesFor(JobCreationRequest request) {
        Set<ProfileOptions> profiles = new HashSet<>();
        if (request.getSourceProfiles().size() == 0) {
            throw new IllegalArgumentException(
                    "There must be at least one source profile to download data from!");
        }

        for (SourceProfileEntry source : request.getSourceProfiles()) {
            Profile p = getProfileDao().findById(source.getId());
            if (p == null) {
                throw new IllegalArgumentException(String.format(
                        textBundle.getString(UNKNOWN_PROFILE), source.getId()));
            }

            profiles.add(new ProfileOptions(p, source.getOptions().keySet().toArray(new String[]{})));
        }
        return profiles;
    }

    private Profile getSinkProfileFor(JobCreationRequest request) {
        Profile sink = getProfileDao().findById(request.getSinkProfileId());
        if (sink == null) {
            throw new IllegalArgumentException(String.format(
                    textBundle.getString(UNKNOWN_PROFILE), request.getSinkProfileId()));
        }
        return sink;
    }

    @Override
    public ValidationNotes createBackupJob(String username, JobCreationRequest request) {
        try {
            conn.begin();
            BackMeUpUser user = registrationService.queryActivatedUser(username);

            authorizationService.authorize(user, request.getKeyRing());

            Set<ProfileOptions> profiles = getSourceProfilesFor(request);

            Profile sink = getSinkProfileFor(request);

            List<ActionProfile> actions = getActionProfilesFor(request);

            ExecutionTime execTime = BackUpJobCreationHelper.getExecutionTimeFor(request);

            conn.rollback();
            BackupJob job = jobManager.createBackupJob(user, profiles, sink, actions,
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
    public ValidationNotes updateBackupJob(String username,
            JobUpdateRequest updateRequest) {
        boolean scheduleJob = false;

        if (updateRequest == null) {
            throw new IllegalArgumentException("Update must not be null!");
        }

        if (updateRequest.getJobId() == null) {
            throw new IllegalArgumentException("JobId must not be null!");
        }


        try {
            conn.begin();
            
            BackMeUpUser user = registrationService.queryActivatedUser(username);
            authorizationService.authorize(user, updateRequest.getKeyRing());
        
            BackupJob job = getBackupJobDao().findById(updateRequest.getJobId());
            if (job == null || !job.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException(String.format(textBundle.getString(JOB_USER_MISSMATCH),
                        updateRequest.getJobId(), username));
            }

            job.getRequiredActions().clear();
            job.getRequiredActions().addAll(getActionProfilesFor(updateRequest));

            job.getSourceProfiles().clear();
            job.getSourceProfiles().addAll(getSourceProfilesFor(updateRequest));

            job.setJobTitle(updateRequest.getJobTitle());
            job.setSinkProfile(getSinkProfileFor(updateRequest));

            ExecutionTime et = BackUpJobCreationHelper.getExecutionTimeFor(updateRequest);

            // check if the interval has changed
            if (job.getTimeExpression ().compareToIgnoreCase (updateRequest.getTimeExpression()) != 0)
            {
                scheduleJob = true;

                // chage start date to now if interval has changed
                job.setStart (new Date ());
            }

            job.setTimeExpression(updateRequest.getTimeExpression());
            job.setDelay(et.getDelay());
            job.setReschedule (et.isReschedule ());

            if (job.isReschedule () == true)
            {
                Date execTime = new Date (new Date().getTime() + job.getDelay());
                job.setNextExecutionTime (execTime);
            }
            else
            {
                job.setNextExecutionTime (null);
            }

            conn.commit();

            if(scheduleJob == true)
            {
                // add the updated job to the queue. (All old queue entrys get invalid and will not be executed)
                jobManager.runBackUpJob (job);
            }

            ValidationNotes vn = validateBackupJob(username, job.getId(), updateRequest.getKeyRing());
            vn.setJob(job);
            return vn;
        } finally {
            conn.rollback();
        }
    }

    @Override
    public JobUpdateRequest getBackupJob(String username, final Long jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("JobId must not be null");
        }

        return conn.txNewReadOnly(new Callable<JobUpdateRequest>() {
            @Override public JobUpdateRequest call() {
                
                BackupJobDao jobDao = dal.createBackupJobDao();
                BackupJob job = jobDao.findById(jobId);
                return BackUpJobConverter.convertToUpdateRequest(job);
                
            }
        });
    }
    
    @Override
    public Job getBackupJobFull(String username, final Long jobId) {
        if (jobId == null) {
        	throw new IllegalArgumentException("JobId must not be null");
        }

        return conn.txNewReadOnly(new Callable<Job>() {
            @Override public Job call() {
                BackupJob job = getBackupJobDao().findById(jobId);
                return BackUpJobConverter.convertToJob(job);
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
                
                registrationService.queryActivatedUser(username);
                return getBackupJobDao().findByUsername(username);
                
            }
        });
    }

    @Override
    public void deleteJob(final String username, final Long jobId) {
        conn.txNew(new Runnable() {
            @Override public void run() {
                registrationService.queryActivatedUser(username);
                BackupJob job = queryExistingUserJob(jobId, username);

                // Delete Job status records first
                StatusDao statusDao = getStatusDao();
                for (Status status : statusDao.findByJobId(job.getId())) {
                    statusDao.delete(status);
                }

                getBackupJobDao().delete(job);
            }
        });
    }
    private BackupJob queryExistingJob(Long jobId) {
        BackupJob job = getBackupJobDao().findById(jobId);
        if (job == null) {
            throw new IllegalArgumentException(String.format(textBundle.getString(NO_SUCH_JOB), jobId));
        }
        return job;
    }

    private BackupJob queryExistingUserJob(Long jobId, String username) {
        BackupJob job = queryExistingJob(jobId);
        if (!job.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException(String.format(textBundle.getString(JOB_USER_MISSMATCH),
                    jobId, username));
        }
        return job;
    }

    private List<Status> getStatusForJob(final BackupJob job) {
        StatusDao sd = dal.createStatusDao();
        List<Status> status = sd.findLastByJob(job.getUser().getUsername(), job.getId());
        
        Set<FileItem> fileItems = searchService.getAllFileItems(job);
        for (Status stat : status) {
            stat.setFiles(fileItems);
        }
        return status;
    }

    @Override
    public List<Status> getStatus(final String username, final Long jobId) {
        return conn.txNewReadOnly(new Callable<List<Status>>() {
            @Override public List<Status> call() {
                
                registrationService.queryActivatedUser(username);
                BackupJobDao jobDao = getBackupJobDao();
                
                if (jobId == null) {
                    List<Status> status = new ArrayList<>();
                    BackupJob job = jobDao.findLastBackupJob(username);
                    if (job != null) {
                        status.addAll(getStatusForJob(job));
                    }
                    // for (BackupJob job : jobs) {
                    //     status.add(getStatusForJob(job));
                    // }
                    return status;
                }
                
                BackupJob job = queryExistingUserJob(jobId, username);
                List<Status> status = new ArrayList<>();
                status.addAll(getStatusForJob(job));
                return status;
                
            }
        });
    }

    @Override
    public ProtocolDetails getProtocolDetails(String username, String fileId) {
        return searchService.getProtocolDetails(username, fileId);
    }

    @Override
    public ProtocolOverview getProtocolOverview(final String username, final String duration) {
        return conn.txNewReadOnly(new Callable<ProtocolOverview>() {
            @Override public ProtocolOverview call() {
                
                BackMeUpUser user = registrationService.queryActivatedUser(username);
                
                Date to = new Date();
                Date from = duration.equals("month") ? new Date(to.getTime() - DelayTimes.DELAY_MONTHLY) :
                    new Date(to.getTime() - DelayTimes.DELAY_WEEKLY);
                
                return getProtocolOverview(user, from, to);
                
            }
        });
    }

    private ProtocolOverview getProtocolOverview(BackMeUpUser user, Date from, Date to) {
        // TODO PK move
        JobProtocolDao jpd = dal.createJobProtocolDao();
        List<JobProtocol> protocols = jpd.findByUsernameAndDuration(user.getUsername(), from, to);
        ProtocolOverview po = new ProtocolOverview();
        Map<String, Entry> entries = new HashMap<>();
        double totalSize = 0;
        long totalCount = 0;
        for (JobProtocol prot : protocols) {
            totalCount += prot.getTotalStoredEntries();
            for (JobProtocolMember member : prot.getMembers()) {
                Entry entry = entries.get(member.getTitle());
                if (entry == null) {
                    entry = new Entry(member.getTitle(), 0, member.getSpace());
                    entries.put(member.getTitle(), entry);
                } else {
                    entry.setAbsolute(entry.getAbsolute() + member.getSpace());
                }
                totalSize += member.getSpace();
            }
            po.getActivities().add(new Activity(prot.getJob().getJobTitle(), prot.getExecutionTime()));
        }

        for (Entry entry : entries.values()) {
            entry.setPercent(100 * entry.getAbsolute() / totalSize);
            po.getStoredAmount().add(entry);
        }
        po.setTotalCount(totalCount+"");
        // TODO Determine format of bytes (currently MB)
        po.setTotalStored(totalSize / 1024 / 1024 +" MB");
        po.setUser(user.getUserId());
        return po;
    }
    
    @Override
    public void updateJobProtocol(final String username, final Long jobId, final JobProtocolDTO jobProtocol) {
    	conn.txNew(new Runnable() {
            @Override public void run() {
                BackMeUpUser user = registrationService.queryActivatedUser(username);
                BackupJob job = queryExistingUserJob(jobId, username);
                
                JobProtocolDao jpd = dal.createJobProtocolDao();
                
                JobProtocol protocol = new JobProtocol();
                protocol.setUser(user);
                protocol.setJob(job);
                protocol.setSuccessful(jobProtocol.isSuccessful());
                
                for(JobProtocolMemberDTO pm : jobProtocol.getMembers()) {
                	protocol.addMember(new JobProtocolMember(protocol, pm.getTitle(), pm.getSpace()));
                }
                
                if (protocol.isSuccessful()) {
        			job.setLastSuccessful(protocol.getExecutionTime());
        			job.setStatus(BackupJobStatus.successful);
        		} else {
        			job.setLastFailed(protocol.getExecutionTime());
        			job.setStatus(BackupJobStatus.error);
        		}
                
                jpd.save(protocol);
            }
        });
    }
    
    @Override
    public void deleteJobProtocols(final String username) {
        conn.txNew(new Runnable() {
            @Override public void run() {
                registrationService.queryActivatedUser(username);
                JobProtocolDao jpd = dal.createJobProtocolDao();
                jpd.deleteByUsername(username);
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

                BackMeUpUser user = registrationService.queryActivatedUser(username);
                
                authorizationService.authorize(user, keyRing);
                
                org.backmeup.model.spi.SourceSinkDescribable.Type type = desc.getType();
                Profile profile = new Profile(getUserDao().findByName(username),
                        profileName, uniqueDescIdentifier, type);
                AuthRequest ar = new AuthRequest();
                switch (auth.getAuthType()) {
                case OAuth:
                    OAuthBased oauth = plugins.getOAuthBasedAuthorizable(uniqueDescIdentifier);
                    Properties p = new Properties();
                    p.setProperty("callback", callbackUrl);
                    String redirectUrl = oauth.createRedirectURL(p, callbackUrl);
                    ar.setRedirectURL(redirectUrl);
                    // TODO Store all properties within keyserver & don't store them within the local database!
                    
                    profile = getProfileDao().save(profile);
                    authorizationService.initProfileAuthInformation(profile, p, keyRing);
                    break;
                case InputBased:
                    InputBased ibased = plugins.getInputBasedAuthorizable(uniqueDescIdentifier);
                    ar.setRequiredInputs(ibased.getRequiredInputFields());
                    p = new Properties();
                    p.setProperty("callback", callbackUrl);
                    profile = getProfileDao().save(profile);
                    authorizationService.initProfileAuthInformation(profile, p, keyRing);
                    break;
                default:
                    throw new IllegalArgumentException("unknown enum value " + auth.getAuthType());
                }
                
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
            ProfileDao profileDao = getProfileDao();
            Profile p = profileDao.findById(profileId);

            props.putAll(authorizationService.getProfileAuthInformation(p, keyRing));

            Authorizable auth = plugins.getAuthorizable(p.getDescription());
            if (auth.getAuthType() == AuthorizationType.InputBased) {
                InputBased inputBasedService = plugins.getInputBasedAuthorizable(p
                        .getDescription());
                if (inputBasedService.isValid(props)) {
                    String userId = auth.postAuthorize(props);
                    if (userId != null) {
                        p.setIdentification(userId);
                    }
                    profileDao.save(p);
                    authorizationService.overwriteProfileAuthInformation(p, props, keyRing);
                    conn.commit();
                    return;
                } else {
                    conn.rollback();
                    throw new ValidationException(ValidationExceptionType.AuthException,
                            textBundle.getString(VALIDATION_OF_ACCESS_DATA_FAILED));
                }
            } else {
                String userId = auth.postAuthorize(props);
                if (userId != null) {
                    p.setIdentification(userId);
                }
                profileDao.save(p);
                authorizationService.overwriteProfileAuthInformation(p, props, keyRing);
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
                    BackMeUpUser user = registrationService.queryActivatedUser(username);
                    authorizationService.authorize(user, keyRingPassword);
                    SearchResponse search = searchService.createSearch(query, new String[0]);
                    return search.getId();
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

                BackMeUpUser user = registrationService.queryActivatedUser(username);
                searchService.deleteIndexOf(user);
                
            }
        });
    }

    @Override
    public void deleteIndexForJobAndTimestamp(final Long jobId, final Long timestamp) {
        conn.txNewReadOnly(new Runnable() {
            @Override public void run() {

                BackupJob job = queryExistingJob(jobId);
                searchService.delete(job, timestamp);
                
            }
        });
    }

    @Override
    public SearchResponse queryBackup(final String username, final long searchId, final Map<String, List<String>> filters) {
        return conn.txNewReadOnly(new Callable<SearchResponse>() {
            @Override public SearchResponse call() {
                
                BackMeUpUser user = registrationService.queryActivatedUser(username);
                return searchService.runSearch(user, searchId, filters);

            }
        });
    }

    @Override
    public File getThumbnail(final String username, final String fileId) {
        return conn.txNewReadOnly(new Callable<File>() {
            @Override public File call() {
                
                BackMeUpUser user = registrationService.queryActivatedUser(username);
                return searchService.getThumbnailPathForFile(user, fileId);

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

        logger.info("Shutting down job workers!");
//        for (RabbitMQJobReceiver receiver : jobWorker) {
//            receiver.stop();
//        }
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
                
                Profile p = queryExistingProfile(profileId);
                
                if (!p.getUser().getUsername().equals(username)) {
                    throw new IllegalArgumentException(String.format(
                            textBundle.getString(USER_HAS_NO_PROFILE), username, profileId));
                }
                
                SourceSinkDescribable ssd = plugins.getSourceSinkById(p.getDescription());
                if (ssd == null) {
                    throw new IllegalArgumentException(String.format(
                            textBundle.getString(UNKNOWN_SOURCE_SINK), p.getDescription()));
                }
                
                Properties accessData = authorizationService.getProfileAuthInformation(p, keyRing);
                Properties metadata = ssd.getMetadata(accessData);
                return metadata;
                
            }
        });
    }

    @Override
    public ValidationNotes validateProfile(final String username, final Long profileId, final String keyRing) {
        return conn.txJoinReadOnly(new Callable<ValidationNotes>() {
            @Override public ValidationNotes call() {

                String pluginName = null;
                try {
                    Profile p = getProfileDao().findById(profileId);
                    if (p == null || !p.getUser().getUsername().equals(username)) {
                        throw new IllegalArgumentException(String.format(
                                textBundle.getString(USER_HAS_NO_PROFILE), username, profileId));
                    }
                    pluginName = p.getDescription();
                    Validationable validator = plugins.getValidator(p.getDescription());
                    Properties accessData = authorizationService.getProfileAuthInformation(p, keyRing);
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
                
                getUser(username);

                BackupJob job = jobManager.getBackUpJob(jobId);
                if (job == null || !job.getUser().getUsername().equals(username)) {
                    throw new IllegalArgumentException(String.format(
                            textBundle.getString(UNKNOWN_JOB_WITH_ID), jobId));
                }

                ValidationNotes notes = new ValidationNotes();
                try {
                    // plugin-level validation
                    for (ProfileOptions po : job.getSourceProfiles()) {
                        SourceSinkDescribable ssd = plugins.getSourceSinkById(po.getProfile()
                                .getDescription());
                        if (ssd == null) {
                            notes.addValidationEntry(ValidationExceptionType.PluginUnavailable, po
                                    .getProfile().getDescription());
                        }

                        // Validate source plug-in itself
                        notes.getValidationEntries().addAll(
                                validateProfile(username, po.getProfile().getProfileId(), keyRing)
                                .getValidationEntries());
                    }

                    // validate sink profile
                    notes.getValidationEntries().addAll(
                            validateProfile(username, job.getSinkProfile().getProfileId(), keyRing)
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
                
                Profile profile = queryExistingProfile(profileId);
                authorizationService.appendProfileAuthInformation(profile, entries, keyRing);
                
                getProfileDao().save(profile); // TODO why save, has not been changed?
                
            }
        });
    }

    private Profile queryExistingProfile(Long profileId) {
        // TODO PK move
        Profile profile = getProfileDao().findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_PROFILE), profileId));
        }
        return profile;
    }

    @Override
    public BackMeUpUser verifyEmailAddress(final String verificationKey) {
        return conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                
                return registrationService.activateUserFor(verificationKey);
                
            }
        });
    }

    @Override
    public BackMeUpUser requestNewVerificationEmail(final String username) {
        return conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                
                return registrationService.requestNewVerificationEmail(username);
                
            }
        });
    }

    @Override
    public List<KeyserverLog> getKeysrvLogs(BackMeUpUser user) {
        return authorizationService.getLogs(user);
    }
}
