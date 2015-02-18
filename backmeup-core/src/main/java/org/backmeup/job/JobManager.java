package org.backmeup.job;

import org.backmeup.model.BackupJob;

/**
 * The JobManager is the interface to schedule jobs which will then be run
 * asynchronously by distributing them to workers.
 * 
 * A JobManager may start up a framework to run all queued backup jobs.
 * 
 * @author fschoeppl
 */
public interface JobManager {

//    BackupJob createBackupJob(BackupJob backupJob);

    void runBackUpJob(BackupJob job);

}
