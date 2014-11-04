package org.backmeup.logic.impl;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
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
import org.backmeup.index.model.FileItem;
import org.backmeup.index.model.SearchResponse;
import org.backmeup.job.JobManager;
import org.backmeup.logic.AuthorizationLogic;
import org.backmeup.logic.BackupLogic;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.logic.PluginsLogic;
import org.backmeup.logic.ProfileLogic;
import org.backmeup.logic.SearchLogic;
import org.backmeup.logic.UserRegistration;
import org.backmeup.model.AuthData;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.PluginConfigInfo;
import org.backmeup.model.Profile;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.StatusWithFiles;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.constants.DelayTimes;
import org.backmeup.model.dto.JobProtocolDTO;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.PluginUnavailableException;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.api.connectors.Datasource;
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
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    // There seems to be a problem with weld (can't find resource bundle 
    // with getClass().getSimpleName()). Therefore use class name. 
    private final ResourceBundle textBundle = ResourceBundle.getBundle("BusinessLogicImpl");

    private JobManager jobManager;

    @Inject
    private Connection conn;

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
     }
    
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
//                search.deleteIndexOf(u.getUserId());
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
    
    // ========================================================================
	
	// Plugin operations ------------------------------------------------------


    @Override
    public List<PluginDescribable> getDatasources() {
        return plugins.getDatasources();
    }
    
    @Override
    public boolean isPluginAvailable(String pluginId) {
    	return plugins.isPluginAvailable(pluginId);
    }
    
    @Override
    public PluginDescribable getPluginDescribable(String pluginId) {
    	return plugins.getPluginDescribableById(pluginId);
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

    @Deprecated
    @Override
    public Profile deleteProfile(final Long userId, final Long profileId) {
        return conn.txNew(new Callable<Profile>() {
            @Override public Profile call() {
                
                return profiles.deleteProfile(profileId, userId);
                
            }
        });
    }
    
    @Override
    public void deleteProfile(final Long profileId) {
    	conn.txJoin(new Runnable() {
			@Override public void run() {
                
                profiles.deleteProfile(profileId);
                
            }
        });
    }

    @Override
    public List<String> getDatasourceOptions(final Long userId, final Long profileId, final String keyRingPassword) {
        return conn.txJoinReadOnly(new Callable<List<String>>() {
            @Override public List<String> call() {
                
                Profile p = profiles.getExistingUserProfile(profileId, userId);
                String pluginId = p.getPluginId();
                Datasource source = plugins.getDatasource(pluginId);
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
                Profile sourceProfile = job.getSourceProfile();
                return profiles.getProfileOptions(profileId, sourceProfile);
            
            }
        });
    }

	@Override
    public void changeProfile(final Long profileId, final Long jobId, final List<String> sourceOptions) {
		conn.txJoin(new Runnable() {
			@Override public void run() {

			    BackupJob backupjob = backupJobs.getExistingJob(jobId);
				Profile sourceProfiles = backupjob.getSourceProfile();
                profiles.setProfileOptions(profileId, sourceProfiles, sourceOptions);
				
			}
		});
	}

    @Override
    public List<PluginDescribable> getDatasinks() {
        return plugins.getDatasinks();
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
    public List<PluginDescribable> getActions() {
        return plugins.getActions();
    }

    @Override
    public Profile getStoredActionOptions(final String actionId, final Long jobId) {
        return conn.txJoinReadOnly(new Callable<Profile>() {
            @Override public Profile call() {

                return backupJobs.getJobActionOption(actionId, jobId);
            
            }
        });
    }

    @Deprecated
	@Override
	public List<String> getActionOptions(String actionId) {
	    return plugins.getActionOptions(actionId);
	}

    @Deprecated
    @Override
    public void changeActionOptions(final String actionId, final Long jobId, final Map<String, String> actionOptions) {
        conn.txJoin(new Runnable() {
            @Override public void run() {

                backupJobs.updateJobActionOption(actionId, jobId, actionOptions);
            
            }
        });
    }

    @Deprecated
    private List<Profile> getActionProfilesFor(BackupJob request) {
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
    
    @Deprecated
    @Override
    public AuthRequest getPluginConfiguration(final String pluginId) {
    	return conn.txNew(new Callable<AuthRequest>() {
            @Override public AuthRequest call() {

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
    
    @Override
    public PluginConfigInfo getPluginConfiguration(final String pluginId, String dummy) {
    	return conn.txNew(new Callable<PluginConfigInfo>() {
    		
            @Override public PluginConfigInfo call() {
            	PluginConfigInfo pluginConfigInfo = plugins.getPluginConfigInfo(pluginId);
                return pluginConfigInfo;

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
    public Profile addPluginProfile(final Profile profile) {
    	
        return conn.txNew(new Callable<Profile>() {
            @Override public Profile call() {
            	// Check if plugin authorization data is required and still valid
                if((profile.getAuthData() != null) && (profile.getAuthData().getId() != null)) {
                    AuthData authData = profiles.getAuthData(profile.getAuthData().getId());
                    profile.setAuthData(authData);
                    
                    String identification = plugins.authorizePlugin(profile.getAuthData());
                    profile.setIdentification(identification);
                }			
				
                // Check if plugin validation is required and properties and options are valid
				if (plugins.requiresValidation(profile.getPluginId())) {
					plugins.validatePlugin(profile.getPluginId(), profile.getProperties(), profile.getOptions());
				}

				// Everything is in place and valid, now we can store the new profile
				Profile p = profiles.save(profile);

				return p;
            }
        });
        
    }
        
    @Override
    public ValidationNotes validateProfile(final Long userId, final Long profileId, final String keyRing) {
        return conn.txJoinReadOnly(new Callable<ValidationNotes>() {
            @Override public ValidationNotes call() {

                String pluginId = null;
                try {
                    
                    Profile p = profiles.getExistingUserProfile(profileId, userId);
                    pluginId = p.getPluginId();
                    Validationable validator = plugins.getValidator(pluginId);
                    Properties accessData = authorization.getProfileAuthInformation(p, keyRing);
                    Map<String, String> authProps = new HashMap<String, String>();
                    for (final String name: accessData.stringPropertyNames())
                        authProps.put(name, accessData.getProperty(name));
                    return validator.validateProperties(authProps);
                    
                } catch (PluginUnavailableException pue) {
                    ValidationNotes notes = new ValidationNotes();
                    notes.addValidationEntry(ValidationExceptionType.NoValidatorAvailable, pluginId, pue);
                    return notes;
                } catch (Exception pe) {
                    ValidationNotes notes = new ValidationNotes();
                    notes.addValidationEntry(ValidationExceptionType.Error, pluginId, pe);
                    return notes;
                }
            
            }
        });
    }
    
    public ValidationNotes validateProfile(final Profile profile) {
        return conn.txJoinReadOnly(new Callable<ValidationNotes>() {
        	
            @Override public ValidationNotes call() {
            	ValidationNotes notes = new ValidationNotes();
            	
                try {
                	// Check if plugin authorization data is required and still valid
                    if((profile.getAuthData() != null) && (profile.getAuthData().getId() != null)) {
                        AuthData authData = profiles.getAuthData(profile.getAuthData().getId());
                        profile.setAuthData(authData);
                        
                        String identification = plugins.authorizePlugin(profile.getAuthData());
                        profile.setIdentification(identification);
                    }			
    				
                    // Check if plugin validation is required and properties and options are valid
    				if (plugins.requiresValidation(profile.getPluginId())) {
    					ValidationNotes vn = plugins.validatePlugin(profile.getPluginId(), profile.getProperties(), profile.getOptions());
    					notes.addAll(vn);
    				}
    				return notes;
                    
                } catch (Exception e) {
                    notes.addValidationEntry(ValidationExceptionType.Error, profile.getPluginId(), e);
                    return notes;
                }
            }
            
        });
    }
    
    @Deprecated
    @Override
    public Profile addPluginProfile(final String pluginId, final Profile profile, final Properties props, final List<String> options) {
    	return conn.txNew(new Callable<Profile>() {
            @Override public Profile call() {
            	
            	// TODO: onyl for oauth, why?
            	// -> props now filled with "callback=http://localhost:9998/oauth_callback" 
            	plugins.configureAuth(props, pluginId);               
                
                Profile p = profiles.createNewProfile(profile.getUser(), pluginId, profile.getName(), profile.getType());
                String identification = plugins.getAuthorizedUserId(pluginId, props); // plugin -> postAuthorize
                profiles.setIdentification(p, identification); // ?
                authorization.overwriteProfileAuthInformation(p, props, profile.getUser().getPassword());
                return p;
            }
        });
    }
    
    @Deprecated
    @Override
    public void updatePluginProfile(final String pluginId, final Profile profile, final Properties props, final List<String> options) {
    	conn.txNew(new Runnable() {
            @Override public void run() {
                
                Profile p = profiles.queryExistingProfile(profile.getId());
                if(p == null) {
                	p = profiles.createNewProfile(profile.getUser(), pluginId, profile.getName(), profile.getType());
                }

                String identification = plugins.getAuthorizedUserId(pluginId, props); // plugin -> postAuthorize
                profiles.setIdentification(p, identification); // ?
                authorization.overwriteProfileAuthInformation(p, props, profile.getUser().getPassword());

            }
        });
    }
    
    @Override
    public Profile updatePluginProfile(final Profile profile) {
    	return conn.txNew(new Callable<Profile>() {
            @Override public Profile call() {
            	// TODO: Refactor (see addPluginProfile method); put validation logic in own method
            	// Check if plugin authorization data is required and still valid
                if((profile.getAuthData() != null) && (profile.getAuthData().getId() != null)) {
                    AuthData authData = profiles.getAuthData(profile.getAuthData().getId());
                    profile.setAuthData(authData);
                    
                    String identification = plugins.authorizePlugin(profile.getAuthData());
                    profile.setIdentification(identification);
                }			
				
                // Check if plugin validation is required and properties and options are valid
				if (plugins.requiresValidation(profile.getPluginId())) {
					plugins.validatePlugin(profile.getPluginId(), profile.getProperties(), profile.getOptions());
				}
                
                return profiles.updateProfile(profile);
                
            }
        });
    };
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
    
    @Deprecated
    @Override
    public ValidationNotes createBackupJob(BackupJob backupJob) {
        try {
            conn.begin();
            
            BackMeUpUser user = backupJob.getUser();
            Date startDate = backupJob.getStart();
            long delayTime = backupJob.getDelay();
            String jobTitle = backupJob.getJobTitle();
            boolean reschedule = backupJob.isReschedule();
            String timeExpression = backupJob.getTimeExpression();

            Profile source = backupJob.getSourceProfile();
            Profile sink = backupJob.getSinkProfile();
            List<Profile> actions = backupJob.getActionProfiles();

            conn.rollback();
            
            BackupJob job = jobManager.createBackupJob(user, source, sink, actions, startDate, delayTime, jobTitle, reschedule, timeExpression);
            ValidationNotes vn = validateBackupJob(backupJob.getUser().getUserId(), job.getId(), backupJob.getUser().getPassword());
            vn.setJob(job);
            return vn;
            
        } finally {
            conn.rollback();
        }
    }
    
    @Override
    public BackupJob createBackupJob(BackupJob backupJob, String dummy) {
        try {
        	ValidationNotes notes = validateBackupJob(backupJob);
        	if(!notes.getValidationEntries().isEmpty()){
        		//TODO: throw exception?
        	}
            BackupJob job = jobManager.createBackupJob(backupJob);
            return job;
        } finally {
//            conn.rollback();
        }
    }

    @Override
    public BackupJob getBackupJob(final Long jobId) {
        return conn.txNewReadOnly(new Callable<BackupJob>() {
            @Override public BackupJob call() {

                return backupJobs.getExistingJob(jobId);
                
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
    public List<StatusWithFiles> getStatus(final Long userId, final Long jobIdOrNull) {
        return conn.txNewReadOnly(new Callable<List<StatusWithFiles>>() {
            @Override public List<StatusWithFiles> call() {
                
            	registration.getUserByUserId(userId, true);
                List<StatusWithFiles> status = backupJobs.getStatus(userId, jobIdOrNull);

                if (status.size() > 0) {
                    Long newOrExistingId = status.get(0).getStatus().getJob().getId();
                    addFileItemsToStatuses(userId, status, newOrExistingId);
                }
                
                return status;
                
            }
        });
    }

    private void addFileItemsToStatuses(Long userId, List<StatusWithFiles> status, Long jobId) {
        Set<FileItem> fileItems = search.getAllFileItems(userId, jobId);
        for (StatusWithFiles stat : status) {
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
    public void deleteIndexForUser(final Long userId) {
        conn.txNewReadOnly(new Runnable() {
            @Override public void run() {

//                BackMeUpUser user = registration.getActiveUser(username);
            	registration.getUserByUserId(userId, true);
                search.deleteIndexOf(userId);
                
            }
        });
    }

    @Override
    public void deleteIndexForJobAndTimestamp(final Long userId, final Long jobId, final Long timestamp) {
        conn.txNewReadOnly(new Runnable() {
            @Override public void run() {

                BackupJob job = backupJobs.getExistingJob(jobId);
                search.delete(userId, job.getId(), timestamp);
                
            }
        });
    }

    @Override
    public SearchResponse queryBackup(final Long userId, final String query, final String source, final String type, final String job) {
        return conn.txNewReadOnly(new Callable<SearchResponse>() {
            @Override public SearchResponse call() {
                
//                BackMeUpUser user = registration.getActiveUser(username);
            	BackMeUpUser user = registration.getUserByUserId(userId, true);
                return search.runSearch(user, query, source, type, job);

            }
        });
    }

    @Deprecated
    @Override
    public File getThumbnail(final Long userId, final String fileId) {
        return conn.txNewReadOnly(new Callable<File>() {
            @Override public File call() {
                
//                BackMeUpUser user = registration.getActiveUser(username);
            	registration.getUserByUserId(userId, true);
                return search.getThumbnailPathForFile(userId, fileId);

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
                    validateSourceProfiles(job.getSourceProfile(), notes);

                    Long sinkProfileId = job.getSinkProfile().getId();
                    getValidationEntriesForProfile(sinkProfileId, notes);

                } catch (BackMeUpException bme) {
                    notes.addValidationEntry(ValidationExceptionType.Error, bme);
                }
                return notes;
            }

            private void validateSourceProfiles(Profile sourceProfile, ValidationNotes notes) {
            	Profile source = sourceProfile;
                String sourceSinkId = source.getPluginId();
                plugins.validateSourceSinkExists(sourceSinkId, notes);

                Long profileId = source.getId();
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
    public ValidationNotes validateBackupJob(final BackupJob backupJob) {
        return conn.txNewReadOnly(new Callable<ValidationNotes>() {
            @Override public ValidationNotes call() {

            	ValidationNotes notes = new ValidationNotes();
                try {
                    getValidationEntriesForProfile(backupJob.getSourceProfile(), notes);

                    getValidationEntriesForProfile(backupJob.getSinkProfile(), notes);
                    
                    for(Profile actionProfile : backupJob.getActionProfiles()) {
                    	getValidationEntriesForProfile(actionProfile, notes);
                    }

                } catch (BackMeUpException bme) {
                    notes.addValidationEntry(ValidationExceptionType.Error, bme);
                }
                return notes;

            }
            
            private void getValidationEntriesForProfile(Profile profile, ValidationNotes notes) {
                notes.getValidationEntries().addAll(
                        validateProfile(profile)
                        .getValidationEntries());
            }
        });
    }

//    @Override
//    public List<KeyserverLog> getKeysrvLogs(BackMeUpUser user) {
//        return authorization.getLogs(user);
//    }

	@Override
	public AuthData addPluginAuthData(final AuthData authData) {
		// TODO Auto-generated method stub
		
		return conn.txNew(new Callable<AuthData>() {
            @Override public AuthData call() {
            	
				if (authData.getUser() == null) {
					throw new IllegalArgumentException("User must not be null");
				}

				if (!plugins.requiresAuthorization(authData.getPluginId())) {
					throw new PluginException(authData.getPluginId(), "AuthData is not required for this plugin");
				}
            	
            	Properties authProps = new Properties();
            	authProps.putAll(authData.getProperties());
            	
            	// The following statement calls the authorize method of the plugin authorizable
            	// It checks if the authentication data is required and valid
            	String identification = plugins.authorizePlugin(authData);
            	return profiles.addAuthData(authData);
               
            }
        });

	}

	@Override
	public AuthData getPluginAuthData(final Long authDataId) {
		return conn.txNewReadOnly(new Callable<AuthData>() {
            @Override public AuthData call() {
            	
            	return profiles.getAuthData(authDataId);
               
            }
        });
	}
	
	@Override
	public List<AuthData> listPluginAuthData(final Long userId) {
		return conn.txNewReadOnly(new Callable<List<AuthData>>() {
            @Override public List<AuthData> call() {
            	
            	return profiles.getAuthDataOf(userId);
               
            }
        });
	}

	@Override
	public AuthData updatePluginAuthData(final AuthData authData) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deletePluginAuthData(final Long authDataId) {
		conn.txNew(new Runnable() {
            @Override public void run() {
                
            	profiles.deleteAuthData(authDataId);
            	
            }
        });
		
	}

}
