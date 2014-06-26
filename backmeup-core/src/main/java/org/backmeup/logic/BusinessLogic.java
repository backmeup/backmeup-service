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
import org.backmeup.model.exceptions.AlreadyRegisteredException;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.ValidationException;
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
	
    //profile operation
    void addProfileEntries(Long profileId, Properties entries, String keyRing);

    //validate profile operation
    ValidationNotes validateProfile(String username, Long profileId, String keyRing);

    //metadata operations 
    Properties getMetadata(String username, Long profileId, String keyRing);
	
	//action operations
	List<ActionDescribable> getActions();
	List<String> getActionOptions(String actionId);
	
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
	
	//datasink/-source auth operations
	AuthRequest preAuth(String username, String sourceSinkId, String profileName, String keyRing) throws PluginException, InvalidCredentialsException;
	void postAuth(Long profileId, Properties props, String keyRing) throws PluginException, ValidationException, InvalidCredentialsException;
	
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