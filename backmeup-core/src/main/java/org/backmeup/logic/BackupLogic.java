package org.backmeup.logic;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.Status;
import org.backmeup.model.dto.JobProtocolDTO;

public interface BackupLogic {

    void deleteJobsOf(String username);

    BackupJob getExistingJob(Long jobId);

    BackupJob getExistingUserJob(Long jobId, String username);

    ActionProfile getJobActionOption(String actionId, Long jobId);

    void updateJobActionOption(String actionId, Long jobId, Map<String, String> actionOptions);

    BackupJob fullJobFor(Long jobId);

    void deleteJob(String username, Long jobId);

    List<Status> getStatus(String username, Long jobId);

    List<BackupJob> getBackupJobsOf(String username);

    BackupJob updateRequestFor(Long jobId);

    void updateJob(BackupJob job, BackupJob updatedJob);

    ProtocolOverview getProtocolOverview(BackMeUpUser user, Date from, Date to);

    void createJobProtocol(BackMeUpUser user, BackupJob job, JobProtocolDTO jobProtocol);

    void deleteProtocolsOf(String username);

}