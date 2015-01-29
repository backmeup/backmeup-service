package org.backmeup.logic;

import java.util.Date;
import java.util.List;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.dto.JobProtocolDTO;

public interface BackupLogic {

    BackupJob createJob(BackupJob job);
    
    List<BackupJob> getBackupJobsOf(Long userId);
    
    BackupJob getExistingJob(Long jobId);

    BackupJob getExistingUserJob(Long jobId, Long userId);

    void updateJob(BackupJob job, BackupJob updatedJob);
    
    void deleteJob(Long userId, Long jobId);
    
    void deleteJobsOf(Long userId);

    
    void createJobProtocol(BackMeUpUser user, BackupJob job, JobProtocolDTO jobProtocol);
    
    ProtocolOverview getProtocolOverview(BackMeUpUser user, Date from, Date to);

    void deleteProtocolsOf(Long userId);
    
}