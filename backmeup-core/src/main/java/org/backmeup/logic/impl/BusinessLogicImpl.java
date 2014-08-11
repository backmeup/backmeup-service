package org.backmeup.logic.impl;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.Connection;
import org.backmeup.job.JobManager;
import org.backmeup.logic.AuthorizationLogic;
import org.backmeup.logic.BackupLogic;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.logic.PluginsLogic;
import org.backmeup.logic.ProfileLogic;
import org.backmeup.logic.SearchLogic;
import org.backmeup.logic.UserRegistration;
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
import org.backmeup.model.dto.JobProtocolDTO;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.PluginUnavailableException;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.api.connectors.Datasource;
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

    private static final String SHUTTING_DOWN_BUSINESS_LOGIC = "org.backmeup.logic.impl.BusinessLogicImpl.SHUTTING_DOWN_BUSINESS_LOGIC";
    private static final String ERROR_OCCURED = "org.backmeup.logic.impl.BusinessLogicImpl.ERROR_OCCURED";
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    // There seems to be a problem with weld (can't find resource bundle 
    // with getClass().getSimpleName()). Therefore use class name. 
    private final ResourceBundle textBundle = ResourceBundle.getBundle("BusinessLogicImpl");

    private JobManager jobManager;

    @Inject
    private Connection conn;

    @Inject
    private Node esNode;

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

    @Inject
    private PluginsLogic plugins; 
    
    @Inject
    @Configuration(key = "backmeup.autoVerifyUser")
    private Boolean autoVerifyUser;
    
    // Getter and Setter ------------------------------------------------------
    
    @Inject
    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
        this.jobManager.start();
    }
    
    // ========================================================================
    
    // CDI lifecycle methods --------------------------------------------------
    
    @Override
    @PreDestroy
    public void shutdown() {
        logger.debug(textBundle.getString(SHUTTING_DOWN_BUSINESS_LOGIC));
        jobManager.shutdown();
        esNode.close();
    }
    
    // ========================================================================
    
    // Authentication ---------------------------------------------------------
    
     @Override
     public BackMeUpUser authorize(final String username, final String password) {
    	 return conn.txJoinReadOnly(new Callable<BackMeUpUser>() {
             @Override public BackMeUpUser call() {

            	 BackMeUpUser user = registration.getUserByUsername(username, true);
                 authorization.authorize(user, password);
                 return user;
             }
         }); 
     };
    
    // ========================================================================
    
    // User operations --------------------------------------------------------
    
    @Override
    public BackMeUpUser getUserByUsername(final String username) {
        return conn.txJoinReadOnly(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {

                return registration.getUserByUsername(username, true);
            
            }
        });
    }
    
    @Override
    public BackMeUpUser getUserByUserId(final Long userId) {
        return conn.txJoinReadOnly(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {

                return registration.getUserByUserId(userId, true);
            
            }
        });
    }

    @Override
    public BackMeUpUser deleteUser(final Long userId) {
        BackMeUpUser user = conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                
                BackMeUpUser u = registration.getUserByUserId(userId);
                authorization.unregister(u);
                backupJobs.deleteJobsOf(u.getUserId());
                profiles.deleteProfilesOf(u.getUserId());
                registration.delete(u); 
                search.deleteIndexOf(u);
                return u;
                
            }
        });

        return user;
    }

    @Override
    public BackMeUpUser updateUser(final BackMeUpUser user) {
        return conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
            	
            	registration.getUserByUsername(user.getUsername(), true);
//                BackMeUpUser user = getAuthorizedUser(oldUsername, oldPassword);
//                registration.ensureNewValuesAvailable(user, newUsername, newEmail);
//                authorization.updatePasswords(user, oldPassword, newPassword, oldKeyRingPassword, newKeyRingPassword);
                registration.update(user);
                return user;
                
            }
        });
    }
    
    @Override
    public BackMeUpUser addUser(final BackMeUpUser newUser) {
        return conn.txNew(new Callable<BackMeUpUser>() {
            @Override public BackMeUpUser call() {
                BackMeUpUser user = registration.register(newUser);
                authorization.register(user);
                if(autoVerifyUser) {
                	registration.activateUserFor(user.getVerificationKey());
                } else {
                	registration.sendVerificationEmailFor(user);
                }
                return user;
                
                
            }
        });
    }

