package org.backmeup.logic;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.Status;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.dto.JobProtocolDTO;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;

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
	// user operations --------------------------------------------------------
	BackMeUpUser getUserByUsername(String username);
	BackMeUpUser getUserByUserId(String userId);
	BackMeUpUser deleteUser(String userId);
	BackMeUpUser updateUser(BackMeUpUser user);
	BackMeUpUser addUser(BackMeUpUser user);
	
	// plugin operations ------------------------------------------------------
	SourceSinkDescribable getPluginDescribable(String pluginId);
	AuthRequest getPluginConfiguration(String pluginId);
	Profile addPluginProfile(String pluginId, Profile profile, Properties props, List<String> options);
	void updatePluginProfile(String pluginId, Profile profile, Properties props, List<String> options);

	// action operations
	void changeActionOptions(String actionId, Long jobId, Map<String, String> actionOptions);
	ActionProfile getStoredActionOptions(String actionId, Long jobId);
	
	//datasource operations
	List<SourceSinkDescribable> getDatasources();
	List<Profile> getDatasourceProfiles(String username);
	Profile deleteProfile(String username, Long profile);
	List<String> getDatasourceOptions(String username, Long profileId, String keyRingPassword);
	List<String> getStoredDatasourceOptions(String username, Long profileId, Long jobId);
	void changeProfile(Long profileId, Long jobId, List<String> sourceOptions);
	
	//datasink operations
	List<SourceSinkDescribable> getDatasinks();
	List<Profile> getDatasinkProfiles(String username);

	//action operations
	List<ActionDescribable> getActions();
	List<String> getActionOptions(String actionId);
	
	// Profile/Auth operations
//	void addProfileEntries(Long profileId, Properties entries, String keyRing);
	ValidationNotes validateProfile(String username, Long profileId, String keyRing);
//	AuthRequest preAuth(String username, String sourceSinkId, String profileName, String keyRing) throws PluginException, InvalidCredentialsException;
//	void postAuth(Long profileId, Properties props, String keyRing) throws PluginException, ValidationException, InvalidCredentialsException;
	
	//metadata operations 
//    Properties getMetadata(String username, Long profileId, String keyRing);
		
	
	// backupjob operations ---------------------------------------------------
	
	//job & validation operations
	ValidationNotes validateBackupJob(String username, Long jobId, String keyRing);
	ValidationNotes updateBackupJob(String username, BackupJob updateRequest);
	BackupJob getBackupJob(String username, Long jobId);
	// Should replace method 'getBackupJob' ?
	BackupJob getBackupJobFull(String username, Long jobId);
//	Job updateBackupJobFull(String username, Job backupJob);  
	ValidationNotes createBackupJob(String username, BackupJob request);
	List<BackupJob> getJobs(String username);
	void deleteJob(String username, Long jobId);
	List<Status> getStatus(String username, Long jobId);
	ProtocolDetails getProtocolDetails(String username, String fileId);
	ProtocolOverview getProtocolOverview(String username, String duration);
	void updateJobProtocol(String username, Long jobId, JobProtocolDTO jobProtocol);
	void deleteJobProtocols(String username);
	
	// search operations ------------------------------------------------------
	long searchBackup(String username, String keyRingPassword, String query);
	//SearchResponse queryBackup(String username, long searchId, String filterType, String filterValue);
	SearchResponse queryBackup(String username, long searchId, Map<String, List<String>> filters);
	File getThumbnail(String username, String fileId);
	void deleteIndexForUser(String username);
	void deleteIndexForJobAndTimestamp(Long jobId, Long timestamp);
	
	// log operations ---------------------------------------------------------
	List<KeyserverLog> getKeysrvLogs (BackMeUpUser user);
	
	// lifecycle operations ---------------------------------------------------
	void shutdown();
}