package org.backmeup.job;

import java.util.Date;
import java.util.List;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;

/**
 * 
 * The JobManager is the interface to 
 * create new jobs which will then be 
 * run asynchronously by this layer.
 * 
 * A JobManager may start up a framework
 * to run all queued backup jobs.
 * 
 * @author fschoeppl
 */
public interface JobManager {

	@Deprecated public BackupJob createBackupJob(BackMeUpUser user,
			Profile sourceProfile, Profile sinkProfile,
			List<Profile> requiredActions, Date start, long delay, String jobTitle,
			boolean reschedule, String timeExpression);
	public BackupJob createBackupJob(BackupJob backupJob);

	
	public void runBackUpJob(BackupJob job);
	
	public void start();
	public void shutdown();
}
