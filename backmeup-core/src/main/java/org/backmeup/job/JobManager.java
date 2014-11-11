package org.backmeup.job;

import org.backmeup.model.BackupJob;

/**
 * 
 * The JobManager is the interface to schedule jobs which will then be run 
 * asynchronously by this layer.
 * 
 * A JobManager may start up a framework to run all queued backup jobs.
 * 
 * @author fschoeppl
 */
public interface JobManager {

	public BackupJob createBackupJob(BackupJob backupJob);
	
	public void runBackUpJob(BackupJob job);
	
	public void start();
	public void shutdown();
}