//    @Override
//    public void setUserProperty(final String username, final String key, final String value) {
//        conn.txJoin(new Runnable() {
//            @Override public void run() {
//
//                BackMeUpUser user = registration.getActiveUser(username);
//                user.setUserProperty(key, value);
//            
//            }
//        });
//    }
//
//    @Override
//    public void deleteUserProperty(final String username, final String key) {
//        conn.txJoin(new Runnable() {
//            @Override public void run() {
//
//                BackMeUpUser user = registration.getActiveUser(username);
//                user.deleteUserProperty(key); 
//            
//            }
//        });
//    }
    
//  @Override
//  public BackMeUpUser login(final String username, final String password) {
//      return conn.txNewReadOnly(new Callable<BackMeUpUser>() {
//          @Override public BackMeUpUser call() {
//              
//              BackMeUpUser user = registration.getExistingUser(username);
//              authorization.authorize(user, password);
//              return user;
//              
//          }
//      });
//  }
    
//  @Override
//  public BackMeUpUser verifyEmailAddress(final String verificationKey) {
//      return conn.txNew(new Callable<BackMeUpUser>() {
//          @Override public BackMeUpUser call() {
//              
//              return registration.activateUserFor(verificationKey);
//              
//          }
//      });
//  }
//
//  @Override
//  public BackMeUpUser requestNewVerificationEmail(final String username) {
//      return conn.txNew(new Callable<BackMeUpUser>() {
//          @Override public BackMeUpUser call() {
//              
//              return registration.requestNewVerificationEmail(username);
//              
//          }
//      });
//  }
    
	private BackMeUpUser getAuthorizedUser(Long userId, String keyRing) {
		BackMeUpUser user = registration.getUserByUserId(userId, true);
		authorization.authorize(user, keyRing);
		return user;
	}

	private void ensureUserIsAuthorized(Long userId, String keyRing) {
		getAuthorizedUser(userId, keyRing);
	}
    
    // ========================================================================
	
	// Plugin operations ------------------------------------------------------


    @Override
    public List<SourceSinkDescribable> getDatasources() {
        return plugins.getConnectedDatasources();
    }
    
    @Override
    public SourceSinkDescribable getPluginDescribable(String pluginId) {
    	return plugins.getSourceSinkById(pluginId);
    }

    @Override
    public List<Profile> getDatasourceProfiles(final Long userId) {
        return conn.txNewReadOnly(new Callable<List<Profile>>() {
            @Override public List<Profile> call() {
                
                return profiles.getProfilesOf(userId);
                
            }
        });
    }
    
    @Override
    public Profile getPluginProfile(final Long profileId) {
    	return conn.txNewReadOnly(new Callable<Profile>() {
            @Override public Profile call() {
                
                return profiles.queryExistingProfile(profileId);
                
            }
        });
    }

    @Override
    public Profile deleteProfile(final Long userId, final Long profileId) {
        return conn.txNew(new Callable<Profile>() {
            @Override public Profile call() {
                
                return profiles.deleteProfile(profileId, userId);
                
            }
        });
    }

    @Override
    public List<String> getDatasourceOptions(final Long userId, final Long profileId, final String keyRingPassword) {
        return conn.txJoinReadOnly(new Callable<List<String>>() {
            @Override public List<String> call() {
                
                Profile p = profiles.getExistingUserProfile(profileId, userId);
                String profileDescription = p.getDescription();
                Datasource source = plugins.getDatasource(profileDescription);
                Properties accessData = authorization.fetchProfileAuthenticationData(p, keyRingPassword);
                return source.getAvailableOptions(accessData);
                
            }
        });
    }

    @Override
    public List<String> getStoredDatasourceOptions(final Long userId, final Long profileId, final Long jobId) {
        return conn.txJoinReadOnly(new Callable<List<String>>() {
            @Override public List<String> call() {

//                registration.ensureUserIsActive(username);
                BackupJob job = backupJobs.getExistingUserJob(jobId, userId);
                ProfileOptions sourceProfile = job.getSourceProfiles();
                return profiles.getProfileOptions(profileId, sourceProfile);
            
            }
        });
    }

	@Override
    public void changeProfile(final Long profileId, final Long jobId, final List<String> sourceOptions) {
		conn.txJoin(new Runnable() {
			@Override public void run() {

			    BackupJob backupjob = backupJobs.getExistingJob(jobId);
				ProfileOptions sourceProfiles = backupjob.getSourceProfiles();
                profiles.setProfileOptions(profileId, sourceProfiles, sourceOptions);
				
			}
		});
	}

    @Override
    public List<SourceSinkDescribable> getDatasinks() {
        return plugins.getConnectedDatasinks();
    }

    @Override
    public List<Profile> getDatasinkProfiles(final Long userId) {
        return conn.txNewReadOnly(new Callable<List<Profile>>() {
            @Override public List<Profile> call() {
                
                return profiles.getDatasinkProfilesOf(userId);
                
            }
        });
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
	    return plugins.getActionOptions(actionId);
	}

    @Override
    public void changeActionOptions(final String actionId, final Long jobId, final Map<String, String> actionOptions) {
        conn.txJoin(new Runnable() {
            @Override public void run() {

                backupJobs.updateJobActionOption(actionId, jobId, actionOptions);
            
            }
        });
    }

    private List<ActionProfile> getActionProfilesFor(BackupJob request) {
        return plugins.getActionProfilesFor(request);
    }
    
    /*
    
    @Override
    public AuthRequest preAuth(final String username, final String uniqueDescIdentifier,
            final String profileName, final String keyRing) {

        return conn.txNew(new Callable<AuthRequest>() {
            @Override public AuthRequest call() {

                BackMeUpUser user = getAuthorizedUser(username, keyRing);

                Properties p = new Properties();
                AuthRequest ar = plugins.configureAuth(p, uniqueDescIdentifier);
                
                SourceSinkDescribable desc = plugins.getSourceSinkById(uniqueDescIdentifier);
                Profile profile = profiles.createNewProfile(user, uniqueDescIdentifier, profileName, desc.getType());
                authorization.initProfileAuthInformation(profile, p, keyRing);
                
                ar.setProfile(profile);
                return ar;

            }
        });
    }
    
    */
    
    @Override
    public AuthRequest getPluginConfiguration(final String pluginId) {
    	return conn.txNew(new Callable<AuthRequest>() {
            @Override public AuthRequest call() {

//                BackMeUpUser user = getAuthorizedUser(username, keyRing);

                Properties p = new Properties();
                AuthRequest ar = plugins.configureAuth(p, pluginId);
                
//                SourceSinkDescribable desc = plugins.getSourceSinkById(uniqueDescIdentifier);
//                Profile profile = profiles.createNewProfile(user, uniqueDescIdentifier, profileName, desc.getType());
//                authorization.initProfileAuthInformation(profile, p, keyRing);
                
//                ar.setProfile(profile);
                return ar;

            }
        });
    }
    
    /*
    
    @Override
    public void postAuth(final Long profileId, final Properties props, final String keyRing) {
        if (keyRing == null) {
            throw new IllegalArgumentException("keyRing-Parameter cannot be null!");
        } else if (profileId == null) {
            throw new IllegalArgumentException("profileId-Parameter cannot be null!");
        } else if (props == null) {
            throw new IllegalArgumentException("properties-Parameter cannot be null!");
        }
        
        conn.txNew(new Runnable() {
            @Override public void run() {
                
                Profile p = profiles.queryExistingProfile(profileId);
                props.putAll(authorization.getProfileAuthInformation(p, keyRing));

                String sourceSinkId = p.getDescription();
                String userId = plugins.getAuthorizedUserId(sourceSinkId, props);
                profiles.setIdentification(p, userId);
                authorization.overwriteProfileAuthInformation(p, props, keyRing);

            }
        });
    }
    
    */
    
    @Override
    public Profile addPluginProfile(final String pluginId, final Profile profile, final Properties props, final List<String> options) {
    	return conn.txNew(new Callable<Profile>() {
            @Override public Profile call() {
            	
            	// TODO: onyl for oauth, why?
            	// -> props now filled with "callback=http://localhost:9998/oauth_callback" 
            	plugins.configureAuth(props, pluginId);               
                
                Profile p = profiles.createNewProfile(profile.getUser(), pluginId, profile.getProfileName(), profile.getType());
                String identification = plugins.getAuthorizedUserId(pluginId, props); // plugin -> postAuthorize
                profiles.setIdentification(p, identification); // ?
                authorization.overwriteProfileAuthInformation(p, props, profile.getUser().getPassword());
                return p;
            }
        });
    }
    
    @Override
    public void updatePluginProfile(final String pluginId, final Profile profile, final Properties props, final List<String> options) {
    	conn.txNew(new Runnable() {
            @Override public void run() {
                
                Profile p = profiles.queryExistingProfile(profile.getProfileId());
                if(p == null) {
                	p = profiles.createNewProfile(profile.getUser(), pluginId, profile.getProfileName(), profile.getType());
                }

                String identification = plugins.getAuthorizedUserId(pluginId, props); // plugin -> postAuthorize
                profiles.setIdentification(p, identification); // ?
                authorization.overwriteProfileAuthInformation(p, props, profile.getUser().getPassword());

            }
        });
    }
    /*
 // TODO Store profile data within keyserver!
    @Override
    public void addProfileEntries(final Long profileId, final Properties entries, final String keyRing) {
        conn.txNew(new Runnable() {
            @Override public void run() {
                
                Profile profile = profiles.queryExistingProfile(profileId);
                authorization.appendProfileAuthInformation(profile, entries, keyRing);
                profiles.save(profile); // TODO why save? has not been changed
                
            }
        });
    }
    */
    
    @Override
    public ValidationNotes validateProfile(final Long userId, final Long profileId, final String keyRing) {
        return conn.txJoinReadOnly(new Callable<ValidationNotes>() {
            @Override public ValidationNotes call() {

                String pluginName = null;
                try {
                    
                    Profile p = profiles.getExistingUserProfile(profileId, userId);
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
    
    /*
    @Override
    public Properties getMetadata(final String username, final Long profileId, final String keyRing) {
        return conn.txJoinReadOnly(new Callable<Properties>() {
            @Override public Properties call() {
                
                Profile profile = profiles.getExistingUserProfile(profileId, username);
                String sourceSinkId = profile.getDescription();
                SourceSinkDescribable ssd = plugins.getExistingSourceSink(sourceSinkId);
                Properties accessData = authorization.getProfileAuthInformation(profile, keyRing);
                return ssd.getMetadata(accessData);
                
            }
        });
    }
    */
    // ========================================================================
    
    @Override
    public ValidationNotes createBackupJob(BackupJob request) {
        try {
            conn.begin();

            ProfileOptions pos = request.getSourceProfiles();
//            Set<ProfileOptions> pos = profiles.getSourceProfilesOptionsFor(request.getSourceProfiles());
            Profile sink = profiles.queryExistingProfile(request.getSinkProfile().getProfileId());

            List<ActionProfile> actions = getActionProfilesFor(request);

//            ExecutionTime execTime = BackUpJobCreationHelper.getExecutionTimeFor(request);

            conn.rollback();
            
            BackupJob job = jobManager.createBackupJob(request.getUser(), pos, sink, actions, request.getStart(), request.getDelay(), request.getJobTitle(), request.isReschedule(), request.getTimeExpression());
            ValidationNotes vn = validateBackupJob(request.getUser().getUserId(), job.getId(), request.getUser().getPassword());
            vn.setJob(job);
            return vn;
            
        } finally {
            conn.rollback();
        }
    }

    @Override
    public BackupJob getBackupJob(final Long jobId) {
        return conn.txNewReadOnly(new Callable<BackupJob>() {
            @Override public BackupJob call() {

                return backupJobs.updateRequestFor(jobId);
                
            }
        });
    }
    
    @Override
    public BackupJob getBackupJobFull(final Long jobId) {
        return conn.txNewReadOnly(new Callable<BackupJob>() {
            @Override public BackupJob call() {
                
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

 // Note: keyRing won't be overridden
    @Override
    public BackupJob updateBackupJob(final Long userId, final BackupJob backupJob) {
        if (backupJob.getId() == null) {
            throw new IllegalArgumentException("JobId must not be null!");
        }
        
        BackupJob job = conn.txNew(new Callable<BackupJob>() {
            @Override public BackupJob call() {

                BackupJob persistentJob = backupJobs.getExistingUserJob(backupJob.getId(), userId);
                backupJobs.updateJob(persistentJob, backupJob);
                return persistentJob;
            
            }
        });

		return job;
    }
    
    @Override
    public List<BackupJob> getJobs(final Long userId) {
        return conn.txNewReadOnly(new Callable<List<BackupJob>>() {
            @Override public List<BackupJob> call() {
                
//                registration.ensureUserIsActive(username);
            	registration.getUserByUserId(userId, true);
                return backupJobs.getBackupJobsOf(userId);
                
            }
        });
    }

    @Override
    public void deleteJob(final Long userId, final Long jobId) {
        conn.txNew(new Runnable() {
            @Override public void run() {

//                registration.ensureUserIsActive(username);
            	registration.getUserByUserId(userId, true);
                backupJobs.deleteJob(userId, jobId);
                
            }
        });
    }
    
    @Override
    public List<Status> getStatus(final Long userId, final Long jobIdOrNull) {
        return conn.txNewReadOnly(new Callable<List<Status>>() {
            @Override public List<Status> call() {
                
            	registration.getUserByUserId(userId, true);
                List<Status> status = backupJobs.getStatus(userId, jobIdOrNull);

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
    public ProtocolDetails getProtocolDetails(Long userId, String fileId) {
        return search.getProtocolDetails(userId, fileId);
    }

    @Override
    public ProtocolOverview getProtocolOverview(final Long userId, final String duration) {
        return conn.txNewReadOnly(new Callable<ProtocolOverview>() {
            @Override public ProtocolOverview call() {
                
//                BackMeUpUser user = registration.getActiveUser(username);
            	BackMeUpUser user = registration.getUserByUserId(userId, true);
                
                Date to = new Date();
                Date from = duration.equals("month") ? new Date(to.getTime() - DelayTimes.DELAY_MONTHLY) :
                    new Date(to.getTime() - DelayTimes.DELAY_WEEKLY);
                
                return backupJobs.getProtocolOverview(user, from, to);
                
            }
        });
    }
    
    @Override
    public void updateJobProtocol(final Long userId, final Long jobId, final JobProtocolDTO jobProtocol) {
    	conn.txNew(new Runnable() {
            @Override public void run() {
                
//                BackMeUpUser user = registration.getActiveUser(username);
            	BackMeUpUser user = registration.getUserByUserId(userId, true);
                BackupJob job = backupJobs.getExistingUserJob(jobId, userId);
                backupJobs.createJobProtocol(user, job, jobProtocol);
                
            }
        });
    }
    
    @Override
    public void deleteJobProtocols(final Long userId) {
        conn.txNew(new Runnable() {
            @Override public void run() {
                
//                registration.ensureUserIsActive(username);
            	registration.getUserByUserId(userId, true);
                backupJobs.deleteProtocolsOf(userId);
                
            }
        });
    }

    @Override
    public long searchBackup(final Long userId, final String keyRingPassword, final String query) {
        try {

            return conn.txNew(new Callable<Long>() {
                @Override public Long call() {
                    
                    ensureUserIsAuthorized(userId, keyRingPassword);
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
    public void deleteIndexForUser(final Long userId) {
        conn.txNewReadOnly(new Runnable() {
            @Override public void run() {

//                BackMeUpUser user = registration.getActiveUser(username);
            	BackMeUpUser user = registration.getUserByUserId(userId, true);
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
    public SearchResponse queryBackup(final Long userId, final long searchId, final Map<String, List<String>> filters) {
        return conn.txNewReadOnly(new Callable<SearchResponse>() {
            @Override public SearchResponse call() {
                
//                BackMeUpUser user = registration.getActiveUser(username);
            	BackMeUpUser user = registration.getUserByUserId(userId, true);
                return search.runSearch(user, searchId, filters);

            }
        });
    }

    @Override
    public File getThumbnail(final Long userId, final String fileId) {
        return conn.txNewReadOnly(new Callable<File>() {
            @Override public File call() {
                
//                BackMeUpUser user = registration.getActiveUser(username);
            	BackMeUpUser user = registration.getUserByUserId(userId, true);
                return search.getThumbnailPathForFile(user, fileId);

            }
        });
    }

    //TODO Add password parameter to get token from keyserver to validate the profile
    @Override
    public ValidationNotes validateBackupJob(final Long userId, final Long jobId, final String keyRing) {
        return conn.txNewReadOnly(new Callable<ValidationNotes>() {
            @Override public ValidationNotes call() {
                
//                registration.ensureUserIsActive(username);
            	registration.getUserByUserId(userId, true);
                return validatePluginProfiles();

            }

            private ValidationNotes validatePluginProfiles() {
                ValidationNotes notes = new ValidationNotes();
                try {
                    BackupJob job = backupJobs.getExistingUserJob(jobId, userId); 
                    validateSourceProfiles(job.getSourceProfiles(), notes);

                    Long sinkProfileId = job.getSinkProfile().getProfileId();
                    getValidationEntriesForProfile(sinkProfileId, notes);

                } catch (BackMeUpException bme) {
                    notes.addValidationEntry(ValidationExceptionType.Error, bme);
                }
                return notes;
            }

            private void validateSourceProfiles(ProfileOptions sourceProfiles, ValidationNotes notes) {
            	ProfileOptions po = sourceProfiles;
                String sourceSinkId = po.getProfile().getDescription();
                plugins.validateSourceSinkExists(sourceSinkId, notes);

                Long profileId = po.getProfile().getProfileId();
                getValidationEntriesForProfile(profileId, notes);
            }

            private void getValidationEntriesForProfile(Long id, ValidationNotes notes) {
                notes.getValidationEntries().addAll(
                        validateProfile(userId, id, keyRing)
                        .getValidationEntries());
            }
        });
    }

    @Override
    public List<KeyserverLog> getKeysrvLogs(BackMeUpUser user) {
        return authorization.getLogs(user);
    }

}
