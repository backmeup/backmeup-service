package org.backmeup.logic;

import java.util.List;
import java.util.Set;

import org.backmeup.index.model.SearchResponse;
import org.backmeup.index.model.sharing.SharingPolicyEntry;
import org.backmeup.index.model.sharing.SharingPolicyEntry.SharingPolicyTypeEntry;
import org.backmeup.model.AuthData;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJobExecution;
import org.backmeup.model.PluginConfigInfo;
import org.backmeup.model.Profile;
import org.backmeup.model.spi.PluginDescribable;

/**
 * The BusinessLogic interface contains all available operations of this project.
 * 
 * It should delegate its operations to other layers so that these can be exchanged more easily.
 * 
 * The org.backmeup.rest project uses this interface to realize its operations.
 * 
 * @author fschoeppl
 */
public interface BusinessLogic {
    // authorization ----------------------------------------------------------
    BackMeUpUser authorize(String username, String password);

    // user operations --------------------------------------------------------
    BackMeUpUser addUser(BackMeUpUser user);
    BackMeUpUser getUserByUsername(String username);
    BackMeUpUser getUserByUserId(Long userId);
    BackMeUpUser updateUser(BackMeUpUser user);
    BackMeUpUser deleteUser(Long userId);

    // plugin operations ------------------------------------------------------
    boolean                 isPluginAvailable(String pluginId);
    List<PluginDescribable> getDatasources();
    List<PluginDescribable> getDatasinks();
    List<PluginDescribable> getActions();
    PluginDescribable       getPluginDescribable(String pluginId);
    PluginConfigInfo        getPluginConfiguration(String pluginId);
    PluginConfigInfo        getPluginConfiguration(String pluginId, AuthData authData);

    // profile operations ------------------------------------------------------
    AuthData       addPluginAuthData(AuthData authData);
    AuthData       getPluginAuthData(Long authDataId);
    List<AuthData> listPluginAuthData(Long userId);
    AuthData       updatePluginAuthData(AuthData authData);
    void           deletePluginAuthData(Long authDataId);

    Profile         addPluginProfile(Profile profile);
    Profile         getPluginProfile(Long profileId);
    Profile         updatePluginProfile(Profile profile);
    void            deleteProfile(Long profileId);

    // backupjob operations ---------------------------------------------------
    BackupJob                createBackupJob(BackupJob backupJob);
    BackupJob                createBackupJob(BackupJob backupJob, boolean startImmediately);
    void                     startBackupJob(BackMeUpUser activeUser, BackupJob backupJob);
    BackupJob                getBackupJob(Long jobId);
    List<BackupJob>          getBackupJobs(Long userId);
    BackupJobExecution       getBackupJobExecution(Long jobExecId);
    List<BackupJobExecution> getBackupJobExecutions(Long jobId);
    BackupJob                updateBackupJob(Long userId, BackupJob backupJob);
    BackupJobExecution       updateBackupJobExecution(BackupJobExecution jobExecution);
    void                     deleteBackupJob(Long userId, Long jobId);
    
    // search operations ------------------------------------------------------
    SearchResponse queryBackup(Long userId, String query, String source, String type, String job, String owner);

    // sharing operations -----------------------------------------------------
    Set<SharingPolicyEntry> getAllOwnedSharingPolicies(Long currUserId);
    Set<SharingPolicyEntry> getAllIncomingSharingPolicies(Long currUserId);
    SharingPolicyEntry      createAndAddSharingPolicy(Long currUserId, Long sharingWithUserId, SharingPolicyTypeEntry policy, String sharedElementID, String name, String description);
    String                  removeOwnedSharingPolicy(Long currUserId, Long policyID);
    String                  removeAllOwnedSharingPolicies(Long currUserId);
    String                  approveIncomingSharing(final Long currUserId, final Long policyID);
    String                  declineIncomingSharing(final Long currUserId, final Long policyID);
}