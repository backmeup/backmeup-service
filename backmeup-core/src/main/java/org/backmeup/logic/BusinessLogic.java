package org.backmeup.logic;

import java.util.List;

import org.backmeup.index.model.SearchResponse;
import org.backmeup.model.AuthData;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.PluginConfigInfo;
import org.backmeup.model.Profile;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.dto.JobProtocolDTO;
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
    ValidationNotes validateProfile(Long userId, Long profileId, String keyRing);
    void            deleteProfile(Long profileId);

    // backupjob operations ---------------------------------------------------
    BackupJob       createBackupJob(BackupJob backupJob);
    List<BackupJob> getJobs(Long userId);
    BackupJob       getBackupJobFull(Long jobId);
    void            validateBackupJob(BackupJob backupJob);
    BackupJob       updateBackupJob(Long userId, BackupJob backupJob);
    void            deleteJob(Long userId, Long jobId);

    ProtocolDetails  getProtocolDetails(Long userId, String fileId);
    ProtocolOverview getProtocolOverview(Long userId, String duration);
    void             updateJobProtocol(Long userId, Long jobId, JobProtocolDTO jobProtocol);
    void             deleteJobProtocols(Long userId);

    // search operations ------------------------------------------------------
    SearchResponse queryBackup(Long userId, String query, String source, String type, String job);
}