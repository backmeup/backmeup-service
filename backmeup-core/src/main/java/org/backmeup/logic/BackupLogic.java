package org.backmeup.logic;

import java.util.List;

import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJobExecution;

public interface BackupLogic {

    BackupJob addBackupJob(BackupJob job);

    BackupJob getBackupJob(Long jobId);

    BackupJob getBackupJob(Long jobId, Long userId);

    List<BackupJob> getBackupJobsOf(Long userId);

    void updateBackupJob(BackupJob job, BackupJob updatedJob);

    void deleteBackupJob(Long userId, Long jobId);

    void deleteBackupJobsOf(Long userId);
    

    BackupJobExecution getBackupJobExecution(Long jobExecId, boolean loadProfileDataWithToken);

    List<BackupJobExecution> getBackupJobExecutionsOfBackup(Long jobId);
    
    BackupJobExecution updateBackupJobExecution(BackupJobExecution jobExec);
}
