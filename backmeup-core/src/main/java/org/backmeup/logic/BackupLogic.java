package org.backmeup.logic;

import java.util.Date;
import java.util.List;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJobExecution;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.dto.JobProtocolDTO;

public interface BackupLogic {

    BackupJob addBackupJob(BackupJob job);
       
    BackupJob getBackupJob(Long jobId);

    BackupJob getBackupJob(Long jobId, Long userId);
    
    List<BackupJob> getBackupJobsOf(Long userId);

    void updateBackupJob(BackupJob job, BackupJob updatedJob);
    
    void deleteBackupJob(Long userId, Long jobId);
    
    void deleteBackupJobsOf(Long userId);
    
    
    List<BackupJobExecution> getBackupJobExecutionsOfBackup (Long jobId);
    
    
    void createJobProtocol(BackMeUpUser user, BackupJob job, JobProtocolDTO jobProtocol);
    
    ProtocolOverview getProtocolOverview(BackMeUpUser user, Date from, Date to);

    void deleteProtocolsOf(Long userId);
    
}