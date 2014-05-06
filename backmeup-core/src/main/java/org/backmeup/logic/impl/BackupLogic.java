package org.backmeup.logic.impl;

import java.util.List;
import java.util.Map;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Status;
import org.backmeup.model.dto.Job;
import org.backmeup.model.dto.JobUpdateRequest;

public interface BackupLogic {

    void deleteJobsOfUser(String username);

    BackupJob queryExistingJob(Long jobId);

    BackupJob queryExistingUserJob(Long jobId, String username);

    ActionProfile getStoredActionOptions(String actionId, Long jobId);

    void changeActionOptions(String actionId, Long jobId, Map<String, String> actionOptions);

    Job getJobFor(Long jobId);

    void deleteJob(String username, Long jobId);

    List<Status> getStatus(String username, Long jobId);

    List<BackupJob> findByUsername(String username);

    JobUpdateRequest getUpdateRequestFor(Long jobId);

}