package org.backmeup.logic.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.Connection;
import org.backmeup.index.model.SearchResponse;
import org.backmeup.index.model.sharing.SharingPolicyEntry;
import org.backmeup.index.model.sharing.SharingPolicyEntry.SharingPolicyTypeEntry;
import org.backmeup.job.JobManager;
import org.backmeup.logic.AuthorizationLogic;
import org.backmeup.logic.BackupLogic;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.logic.PluginsLogic;
import org.backmeup.logic.ProfileLogic;
import org.backmeup.logic.SearchLogic;
import org.backmeup.logic.SharingLogic;
import org.backmeup.logic.UserRegistration;
import org.backmeup.model.AuthData;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.PluginConfigInfo;
import org.backmeup.model.Profile;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.constants.DelayTimes;
import org.backmeup.model.dto.JobProtocolDTO;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.PluginUnavailableException;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the BusinessLogic interface by delegating most operations to following layers: - DataAccessLayer -
 * JobManager - PluginLayer
 * 
 * If an error occurs within a method an exception will be thrown that must be handled by the client of the business
 * logic.
 * 
 * @author fschoeppl
 */
@ApplicationScoped
public class BusinessLogicImpl implements BusinessLogic {

    private static final String SHUTTING_DOWN_BUSINESS_LOGIC = "org.backmeup.logic.impl.BusinessLogicImpl.SHUTTING_DOWN_BUSINESS_LOGIC";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // There seems to be a problem with weld (can't find resource bundle 
    // with getClass().getSimpleName()). Therefore use class name. 
    private final ResourceBundle textBundle = ResourceBundle.getBundle("BusinessLogicImpl");

    @Inject
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
    private SharingLogic share;

    @Inject
    private ProfileLogic profiles;

    @Inject
    private BackupLogic backupJobs;

    @Inject
    private PluginsLogic plugins;

    @Inject
    @Configuration(key = "backmeup.autoVerifyUser")
    private Boolean autoVerifyUser;

    // ========================================================================

    // CDI lifecycle methods --------------------------------------------------

    @PreDestroy
    public void shutdown() {
        this.logger.debug(this.textBundle.getString(SHUTTING_DOWN_BUSINESS_LOGIC));
    }

    // ========================================================================

    // Authentication ---------------------------------------------------------

