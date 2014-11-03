package org.backmeup.logic;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.StatusWithFiles;
import org.backmeup.model.dto.JobProtocolDTO;

public interface BackupLogic {

    void deleteJobsOf(Long userId);

    BackupJob getExistingJob(Long jobId);

    BackupJob getExistingUserJob(Long jobId, Long userId);

    Profile getJobActionOption(String actionId, Long jobId);

    void updateJobActionOption(String actionId, Long jobId, Map<String, String> actionOptions);

    BackupJob fullJobFor(Long jobId);

    void deleteJob(Long userId, Long jobId);

    List<StatusWithFiles> getStatus(Long userId, Long jobId);

    List<BackupJob> getBackupJobsOf(Long userId);

    BackupJob updateRequestFor(Long jobId);
    
    BackupJob createJob(BackupJob job);

    void updateJob(BackupJob job, BackupJob updatedJob);

    ProtocolOverview getProtocolOverview(BackMeUpUser user, Date from, Date to);

    void createJobProtocol(BackMeUpUser user, BackupJob job, JobProtocolDTO jobProtocol);

    void deleteProtocolsOf(Long userId);

}