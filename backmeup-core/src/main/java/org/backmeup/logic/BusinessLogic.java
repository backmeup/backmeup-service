package org.backmeup.logic;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.index.model.SearchResponse;
import org.backmeup.model.AuthData;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.StatusWithFiles;
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
	BackMeUpUser getUserByUsername(String username);
	BackMeUpUser getUserByUserId(Long userId);
	BackMeUpUser deleteUser(Long userId);
	BackMeUpUser updateUser(BackMeUpUser user);
	BackMeUpUser addUser(BackMeUpUser user);
	
	// plugin operations ------------------------------------------------------
	boolean isPluginAvailable(String pluginId);
	PluginDescribable getPluginDescribable(String pluginId);
	AuthRequest getPluginConfiguration(String pluginId);
	
	AuthData addPluginAuthData(AuthData authData);
	AuthData getPluginAuthData(Long authDataId);
	List<AuthData> listPluginAuthData(Long userId);
	AuthData updatePluginAuthData(AuthData authData);
	void     deletePluginAuthData(Long authDataId);
	
	Profile addPluginProfile(String pluginId, Profile profile);
	@Deprecated Profile addPluginProfile(String pluginId, Profile profile, Properties props, List<String> options);
	Profile updatePluginProfile(String pluginId, Profile profile);
	@Deprecated void updatePluginProfile(String pluginId, Profile profile, Properties props, List<String> options);
	Profile getPluginProfile(Long profileId);
	void deleteProfile(Long profileId);
	@Deprecated Profile deleteProfile(Long userId, Long profile);

	// action operations
	void changeActionOptions(String actionId, Long jobId, Map<String, String> actionOptions);
	Profile getStoredActionOptions(String actionId, Long jobId);
	
	//datasource operations
	List<PluginDescribable> getDatasources();
	List<Profile> getDatasourceProfiles(Long userId);
	List<String> getDatasourceOptions(Long userId, Long profileId, String keyRingPassword);
	List<String> getStoredDatasourceOptions(Long userId, Long profileId, Long jobId);
	void changeProfile(Long profileId, Long jobId, List<String> sourceOptions);
	
	//datasink operations
	List<PluginDescribable> getDatasinks();
	List<Profile> getDatasinkProfiles(Long userId);

	//action operations
	List<PluginDescribable> getActions();
	List<String> getActionOptions(String actionId);
	
	// Profile/Auth operations
//	void addProfileEntries(Long profileId, Properties entries, String keyRing);
	ValidationNotes validateProfile(Long userId, Long profileId, String keyRing);
//	AuthRequest preAuth(String username, String sourceSinkId, String profileName, String keyRing) throws PluginException, InvalidCredentialsException;
//	void postAuth(Long profileId, Properties props, String keyRing) throws PluginException, ValidationException, InvalidCredentialsException;
	
	//metadata operations 
//    Properties getMetadata(String username, Long profileId, String keyRing);
		
	
	// backupjob operations ---------------------------------------------------
	
	//job & validation operations
	ValidationNotes validateBackupJob(Long userId, Long jobId, String keyRing);
	BackupJob updateBackupJob(Long userId, BackupJob backupJob);
	BackupJob getBackupJob(Long jobId);
	// Should replace method 'getBackupJob' ?
	BackupJob getBackupJobFull(Long jobId);
//	Job updateBackupJobFull(String username, Job backupJob);  
	ValidationNotes createBackupJob(BackupJob request);
	List<BackupJob> getJobs(Long userId);
	void deleteJob(Long userId, Long jobId);
	
	List<StatusWithFiles> getStatus(Long userId, Long jobId);
	
	ProtocolDetails getProtocolDetails(Long userId, String fileId);
	ProtocolOverview getProtocolOverview(Long userId, String duration);
	void updateJobProtocol(Long userId, Long jobId, JobProtocolDTO jobProtocol);
	void deleteJobProtocols(Long userId);
	
	// search operations ------------------------------------------------------
	SearchResponse queryBackup(Long userId, String query, Map<String, List<String>> filters);
	File getThumbnail(Long userId, String fileId);
	void deleteIndexForUser(Long userId);
	void deleteIndexForJobAndTimestamp(Long userId, Long jobId, Long timestamp);
	
	// log operations ---------------------------------------------------------
	List<KeyserverLog> getKeysrvLogs (BackMeUpUser user);
	
	// lifecycle operations ---------------------------------------------------
	void shutdown();
}