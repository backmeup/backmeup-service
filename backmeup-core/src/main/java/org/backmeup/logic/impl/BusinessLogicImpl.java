package org.backmeup.logic.impl;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.Connection;
import org.backmeup.index.model.SearchResponse;
import org.backmeup.index.model.sharing.SharingPolicyEntry;
import org.backmeup.index.model.sharing.SharingPolicyEntry.SharingPolicyTypeEntry;
import org.backmeup.index.model.tagging.TaggedCollectionEntry;
import org.backmeup.job.JobManager;
import org.backmeup.logic.BackupLogic;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.logic.CollectionLogic;
import org.backmeup.logic.FriendlistLogic;
import org.backmeup.logic.HeritageLogic;
import org.backmeup.logic.PluginsLogic;
import org.backmeup.logic.ProfileLogic;
import org.backmeup.logic.SearchLogic;
import org.backmeup.logic.SharingLogic;
import org.backmeup.logic.UserRegistration;
import org.backmeup.logic.WorkerLogic;
import org.backmeup.model.AuthData;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJobExecution;
import org.backmeup.model.FriendlistUser;
import org.backmeup.model.FriendlistUser.FriendListType;
import org.backmeup.model.PluginConfigInfo;
import org.backmeup.model.Profile;
import org.backmeup.model.Token;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.WorkerInfo;
import org.backmeup.model.WorkerMetric;
import org.backmeup.model.dto.WorkerConfigDTO;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.ValidationExceptionType;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessLogicImpl.class);

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
    private SearchLogic search;

    @Inject
    private SharingLogic share;

    @Inject
    private HeritageLogic heritage;

    @Inject
    private CollectionLogic taggedCollection;

    @Inject
    private ProfileLogic profiles;

    @Inject
    private BackupLogic backupJobs;

    @Inject
    private PluginsLogic plugins;

    @Inject
    private WorkerLogic workers;

    @Inject
    private FriendlistLogic friends;

    @Inject
    @Configuration(key = "backmeup.autoVerifyUser")
    private Boolean autoVerifyUser;

    // ========================================================================

    // CDI lifecycle methods --------------------------------------------------

    @PreDestroy
    public void shutdown() {
        LOGGER.debug(this.textBundle.getString(SHUTTING_DOWN_BUSINESS_LOGIC));
    }

    // ========================================================================

    // Authentication ---------------------------------------------------------

    @Override
    public Token authorize(final String username, final String password) {
        return this.conn.txJoinReadOnly(new Callable<Token>() {
            @Override
            public Token call() {

                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUsername(username, true);
                Token token = BusinessLogicImpl.this.registration.authorize(user, password);
                return token;
            }
        });
    }

    @Override
    public Token authorize(final String activationCode) {
        return this.conn.txJoinReadOnly(new Callable<Token>() {
            @Override
            public Token call() {
                Token token = BusinessLogicImpl.this.registration.authorize(activationCode);
                return token;
            }
        });
    }
    
    @Override
    public Token authorizeWorker(final String workerId, final String workerSecret) {
        return this.conn.txJoinReadOnly(new Callable<Token>() {
            @Override
            public Token call() {
                return BusinessLogicImpl.this.workers.authorize(workerId, workerSecret);
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
    public BackMeUpUser getUserByKeyserverUserId(final String keyserverUserId) {
        return this.conn.txJoinReadOnly(new Callable<BackMeUpUser>() {
            @Override
            public BackMeUpUser call() {

                return BusinessLogicImpl.this.registration.getUserByKeyserverUserId(keyserverUserId);

            }
        });
    }

    @Override
    public BackMeUpUser deleteUser(final BackMeUpUser activeUser, final Long userId) {
        BackMeUpUser user = this.conn.txNew(new Callable<BackMeUpUser>() {
            @Override
            public BackMeUpUser call() {

                BackMeUpUser u = BusinessLogicImpl.this.registration.getUserByUserId(userId);
                BusinessLogicImpl.this.backupJobs.deleteBackupJobsOf(u.getUserId());
                BusinessLogicImpl.this.profiles.deleteProfilesOf(activeUser, u.getUserId());
                BusinessLogicImpl.this.registration.delete(activeUser);
                //TODO remove all dependencies, inkl. sharings, anonymous user when removing from friends list, etc.
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
                if (BusinessLogicImpl.this.autoVerifyUser) {
                    BusinessLogicImpl.this.registration.activateUserFor(user.getVerificationKey());
                } else {
                    BusinessLogicImpl.this.registration.sendVerificationEmailFor(user);
                }
                return user;

            }
        });
    }

    @Override
    public BackMeUpUser addAnonymousUser(final BackMeUpUser activeUser) {
        return this.conn.txNew(new Callable<BackMeUpUser>() {
            @Override
            public BackMeUpUser call() {
                BackMeUpUser user = BusinessLogicImpl.this.registration.registerAnonymous(activeUser);
                return user;
            }
        });
    }

    @Override
    public String getAnonymousUserActivationCode(final BackMeUpUser currentUser, final Long userId) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {
                BackMeUpUser anonUser = BusinessLogicImpl.this.registration.getUserByUserId(userId);
                if (!anonUser.isAnonymous()) {
                    throw new BackMeUpException("Activation code only available for anonymous users");
                }
                return BusinessLogicImpl.this.registration.getActivationCode(currentUser, anonUser);
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
    public Profile getPluginProfile(final BackMeUpUser currentUser, final Long profileId) {
        return this.conn.txNewReadOnly(new Callable<Profile>() {
            @Override
            public Profile call() {

                return BusinessLogicImpl.this.profiles.getProfile(currentUser, profileId);

            }
        });
    }

    @Override
    public void deleteProfile(final BackMeUpUser currentUser, final Long profileId) {
        this.conn.txJoin(new Runnable() {
            @Override
            public void run() {

                BusinessLogicImpl.this.profiles.deleteProfile(currentUser, profileId);

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
                PluginConfigInfo pluginConfigInfo = BusinessLogicImpl.this.plugins.getPluginConfigInfo(pluginId, authData);
                return pluginConfigInfo;

            }
        });
    }

    @Override
    public Profile addPluginProfile(final BackMeUpUser currentUser, final Profile profile) {

        return this.conn.txNew(new Callable<Profile>() {
            @Override
            public Profile call() {
                // Check if plugin authorization data is required and still valid
                if ((profile.getAuthData() != null) && (profile.getAuthData().getId() != null)) {
                    AuthData authData = BusinessLogicImpl.this.profiles.getAuthData(currentUser, profile.getAuthData().getId());
                    profile.setAuthData(authData);

                    String identification = BusinessLogicImpl.this.plugins.authorizePlugin(profile.getAuthData());
                    profile.getAuthData().setIdentification(identification);
                }

                // Check if plugin validation is required and properties and options are valid
                if (BusinessLogicImpl.this.plugins.requiresValidation(profile.getPluginId())) {
                    ValidationNotes notes = BusinessLogicImpl.this.plugins.validatePlugin(profile.getPluginId(), profile.getProperties(),
                            profile.getOptions());
                    if (!notes.getValidationEntries().isEmpty()) {
                        throw new ValidationException(ValidationExceptionType.ConfigException, notes);
                    }

                }

                // Everything is in place and valid, now we can store the new profile
                Profile p = BusinessLogicImpl.this.profiles.saveProfile(profile);

                return p;
            }
        });

    }

    public ValidationNotes validateProfile(final BackMeUpUser currentUser, final Profile profile) {
        return this.conn.txJoinReadOnly(new Callable<ValidationNotes>() {
            @Override
            public ValidationNotes call() {
                ValidationNotes notes = new ValidationNotes();

                try {
                    // Check if plugin authorization data is required and still valid
                    if ((profile.getAuthData() != null) && (profile.getAuthData().getId() != null)) {
                        AuthData authData = BusinessLogicImpl.this.profiles.getAuthData(currentUser, profile.getAuthData().getId());
                        profile.setAuthData(authData);

                        String identification = BusinessLogicImpl.this.plugins.authorizePlugin(profile.getAuthData());
                        profile.getAuthData().setIdentification(identification);
                    }

                    // Check if plugin validation is required and properties and options are valid
                    if (BusinessLogicImpl.this.plugins.requiresValidation(profile.getPluginId())) {
                        notes.addAll(BusinessLogicImpl.this.plugins.validatePlugin(profile.getPluginId(), profile.getProperties(),
                                profile.getOptions()));
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
    public Profile updatePluginProfile(final BackMeUpUser currentUser, final Profile profile) {
        return this.conn.txNew(new Callable<Profile>() {
            @Override
            public Profile call() {
                // TODO: Refactor (see addPluginProfile method); put validation logic in own method
                // Check if plugin authorization data is required and still valid
                if ((profile.getAuthData() != null) && (profile.getAuthData().getId() != null)) {
                    AuthData authData = BusinessLogicImpl.this.profiles.getAuthData(currentUser, profile.getAuthData().getId());
                    profile.setAuthData(authData);

                    String identification = BusinessLogicImpl.this.plugins.authorizePlugin(profile.getAuthData());
                    profile.getAuthData().setIdentification(identification);
                }

                // Check if plugin validation is required and properties and options are valid
                if (BusinessLogicImpl.this.plugins.requiresValidation(profile.getPluginId())) {
                    ValidationNotes notes = BusinessLogicImpl.this.plugins.validatePlugin(profile.getPluginId(), profile.getProperties(),
                            profile.getOptions());
                    if (!notes.getValidationEntries().isEmpty()) {
                        throw new ValidationException(ValidationExceptionType.ConfigException, notes);
                    }
                }

                return BusinessLogicImpl.this.profiles.updateProfile(currentUser, profile);

            }
        });
    }

    // ========================================================================

    // Profile operations -----------------------------------------------------
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
    public AuthData getPluginAuthData(final BackMeUpUser currentUser, final Long authDataId) {
        return this.conn.txNewReadOnly(new Callable<AuthData>() {
            @Override
            public AuthData call() {

                return BusinessLogicImpl.this.profiles.getAuthData(currentUser, authDataId);

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
    public void deletePluginAuthData(final BackMeUpUser currentUser, final Long authDataId) {
        this.conn.txNew(new Runnable() {
            @Override
            public void run() {

                BusinessLogicImpl.this.profiles.deleteAuthData(currentUser, authDataId);

            }
        });

    }

    // ========================================================================

    // BackupJob operations ---------------------------------------------------

    @Override
    public BackupJob createBackupJob(final BackMeUpUser activeUser, final BackupJob backupJob) {

        BackupJob job = this.conn.txNew(new Callable<BackupJob>() {
            @Override
            public BackupJob call() {

                validateBackupJob(backupJob.getUser(), backupJob);
                return BusinessLogicImpl.this.backupJobs.addBackupJob(backupJob);

            }
        });

        this.jobManager.scheduleBackupJob(activeUser, job);

        return job;
    }

    @Override
    public void startBackupJob(final BackMeUpUser activeUser, final BackupJob backupJob) {
        this.jobManager.executeBackupJob(activeUser, backupJob);

    }

    @Override
    public BackupJob getBackupJob(final Long jobId) {
        return this.conn.txNewReadOnly(new Callable<BackupJob>() {
            @Override
            public BackupJob call() {

                return BusinessLogicImpl.this.backupJobs.getBackupJob(jobId);

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

                BackupJob persistentJob = BusinessLogicImpl.this.backupJobs.getBackupJob(backupJob.getId(), userId);
                BusinessLogicImpl.this.backupJobs.updateBackupJob(persistentJob, backupJob);
                return persistentJob;

            }
        });

        return job;
    }

    @Override
    public List<BackupJob> getBackupJobs(final Long userId) {
        return this.conn.txNewReadOnly(new Callable<List<BackupJob>>() {
            @Override
            public List<BackupJob> call() {

                BusinessLogicImpl.this.registration.getUserByUserId(userId, true);
                return BusinessLogicImpl.this.backupJobs.getBackupJobsOf(userId);

            }
        });
    }

    @Override
    public BackupJobExecution getBackupJobExecution(final Long jobExecId, final Boolean loadProfileDataWithToken) {
        return this.conn.txNew(new Callable<BackupJobExecution>() {
            @Override
            public BackupJobExecution call() {

                return BusinessLogicImpl.this.backupJobs.getBackupJobExecution(jobExecId, loadProfileDataWithToken);

            }
        });
    }

    @Override
    public List<BackupJobExecution> getBackupJobExecutions(final Long jobId) {
        return this.conn.txNewReadOnly(new Callable<List<BackupJobExecution>>() {
            @Override
            public List<BackupJobExecution> call() {

                return BusinessLogicImpl.this.backupJobs.getBackupJobExecutionsOfBackup(jobId);

            }
        });
    }

    @Override
    public BackupJobExecution updateBackupJobExecution(final BackupJobExecution jobExecution) {
        if (jobExecution.getId() == null) {
            throw new IllegalArgumentException("Id must not be null!");
        }

        BackupJobExecution jobExec = this.conn.txNew(new Callable<BackupJobExecution>() {
            @Override
            public BackupJobExecution call() {

                return BusinessLogicImpl.this.backupJobs.updateBackupJobExecution(jobExecution);

            }
        });

        return jobExec;
    }

    @Override
    public void deleteBackupJob(final Long userId, final Long jobId) {
        this.conn.txNew(new Runnable() {
            @Override
            public void run() {

                BusinessLogicImpl.this.registration.getUserByUserId(userId, true);
                BusinessLogicImpl.this.backupJobs.deleteBackupJob(userId, jobId);

            }
        });
    }

    private void validateBackupJob(final BackMeUpUser currentUser, final BackupJob backupJob) {
        this.conn.txJoinReadOnly(new Runnable() {
            @Override
            public void run() {
                ValidationNotes notes = new ValidationNotes();
                try {
                    notes.addAll(validateProfile(currentUser, backupJob.getSourceProfile()));
                    notes.addAll(validateProfile(currentUser, backupJob.getSinkProfile()));

                    for (Profile actionProfile : backupJob.getActionProfiles()) {
                        notes.addAll(validateProfile(currentUser, actionProfile));
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

    // ========================================================================

    // search operations ------------------------------------------------------
    @Override
    public SearchResponse queryBackup(final BackMeUpUser currUser, final String query, final String source, final String type,
            final String job, final String owner, final String tag, final Long offSetStart, final Long maxResults) {
        return this.conn.txNewReadOnly(new Callable<SearchResponse>() {
            @Override
            public SearchResponse call() {
                return BusinessLogicImpl.this.search.runSearch(currUser, query, source, type, job, owner, tag, offSetStart, maxResults);

            }
        });
    }

    // sharing operations======================================================
    @Override
    public Set<SharingPolicyEntry> getAllOwnedSharingPolicies(final BackMeUpUser currUser) {
        return this.conn.txNewReadOnly(new Callable<Set<SharingPolicyEntry>>() {
            @Override
            public Set<SharingPolicyEntry> call() {
                return BusinessLogicImpl.this.share.getAllOwned(currUser);

            }
        });
    }

    @Override
    public Set<SharingPolicyEntry> getAllIncomingSharingPolicies(final BackMeUpUser currUser) {
        return this.conn.txNewReadOnly(new Callable<Set<SharingPolicyEntry>>() {
            @Override
            public Set<SharingPolicyEntry> call() {
                return BusinessLogicImpl.this.share.getAllIncoming(currUser);

            }
        });
    }

    @Override
    public SharingPolicyEntry createAndAddSharingPolicy(final BackMeUpUser currUser, final Long sharingWithUserId,
            final SharingPolicyTypeEntry policy, final String sharedElementID, final String name, final String description,
            final Date lifespanstart, final Date lifespanend) {
        return this.conn.txNew(new Callable<SharingPolicyEntry>() {
            @Override
            public SharingPolicyEntry call() {
                BackMeUpUser sharingWith = BusinessLogicImpl.this.registration.getUserByUserId(sharingWithUserId);
                return BusinessLogicImpl.this.share.add(currUser, sharingWith, policy, sharedElementID, name, description, lifespanstart,
                        lifespanend);
            }
        });
    }

    @Override
    public SharingPolicyEntry updateExistingSharingPolicy(final BackMeUpUser currUser, final Long policyID, final String name,
            final String description, final Date lifespanstart, final Date lifespanend) {
        return this.conn.txNew(new Callable<SharingPolicyEntry>() {
            @Override
            public SharingPolicyEntry call() {
                return BusinessLogicImpl.this.share.updateOwned(currUser, policyID, name, description, lifespanstart, lifespanend);
            }
        });
    }

    @Override
    public String removeOwnedSharingPolicy(final BackMeUpUser currUser, final Long policyID) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {
                return BusinessLogicImpl.this.share.removeOwned(currUser, policyID);

            }
        });
    }

    @Override
    public String removeAllOwnedSharingPolicies(final BackMeUpUser currUser) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {
                return BusinessLogicImpl.this.share.removeAllOwned(currUser);

            }
        });
    }

    @Override
    public String approveIncomingSharing(final BackMeUpUser currUser, final Long policyID) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {
                return BusinessLogicImpl.this.share.acceptIncomingSharing(currUser, policyID);

            }
        });
    }

    @Override
    public String declineIncomingSharing(final BackMeUpUser currUser, final Long policyID) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {
                return BusinessLogicImpl.this.share.declineIncomingSharing(currUser, policyID);

            }
        });
    }

    // heritage sharing operations======================================================
    @Override
    public Set<SharingPolicyEntry> getAllOwnedHeritagePolicies(final BackMeUpUser currUser) {
        return this.conn.txNewReadOnly(new Callable<Set<SharingPolicyEntry>>() {
            @Override
            public Set<SharingPolicyEntry> call() {
                return BusinessLogicImpl.this.heritage.getAllOwned(currUser);

            }
        });
    }

    @Override
    public Set<SharingPolicyEntry> getAllIncomingHeritagePolicies(final BackMeUpUser currUser) {
        return this.conn.txNewReadOnly(new Callable<Set<SharingPolicyEntry>>() {
            @Override
            public Set<SharingPolicyEntry> call() {
                return BusinessLogicImpl.this.heritage.getAllIncoming(currUser);

            }
        });
    }

    @Override
    public SharingPolicyEntry createAndAddHeritagePolicy(final BackMeUpUser currUser, final Long sharingWithUserId,
            final SharingPolicyTypeEntry policy, final String sharedElementID, final String name, final String description,
            final Date lifespanstart, final Date lifespanend) {
        return this.conn.txNew(new Callable<SharingPolicyEntry>() {
            @Override
            public SharingPolicyEntry call() {
                BackMeUpUser sharingWith = BusinessLogicImpl.this.registration.getUserByUserId(sharingWithUserId);
                return BusinessLogicImpl.this.heritage.add(currUser, sharingWith, policy, sharedElementID, name, description,
                        lifespanstart, lifespanend);
            }
        });
    }

    @Override
    public SharingPolicyEntry updateExistingHeritagePolicy(final BackMeUpUser currUser, final Long policyID, final String name,
            final String description, final Date lifespanstart, final Date lifespanend) {
        return this.conn.txNew(new Callable<SharingPolicyEntry>() {
            @Override
            public SharingPolicyEntry call() {
                return BusinessLogicImpl.this.heritage.updateOwned(currUser, policyID, name, description, lifespanstart, lifespanend);
            }
        });
    }

    @Override
    public String removeOwnedHeritagePolicy(final BackMeUpUser currUser, final Long policyID) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {
                return BusinessLogicImpl.this.heritage.removeOwned(currUser, policyID);

            }
        });
    }

    @Override
    public String activateDeadMannSwitchAndImport(final BackMeUpUser currUser) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {
                return BusinessLogicImpl.this.heritage.activateDeadMannSwitchAndImport(currUser);

            }
        });
    }

    // Tagged Collections =====================================================================
    @Override
    public Set<TaggedCollectionEntry> getAllTaggedCollectionsContainingDocuments(final BackMeUpUser currUser,
            final List<UUID> lDocumentUUIDs) {
        return this.conn.txNew(new Callable<Set<TaggedCollectionEntry>>() {
            @Override
            public Set<TaggedCollectionEntry> call() {
                return BusinessLogicImpl.this.taggedCollection.getAllTaggedCollectionsContainingDocuments(currUser, lDocumentUUIDs);
            }
        });
    }

    @Override
    public Set<TaggedCollectionEntry> getAllTaggedCollectionsByNameQuery(final BackMeUpUser currUser, final String name) {
        return this.conn.txNew(new Callable<Set<TaggedCollectionEntry>>() {
            @Override
            public Set<TaggedCollectionEntry> call() {
                return BusinessLogicImpl.this.taggedCollection.getAllTaggedCollectionsByNameQuery(currUser, name);
            }
        });
    }

    @Override
    public Set<TaggedCollectionEntry> getAllTaggedCollections(final BackMeUpUser currUser) {
        return this.conn.txNew(new Callable<Set<TaggedCollectionEntry>>() {
            @Override
            public Set<TaggedCollectionEntry> call() {
                return BusinessLogicImpl.this.taggedCollection.getAllTaggedCollections(currUser);
            }
        });
    }

    @Override
    public String removeTaggedCollection(final BackMeUpUser currUser, final Long collectionID) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {
                return BusinessLogicImpl.this.taggedCollection.removeTaggedCollection(currUser, collectionID);
            }
        });
    }

    @Override
    public String removeAllCollectionsForUser(final BackMeUpUser currUser) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {
                return BusinessLogicImpl.this.taggedCollection.removeAllCollectionsForUser(currUser);
            }
        });
    }

    @Override
    public TaggedCollectionEntry createAndAddTaggedCollection(final BackMeUpUser currUser, final String name, final String description,
            final List<UUID> containedDocumentIDs) {
        return this.conn.txNew(new Callable<TaggedCollectionEntry>() {
            @Override
            public TaggedCollectionEntry call() {
                return BusinessLogicImpl.this.taggedCollection.createAndAddTaggedCollection(currUser, name, description,
                        containedDocumentIDs);
            }
        });
    }

    @Override
    public String addDocumentsToTaggedCollection(final BackMeUpUser currUser, final Long collectionID, final List<UUID> containedDocumentIDs) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {
                return BusinessLogicImpl.this.taggedCollection.addDocumentsToTaggedCollection(currUser, collectionID, containedDocumentIDs);
            }
        });
    }

    @Override
    public String removeDocumentsFromTaggedCollection(final BackMeUpUser currUser, final Long collectionID,
            final List<UUID> containedDocumentIDs) {
        return this.conn.txNew(new Callable<String>() {
            @Override
            public String call() {
                return BusinessLogicImpl.this.taggedCollection.removeDocumentsFromTaggedCollection(currUser, collectionID,
                        containedDocumentIDs);
            }
        });
    }

    // ========================================================================

    // worker operations ------------------------------------------------------

    @Override
    public WorkerConfigDTO initializeWorker(final WorkerInfo workerInfo) {

        return this.conn.txNew(new Callable<WorkerConfigDTO>() {
            @Override
            public WorkerConfigDTO call() {
                return BusinessLogicImpl.this.workers.initializeWorker(workerInfo);
            }
        });

    }
    
    @Override
    public WorkerInfo getWorkerByWorkerId(final String workerId) {
        
        return this.conn.txJoinReadOnly(new Callable<WorkerInfo>() {
            @Override
            public WorkerInfo call() {
                return BusinessLogicImpl.this.workers.getWorkerByWorkerId(workerId);
            }
        });
        
    }

    @Override
    public void addWorkerMetrics(final List<WorkerMetric> workerMetrics) {

        this.conn.txNew(new Runnable() {
            @Override
            public void run() {
                BusinessLogicImpl.this.workers.addWorkerMetrics(workerMetrics);
            }
        });

    }

    // ========================================================================

    // friendlist operations ------------------------------------------------------
    @Override
    public FriendlistUser addFriend(final BackMeUpUser activeUser, final FriendlistUser friend) {
        return this.conn.txNew(new Callable<FriendlistUser>() {
            @Override
            public FriendlistUser call() {
                if (friend.getFriendListType() == FriendListType.HERITAGE) {
                    //when adding a heritage sharing with - always create a new user
                    BackMeUpUser anonymUser = BusinessLogicImpl.this.registration.registerAnonymous(activeUser);
                    friend.setEmail(anonymUser.getEmail());
                    friend.setFriendsBmuUserId(anonymUser.getUserId());
                }
                return BusinessLogicImpl.this.friends.addFriend(activeUser, friend);
            }
        });
    }

    @Override
    public List<FriendlistUser> getFriends(final Long currUserId, final FriendListType friendlist) {
        return this.conn.txNew(new Callable<List<FriendlistUser>>() {
            @Override
            public List<FriendlistUser> call() {
                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(currUserId, true);
                return BusinessLogicImpl.this.friends.getFriends(user, friendlist);
            }
        });
    }

    @Override
    public FriendlistUser updateFriend(final Long currUserId, final FriendlistUser friend) {
        return this.conn.txNew(new Callable<FriendlistUser>() {
            @Override
            public FriendlistUser call() {
                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(currUserId, true);
                return BusinessLogicImpl.this.friends.updateFriend(user, friend);
            }
        });
    }

    @Override
    public void removeFriend(final Long currUserId, final Long friendId, final FriendListType friendlist) {
        this.conn.txNew(new Runnable() {
            @Override
            public void run() {
                BackMeUpUser user = BusinessLogicImpl.this.registration.getUserByUserId(currUserId, true);
                BusinessLogicImpl.this.friends.removeFriend(user, friendId, friendlist);
            }
        });

    }

    // ========================================================================

}
