package org.backmeup.logic.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.Status;
import org.backmeup.model.dto.Job;
import org.backmeup.model.dto.JobProtocolDTO;
import org.backmeup.model.dto.JobUpdateRequest;

public interface BackupLogic {

    void deleteJobsOf(String username);

    BackupJob getExistingJob(Long jobId);

    BackupJob getExistingUserJob(Long jobId, String username);

    ActionProfile getJobActionOption(String actionId, Long jobId);

    void updateJobActionOption(String actionId, Long jobId, Map<String, String> actionOptions);

    Job fullJobFor(Long jobId);

    void deleteJob(String username, Long jobId);

    List<Status> getStatus(String username, Long jobId);

    List<BackupJob> getBackupJobsOf(String username);

    JobUpdateRequest updateRequestFor(Long jobId);

    void updatelJob(BackupJob job, List<ActionProfile> requiredActions, Set<ProfileOptions> sourceProfiles, Profile sindProfile,
            JobUpdateRequest updateRequest);

    ProtocolOverview getProtocolOverview(BackMeUpUser user, Date from, Date to);

    void createJobProtocol(BackMeUpUser user, BackupJob job, JobProtocolDTO jobProtocol);

    void deleteProtocolsOf(String username);

}