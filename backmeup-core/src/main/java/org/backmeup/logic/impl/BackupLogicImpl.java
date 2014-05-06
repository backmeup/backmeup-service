package org.backmeup.logic.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.StatusDao;
import org.backmeup.logic.impl.helper.BackUpJobConverter;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.ActionProfile.ActionProperty;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Status;
import org.backmeup.model.dto.Job;
import org.backmeup.model.dto.JobUpdateRequest;

@ApplicationScoped
public class BackupLogicImpl implements BackupLogic {

    private static final String JOB_USER_MISSMATCH = "org.backmeup.logic.impl.BusinessLogicImpl.JOB_USER_MISSMATCH";
    private static final String NO_SUCH_JOB = "org.backmeup.logic.impl.BusinessLogicImpl.NO_SUCH_JOB";
    private static final String NO_PROFILE_WITHIN_JOB = "org.backmeup.logic.impl.BusinessLogicImpl.NO_PROFILE_WITHIN_JOB";
    
    @Inject
    private DataAccessLayer dal;

    private final ResourceBundle textBundle = ResourceBundle.getBundle("BackupLogicImpl");
    
    private BackupJobDao getBackupJobDao() {
        return dal.createBackupJobDao();
    }

    private StatusDao getStatusDao() {
        return dal.createStatusDao();
    }

    @Override
    public void deleteJobsOfUser(String username) {
        BackupJobDao jobDao = getBackupJobDao();
        StatusDao statusDao = getStatusDao();
        for (BackupJob job : jobDao.findByUsername(username)) {
            for (Status status : statusDao.findByJobId(job.getId())) {
                statusDao.delete(status);
            }
            jobDao.delete(job);
        }
    }

    @Override
    public BackupJob queryExistingJob(Long jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("JobId must not be null");
        }
        BackupJob job = getBackupJobDao().findById(jobId);
        if (job == null) {
            throw new IllegalArgumentException(String.format(textBundle.getString(NO_SUCH_JOB), jobId));
        }
        return job;
    }

    @Override
    public BackupJob queryExistingUserJob(Long jobId, String username) {
        BackupJob job = queryExistingJob(jobId);
        if (!job.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException(String.format(textBundle.getString(JOB_USER_MISSMATCH),
                    jobId, username));
        }
        return job;
    }

    @Override
    public ActionProfile getStoredActionOptions(String actionId, Long jobId) {
        BackupJob job = queryExistingJob(jobId);
        for (ActionProfile ap : job.getRequiredActions()) {
            if (ap.getActionId().equals(actionId)) {
                return ap;
            }
        }
        throw new IllegalArgumentException(String.format(textBundle.getString(NO_PROFILE_WITHIN_JOB), jobId, actionId));
    }

    @Override
    public void changeActionOptions(String actionId, Long jobId, Map<String, String> actionOptions) {
        BackupJob job = queryExistingJob(jobId);
        for (ActionProfile ap : job.getRequiredActions()) {
            if (ap.getActionId().equals(actionId)) {
                ap.getActionOptions().clear();
                addActionProperties(ap, actionOptions);
            }
        }
    }

    private void addActionProperties(ActionProfile ap, Map<String, String> keyValues) {
        for (Map.Entry<String, String> e : keyValues.entrySet()) {
            ActionProperty aprop = new ActionProperty(e.getKey(), e.getValue());
            aprop.setProfile(ap);
            ap.getActionOptions().add(aprop);
        }
    }

    @Override
    public Job getJobFor(Long jobId) {
        BackupJob job = queryExistingJob(jobId);
        return BackUpJobConverter.convertToJob(job);
    }

    @Override
    public void deleteJob(String username, Long jobId) {
        BackupJob job = queryExistingUserJob(jobId, username);

        deleteStatuses(job.getId());

        getBackupJobDao().delete(job);
    }

    private void deleteStatuses(Long jobId) {
        // Delete Job status records first
        StatusDao statusDao = getStatusDao();
        for (Status status : statusDao.findByJobId(jobId)) {
            statusDao.delete(status);
        }
    }

    @Override
    public List<Status> getStatus(String username, Long jobId) {
        BackupJobDao jobDao = getBackupJobDao();
        
        if (jobId == null) {
            List<Status> status = new ArrayList<>();
            BackupJob job = jobDao.findLastBackupJob(username);
            if (job != null) {
                status.addAll(getStatusForJob(job));
            }
            // for (BackupJob job : jobs) {
            //     status.add(getStatusForJob(job));
            // }
            return status;
        }
        
        BackupJob job = queryExistingUserJob(jobId, username);
        List<Status> status = new ArrayList<>();
        status.addAll(getStatusForJob(job));
        return status;
    }

    private List<Status> getStatusForJob(final BackupJob job) {
        StatusDao sd = dal.createStatusDao();
        List<Status> status = sd.findLastByJob(job.getUser().getUsername(), job.getId());
        return status;
    }

    @Override
    public List<BackupJob> findByUsername(String username) {
        return getBackupJobDao().findByUsername(username);
    }

    @Override
    public JobUpdateRequest getUpdateRequestFor(Long jobId) {
        BackupJob job = queryExistingJob(jobId);
        return BackUpJobConverter.convertToUpdateRequest(job);
    }
    
}