    @Override
    public BackMeUpUser authorize(final String username, final String password) {
        return this.conn.txJoinReadOnly(new Callable<BackMeUpUser>() {
            @Override
            public BackMeUpUser call() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUsername(username, true);
                BusinessLogicImpl.this.authorization.authorize(user, password);
                return user;
            }
        });
    }

    // ========================================================================

    // User operations --------------------------------------------------------

    @Override
    public BackMeUpUser getUserByUsername(final String username) {
        return this.conn.txJoinReadOnly(new Callable<BackMeUpUser>() {
            @Override
            public BackMeUpUser call() {

                return BusinessLogicImpl.this.registration.getUserByUsername(username, true);

            }
        });
    }

    @Override
    public BackMeUpUser getUserByUserId(final Long userId) {
        return this.conn.txJoinReadOnly(new Callable<BackMeUpUser>() {
            @Override
            public BackMeUpUser call() {

                return BusinessLogicImpl.this.registration.getUserByUserId(userId, true);

            }
        });
    }

    @Override
    public BackMeUpUser deleteUser(final Long userId) {
        BackMeUpUser user = this.conn.txNew(new Callable<BackMeUpUser>() {
            @Override
            public BackMeUpUser call() {

                BackMeUpUser u = BusinessLogicImpl.this.registration.getUserByUserId(userId);
                BusinessLogicImpl.this.authorization.unregister(u);
                BusinessLogicImpl.this.backupJobs.deleteJobsOf(u.getUserId());
                BusinessLogicImpl.this.profiles.deleteProfilesOf(u.getUserId());
                BusinessLogicImpl.this.registration.delete(u);
                return u;

            }
        });

        return user;
    }

    @Override
    public BackMeUpUser updateUser(final BackMeUpUser user) {
        return this.conn.txNew(new Callable<BackMeUpUser>() {
            @Override
            public BackMeUpUser call() {

                BusinessLogicImpl.this.registration.getUserByUsername(user.getUsername(), true);
                BusinessLogicImpl.this.registration.update(user);
                return user;

            }
        });
    }

    @Override
    public BackMeUpUser addUser(final BackMeUpUser newUser) {
        return this.conn.txNew(new Callable<BackMeUpUser>() {
            @Override
            public BackMeUpUser call() {
                BackMeUpUser user = BusinessLogicImpl.this.registration.register(newUser);
                BusinessLogicImpl.this.authorization.register(user);
                if (BusinessLogicImpl.this.autoVerifyUser) {
                    BusinessLogicImpl.this.registration.activateUserFor(user.getVerificationKey());
                } else {
                    BusinessLogicImpl.this.registration.sendVerificationEmailFor(user);
                }
                return user;

            }
        });
    }

    // ========================================================================

    // Plugin operations ------------------------------------------------------

    @Override
    public boolean isPluginAvailable(String pluginId) {
        return this.plugins.isPluginAvailable(pluginId);
    }

    @Override
    public List<PluginDescribable> getDatasources() {
        return this.plugins.getDatasources();
    }

    @Override
    public List<PluginDescribable> getDatasinks() {
        return this.plugins.getDatasinks();
    }

    @Override
    public List<PluginDescribable> getActions() {
        return this.plugins.getActions();
    }

    @Override
    public PluginDescribable getPluginDescribable(String pluginId) {
        return this.plugins.getPluginDescribableById(pluginId);
    }

    @Override
    public Profile getPluginProfile(final Long profileId) {
        return this.conn.txNewReadOnly(new Callable<Profile>() {
            @Override
            public Profile call() {

                return BusinessLogicImpl.this.profiles.getProfile(profileId);

            }
        });
    }

    @Override
    public void deleteProfile(final Long profileId) {
        this.conn.txJoin(new Runnable() {
            @Override
            public void run() {

                BusinessLogicImpl.this.profiles.deleteProfile(profileId);

            }
        });
    }

    @Override
    public PluginConfigInfo getPluginConfiguration(final String pluginId) {
        return this.conn.txNew(new Callable<PluginConfigInfo>() {

            @Override
            public PluginConfigInfo call() {
                PluginConfigInfo pluginConfigInfo = BusinessLogicImpl.this.plugins.getPluginConfigInfo(pluginId);
                return pluginConfigInfo;

            }
        });
    }

    @Override
    public PluginConfigInfo getPluginConfiguration(final String pluginId, final AuthData authData) {
        return this.conn.txNew(new Callable<PluginConfigInfo>() {

            @Override
            public PluginConfigInfo call() {
                PluginConfigInfo pluginConfigInfo = BusinessLogicImpl.this.plugins.getPluginConfigInfo(pluginId,
                        authData);
                return pluginConfigInfo;

            }
        });
    }

    @Override
    public Profile addPluginProfile(final Profile profile) {

        return this.conn.txNew(new Callable<Profile>() {
            @Override
            public Profile call() {
                // Check if plugin authorization data is required and still valid
                if ((profile.getAuthData() != null) && (profile.getAuthData().getId() != null)) {
                    AuthData authData = BusinessLogicImpl.this.profiles.getAuthData(profile.getAuthData().getId());
                    profile.setAuthData(authData);

                    String identification = BusinessLogicImpl.this.plugins.authorizePlugin(profile.getAuthData());
                    profile.getAuthData().setIdentification(identification);
                }

                // Check if plugin validation is required and properties and options are valid
                if (BusinessLogicImpl.this.plugins.requiresValidation(profile.getPluginId())) {
                    ValidationNotes notes = BusinessLogicImpl.this.plugins.validatePlugin(profile.getPluginId(),
                            profile.getProperties(), profile.getOptions());
                    if (!notes.getValidationEntries().isEmpty()) {
                        throw new ValidationException(ValidationExceptionType.ConfigException, notes);
                    }

                }

                // Everything is in place and valid, now we can store the new profile
                Profile p = BusinessLogicImpl.this.profiles.save(profile);

                return p;
            }
        });

    }

    @Override
    public ValidationNotes validateProfile(final Long userId, final Long profileId, final String keyRing) {
        return this.conn.txJoinReadOnly(new Callable<ValidationNotes>() {
            @Override
            public ValidationNotes call() {
                String pluginId = null;
                ValidationNotes notes = new ValidationNotes();

                try {
                    Profile p = BusinessLogicImpl.this.profiles.getProfile(profileId);
                    pluginId = p.getPluginId();
                    Validationable validator = BusinessLogicImpl.this.plugins.getValidator(pluginId);
                    Map<String, String> authProps = BusinessLogicImpl.this.authorization.getProfileAuthInformation(p,
                            keyRing);
                    notes.addAll(validator.validateProperties(authProps));
                    return notes;

                } catch (PluginUnavailableException pue) {
                    return ValidationNotes.createExceptionNotes(ValidationExceptionType.NoValidatorAvailable, pluginId,
                            pue);
                } catch (Exception pe) {
                    return ValidationNotes.createExceptionNotes(ValidationExceptionType.Error, pluginId, pe);
                }

            }
        });
    }

    public ValidationNotes validateProfile(final Profile profile) {
        return this.conn.txJoinReadOnly(new Callable<ValidationNotes>() {
            @Override
            public ValidationNotes call() {
                ValidationNotes notes = new ValidationNotes();

                try {
                    // Check if plugin authorization data is required and still valid
                    if ((profile.getAuthData() != null) && (profile.getAuthData().getId() != null)) {
                        AuthData authData = BusinessLogicImpl.this.profiles.getAuthData(profile.getAuthData().getId());
                        profile.setAuthData(authData);

                        String identification = BusinessLogicImpl.this.plugins.authorizePlugin(profile.getAuthData());
                        profile.getAuthData().setIdentification(identification);
                    }

                    // Check if plugin validation is required and properties and options are valid
                    if (BusinessLogicImpl.this.plugins.requiresValidation(profile.getPluginId())) {
                        notes.addAll(BusinessLogicImpl.this.plugins.validatePlugin(profile.getPluginId(),
                                profile.getProperties(), profile.getOptions()));
                    }
                    return notes;

                } catch (Exception e) {
                    notes.addValidationEntry(ValidationExceptionType.Error, profile.getPluginId(), e);
                    return notes;
                }
            }

        });
    }

    @Override
    public Profile updatePluginProfile(final Profile profile) {
        return this.conn.txNew(new Callable<Profile>() {
            @Override
            public Profile call() {
                // TODO: Refactor (see addPluginProfile method); put validation logic in own method
                // Check if plugin authorization data is required and still valid
                if ((profile.getAuthData() != null) && (profile.getAuthData().getId() != null)) {
                    AuthData authData = BusinessLogicImpl.this.profiles.getAuthData(profile.getAuthData().getId());
                    profile.setAuthData(authData);

                    String identification = BusinessLogicImpl.this.plugins.authorizePlugin(profile.getAuthData());
                    profile.getAuthData().setIdentification(identification);
                }

                // Check if plugin validation is required and properties and options are valid
                if (BusinessLogicImpl.this.plugins.requiresValidation(profile.getPluginId())) {
                    ValidationNotes notes = BusinessLogicImpl.this.plugins.validatePlugin(profile.getPluginId(),
                            profile.getProperties(), profile.getOptions());
                    if (!notes.getValidationEntries().isEmpty()) {
                        throw new ValidationException(ValidationExceptionType.ConfigException, notes);
                    }
                }

                return BusinessLogicImpl.this.profiles.updateProfile(profile);

            }
        });
    }

    // ========================================================================

    @Override
    public BackupJob createBackupJob(BackupJob backupJob) {
        try {
            validateBackupJob(backupJob);
            BackupJob job = this.jobManager.createBackupJob(backupJob);
            return job;
        } finally {
            //            conn.rollback();
        }
    }

    @Override
    public BackupJob getBackupJobFull(final Long jobId) {
        return this.conn.txNewReadOnly(new Callable<BackupJob>() {
            @Override
            public BackupJob call() {

                return BusinessLogicImpl.this.backupJobs.getExistingJob(jobId);

            }
        });
    }

    @Override
    public BackupJob updateBackupJob(final Long userId, final BackupJob backupJob) {
        if (backupJob.getId() == null) {
            throw new IllegalArgumentException("JobId must not be null!");
        }

        BackupJob job = this.conn.txNew(new Callable<BackupJob>() {
            @Override
            public BackupJob call() {

                BackupJob persistentJob = BusinessLogicImpl.this.backupJobs.getExistingUserJob(backupJob.getId(),
                        userId);
                BusinessLogicImpl.this.backupJobs.updateJob(persistentJob, backupJob);
                return persistentJob;

            }
        });

        return job;
    }

    @Override
    public List<BackupJob> getJobs(final Long userId) {
        return this.conn.txNewReadOnly(new Callable<List<BackupJob>>() {
            @Override
            public List<BackupJob> call() {

                BusinessLogicImpl.this.registration.getUserByUserId(userId, true);
                return BusinessLogicImpl.this.backupJobs.getBackupJobsOf(userId);

            }
        });
    }

    @Override
    public void deleteJob(final Long userId, final Long jobId) {
        this.conn.txNew(new Runnable() {
            @Override
            public void run() {

                BusinessLogicImpl.this.registration.getUserByUserId(userId, true);
                BusinessLogicImpl.this.backupJobs.deleteJob(userId, jobId);

            }
        });
    }

    @Override
    public ProtocolDetails getProtocolDetails(Long userId, String fileId) {
        return this.search.getProtocolDetails(userId, fileId);
    }

    @Override
    public ProtocolOverview getProtocolOverview(final Long userId, final String duration) {
        return this.conn.txNewReadOnly(new Callable<ProtocolOverview>() {
            @Override
            public ProtocolOverview call() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(userId, true);

                Date to = new Date();
                Date from = duration.equals("month") ? new Date(to.getTime() - DelayTimes.DELAY_MONTHLY) : new Date(to
                        .getTime() - DelayTimes.DELAY_WEEKLY);

                return BusinessLogicImpl.this.backupJobs.getProtocolOverview(user, from, to);

            }
        });
    }

    @Override
    public void updateJobProtocol(final Long userId, final Long jobId, final JobProtocolDTO jobProtocol) {
        this.conn.txNew(new Runnable() {
            @Override
            public void run() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(userId, true);
                BackupJob job = BusinessLogicImpl.this.backupJobs.getExistingUserJob(jobId, userId);
                BusinessLogicImpl.this.backupJobs.createJobProtocol(user, job, jobProtocol);

            }
        });
    }

    @Override
    public void deleteJobProtocols(final Long userId) {
        this.conn.txNew(new Runnable() {
            @Override
            public void run() {

                BusinessLogicImpl.this.registration.getUserByUserId(userId, true);
                BusinessLogicImpl.this.backupJobs.deleteProtocolsOf(userId);

            }
        });
    }

    @Override
    public void validateBackupJob(final BackupJob backupJob) {
        this.conn.txNewReadOnly(new Runnable() {
            @Override
            public void run() {
                ValidationNotes notes = new ValidationNotes();
                try {
                    notes.addAll(validateProfile(backupJob.getSourceProfile()));
                    notes.addAll(validateProfile(backupJob.getSinkProfile()));

                    for (Profile actionProfile : backupJob.getActionProfiles()) {
                        notes.addAll(validateProfile(actionProfile));
                    }

                } catch (BackMeUpException bme) {
                    notes.addValidationEntry(ValidationExceptionType.Error, bme);
                }

                if (!notes.getValidationEntries().isEmpty()) {
                    throw new ValidationException(ValidationExceptionType.ConfigException, notes);
                }
            }
        });
    }

    @Override
    public AuthData addPluginAuthData(final AuthData authData) {
        return this.conn.txNew(new Callable<AuthData>() {
            @Override
            public AuthData call() {

                if (authData.getUser() == null) {
                    throw new IllegalArgumentException("User must not be null");
                }

                if (!BusinessLogicImpl.this.plugins.requiresAuthorization(authData.getPluginId())) {
                    throw new PluginException(authData.getPluginId(), "AuthData is not required for this plugin");
                }

                // The following statement calls the authorize method of the plugin authorizable
                // It checks if the authentication data is required and valid
                BusinessLogicImpl.this.plugins.authorizePlugin(authData);
                return BusinessLogicImpl.this.profiles.addAuthData(authData);

            }
        });

    }

    @Override
    public AuthData getPluginAuthData(final Long authDataId) {
        return this.conn.txNewReadOnly(new Callable<AuthData>() {
            @Override
            public AuthData call() {

                return BusinessLogicImpl.this.profiles.getAuthData(authDataId);

            }
        });
    }

    @Override
    public List<AuthData> listPluginAuthData(final Long userId) {
        return this.conn.txNewReadOnly(new Callable<List<AuthData>>() {
            @Override
            public List<AuthData> call() {

                return BusinessLogicImpl.this.profiles.getAuthDataOf(userId);

            }
        });
    }

    @Override
    public AuthData updatePluginAuthData(final AuthData authData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deletePluginAuthData(final Long authDataId) {
        this.conn.txNew(new Runnable() {
            @Override
            public void run() {

                BusinessLogicImpl.this.profiles.deleteAuthData(authDataId);

            }
        });

    }

    // SEARCH ========================================================================
    @Override
    public SearchResponse queryBackup(final Long userId, final String query, final String source, final String type,
            final String job, final String owner) {
        return this.conn.txNewReadOnly(new Callable<SearchResponse>() {
            @Override
            public SearchResponse call() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(userId, true);
                return BusinessLogicImpl.this.search.runSearch(user, query, source, type, job, owner);

            }
        });
    }

    // SHARING ========================================================================
    @Override
    public Set<SharingPolicyEntry> getAllOwnedSharingPolicies(final Long ownerId) {
        return this.conn.txNewReadOnly(new Callable<Set<SharingPolicyEntry>>() {
            @Override
            public Set<SharingPolicyEntry> call() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(ownerId, true);
                return BusinessLogicImpl.this.share.getAllOwned(user);

            }
        });
    }

    @Override
    public Set<SharingPolicyEntry> getAllIncomingSharingPolicies(final Long ownerId) {
        return this.conn.txNewReadOnly(new Callable<Set<SharingPolicyEntry>>() {
            @Override
            public Set<SharingPolicyEntry> call() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(ownerId, true);
                return BusinessLogicImpl.this.share.getAllIncoming(user);

            }
        });
    }

    @Override
    public SharingPolicyEntry createAndAddSharingPolicy(final Long currUserId, final Long sharingWithUserId,
            final SharingPolicyTypeEntry policy, final String sharedElementID, final String name,
            final String description) {
        return this.conn.txNew(new Callable<SharingPolicyEntry>() {
            @Override
            public SharingPolicyEntry call() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(currUserId, true);
                BackMeUpUser sharingWith = BusinessLogicImpl.this.registration.getUserByUserId(sharingWithUserId);
                return BusinessLogicImpl.this.share.add(user, sharingWith, policy, sharedElementID, name, description);

            }
        });
    }

    @Override
    public String removeOwnedSharingPolicy(final Long currUserId, final Long policyID) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(currUserId, true);
                return BusinessLogicImpl.this.share.removeOwned(user, policyID);

            }
        });
    }

    @Override
    public String removeAllOwnedSharingPolicies(final Long currUserId) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(currUserId, true);
                return BusinessLogicImpl.this.share.removeAllOwned(user);

            }
        });
    }

    @Override
    public String approveIncomingSharing(final Long currUserId, final Long policyID) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(currUserId, true);
                return BusinessLogicImpl.this.share.acceptIncomingSharing(user, policyID);

            }
        });
    }

    @Override
    public String declineIncomingSharing(final Long currUserId, final Long policyID) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(currUserId, true);
                return BusinessLogicImpl.this.share.declineIncomingSharing(user, policyID);

            }
        });
    }

}
