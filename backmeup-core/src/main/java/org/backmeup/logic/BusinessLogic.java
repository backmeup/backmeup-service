package org.backmeup.logic;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.backmeup.index.model.SearchResponse;
import org.backmeup.index.model.sharing.SharingPolicyEntry;
import org.backmeup.index.model.sharing.SharingPolicyEntry.SharingPolicyTypeEntry;
import org.backmeup.index.model.tagging.TaggedCollectionEntry;
import org.backmeup.model.AuthData;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJobExecution;
import org.backmeup.model.FriendlistUser;
import org.backmeup.model.FriendlistUser.FriendListType;
import org.backmeup.model.PluginConfigInfo;
import org.backmeup.model.Profile;
import org.backmeup.model.Token;
import org.backmeup.model.WorkerInfo;
import org.backmeup.model.WorkerMetric;
import org.backmeup.model.dto.WorkerConfigDTO;
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
    Token authorize(String username, String password);

    Token authorize(String activationCode);

    // user operations --------------------------------------------------------
    BackMeUpUser addUser(BackMeUpUser user);

    BackMeUpUser getUserByUsername(String username);

    BackMeUpUser getUserByUserId(Long userId);

    BackMeUpUser getUserByKeyserverUserId(String keyserverUserId);

    BackMeUpUser updateUser(BackMeUpUser user);

    BackMeUpUser deleteUser(BackMeUpUser activeUser, Long userId);

    BackMeUpUser addAnonymousUser(BackMeUpUser activeUser);

    String getAnonymousUserActivationCode(BackMeUpUser currentUser, Long userId);

    // plugin operations ------------------------------------------------------
    boolean isPluginAvailable(String pluginId);

    List<PluginDescribable> getDatasources();

    List<PluginDescribable> getDatasinks();

    List<PluginDescribable> getActions();

    PluginDescribable getPluginDescribable(String pluginId);

    PluginConfigInfo getPluginConfiguration(String pluginId);

    PluginConfigInfo getPluginConfiguration(String pluginId, AuthData authData);

    // profile operations ------------------------------------------------------
    AuthData addPluginAuthData(AuthData authData);

    AuthData getPluginAuthData(BackMeUpUser currentUser, Long authDataId);

    List<AuthData> listPluginAuthData(Long userId);

    AuthData updatePluginAuthData(AuthData authData);

    void deletePluginAuthData(BackMeUpUser currentUser, Long authDataId);

    Profile addPluginProfile(BackMeUpUser currentUser, Profile profile);

    Profile getPluginProfile(BackMeUpUser currentUser, Long profileId);

    Profile updatePluginProfile(BackMeUpUser currentUser, Profile profile);

    void deleteProfile(BackMeUpUser currentUser, Long profileId);

    // backupjob operations ---------------------------------------------------
    BackupJob createBackupJob(BackMeUpUser activeUser, BackupJob backupJob);

    void startBackupJob(BackMeUpUser activeUser, BackupJob backupJob);

    BackupJob getBackupJob(Long jobId);

    List<BackupJob> getBackupJobs(Long userId);

    BackupJobExecution getBackupJobExecution(Long jobExecId, Boolean loadProfileWithToken);

    List<BackupJobExecution> getBackupJobExecutions(Long jobId);

    BackupJob updateBackupJob(Long userId, BackupJob backupJob);

    BackupJobExecution updateBackupJobExecution(BackupJobExecution jobExecution);

    void deleteBackupJob(Long userId, Long jobId);

    // search operations ------------------------------------------------------
    SearchResponse queryBackup(BackMeUpUser currentUser, String query, String source, String type, String job, String owner,
            String taggedCollection, Long offSetStart, Long maxResults);

    // sharing operations -----------------------------------------------------
    Set<SharingPolicyEntry> getAllOwnedSharingPolicies(BackMeUpUser currentUser);

    Set<SharingPolicyEntry> getAllIncomingSharingPolicies(BackMeUpUser currentUser);

    SharingPolicyEntry createAndAddSharingPolicy(BackMeUpUser currentUser, Long sharingWithUserId, SharingPolicyTypeEntry policy,
            String sharedElementID, String name, String description, Date lifespanstart, Date lifespanend);

    SharingPolicyEntry updateExistingSharingPolicy(BackMeUpUser currentUser, Long policyID, String name, String description,
            Date lifespanstart, Date lifespanend);

    String removeOwnedSharingPolicy(BackMeUpUser currentUser, Long policyID);

    String removeAllOwnedSharingPolicies(BackMeUpUser currentUser);

    String approveIncomingSharing(BackMeUpUser currentUser, Long policyID);

    String declineIncomingSharing(BackMeUpUser currentUser, Long policyID);

    // heritage sharing operations -----------------------------------------------------
    Set<SharingPolicyEntry> getAllOwnedHeritagePolicies(BackMeUpUser currentUser);

    Set<SharingPolicyEntry> getAllIncomingHeritagePolicies(BackMeUpUser currentUser);

    SharingPolicyEntry createAndAddHeritagePolicy(BackMeUpUser currentUser, Long sharingWithUserId, SharingPolicyTypeEntry policy,
            String sharedElementID, String name, String description, Date lifespanstart, Date lifespanend);

    SharingPolicyEntry updateExistingHeritagePolicy(BackMeUpUser currentUser, Long policyID, String name, String description,
            Date lifespanstart, Date lifespanend);

    String removeOwnedHeritagePolicy(BackMeUpUser currentUser, Long policyID);

    String activateDeadMannSwitchAndImport(BackMeUpUser currentUser);

    // tagged collections -----------------------------------------------------
    Set<TaggedCollectionEntry> getAllTaggedCollectionsContainingDocuments(BackMeUpUser currentUser, List<UUID> lDocumentUUIDs);

    Set<TaggedCollectionEntry> getAllTaggedCollectionsByNameQuery(BackMeUpUser currentUser, String name);

    Set<TaggedCollectionEntry> getAllTaggedCollections(BackMeUpUser currentUser);

    String removeTaggedCollection(BackMeUpUser currentUser, Long collectionID);

    String removeAllCollectionsForUser(BackMeUpUser currentUser);

    TaggedCollectionEntry createAndAddTaggedCollection(BackMeUpUser currentUser, String name, String description,
            List<UUID> containedDocumentIDs);

    String addDocumentsToTaggedCollection(BackMeUpUser currentUser, Long collectionID, List<UUID> containedDocumentIDs);

    String removeDocumentsFromTaggedCollection(BackMeUpUser currentUser, Long collectionID, List<UUID> containedDocumentIDs);

    // worker operations ------------------------------------------------------
    WorkerConfigDTO initializeWorker(WorkerInfo workerInfo);

    void addWorkerMetrics(List<WorkerMetric> workerMetrics);

    // friendlist operations --------------------------------------------------------
    FriendlistUser addFriend(BackMeUpUser currentUser, FriendlistUser friend);

    List<FriendlistUser> getFriends(Long currUserId, FriendListType friendlist);

    FriendlistUser updateFriend(Long currUserId, FriendlistUser friend);

    void removeFriend(Long currUserId, Long friendId, FriendListType friendlist);

}
