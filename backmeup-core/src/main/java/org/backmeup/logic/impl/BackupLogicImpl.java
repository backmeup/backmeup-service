package org.backmeup.logic.impl;

import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.BackupJobExecutionDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.logic.BackupLogic;
import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJobExecution;
import org.backmeup.model.Profile;
import org.backmeup.model.Token;
import org.backmeup.model.constants.BackupJobStatus;

@ApplicationScoped
public class BackupLogicImpl implements BackupLogic {

    private static final String JOB_USER_MISSMATCH = "org.backmeup.logic.impl.BusinessLogicImpl.JOB_USER_MISSMATCH";
    private static final String NO_SUCH_JOB = "org.backmeup.logic.impl.BusinessLogicImpl.NO_SUCH_JOB";

    @Inject
    private DataAccessLayer dal;

    @Inject
    private Keyserver keyserverClient;

    private final ResourceBundle textBundle = ResourceBundle.getBundle("BackupLogicImpl");

    private BackupJobDao getBackupJobDao() {
        return dal.createBackupJobDao();
    }
    
    private BackupJobExecutionDao getBackupJobExecutionDao() {
        return dal.createBackupJobExecutionDao();
    }

    // BackupLogic methods ----------------------------------------------------

    @Override
    public BackupJob addBackupJob(BackupJob job) {
        job.setStatus(BackupJobStatus.queued);

        // TODO SP: adding and starting a backup job should be two distinct methods.
        // The following steps are necessary when a job is started and therefore a 
        // jobexecution is created and scheduled. 
        Long firstExecutionDate = job.getStartTime().getTime() + job.getDelay();

        storePluginConfigOnKeyserver(job);

        // Obtain an access token from the keyserver. We have to do this, because 
        // the user password is necessary for this step.
        // reusable=true means, that we can get the data for the token + a new token for the next backup
        Token t = keyserverClient.getToken(job, job.getUser().getPassword(), firstExecutionDate, true, null);
        job.setToken(t);

        return getBackupJobDao().save(job);
    }
    
    @Override
    public List<BackupJob> getBackupJobsOf(Long userId) {
        return getBackupJobDao().findByUserId(userId);
    }

    @Override
    public BackupJob getBackupJob(Long jobId) {
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
    public BackupJob getBackupJob(Long jobId, Long userId) {
        BackupJob job = getBackupJob(jobId);
        if (!job.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException(String.format(textBundle.getString(JOB_USER_MISSMATCH),
                    jobId, userId));
        }
        return job;
    }

    @Override
    public void updateBackupJob(BackupJob persistentJob, BackupJob updatedJob) {
        persistentJob.getToken().setTokenId(updatedJob.getToken().getTokenId());
        persistentJob.getToken().setToken(updatedJob.getToken().getToken());
        persistentJob.getToken().setBackupdate(updatedJob.getToken().getBackupdate());

        persistentJob.setStatus(updatedJob.getStatus());

        // TODO: update fields
    }

    @Override
    public void deleteBackupJob(Long userId, Long jobId) {
        BackupJob job = getBackupJob(jobId, userId);

        getBackupJobDao().delete(job);
    }

    @Override
    public void deleteBackupJobsOf(Long userId) {
        BackupJobDao jobDao = getBackupJobDao();
        for (BackupJob job : jobDao.findByUserId(userId)) {
            jobDao.delete(job);
        }
    }
    
    @Override
    public BackupJobExecution getBackupJobExecution(Long jobExecId) {
        return getBackupJobExecutionDao().findById(jobExecId);
    }
    
    @Override
    public List<BackupJobExecution> getBackupJobExecutionsOfBackup(Long jobId) {
        return getBackupJobExecutionDao().findByBackupJobId(jobId);
    }

    // Helper methods ---------------------------------------------------------

    private void storePluginConfigOnKeyserver(BackupJob job) {
        // Active user (password is set) is stored in job.getUser()
        // profile users (job.getXProfile().getUser() password is null!
        updateProfileOnKeyserver(job.getSourceProfile(), job.getUser().getPassword());
        updateProfileOnKeyserver(job.getSinkProfile(), job.getUser().getPassword());
        for (Profile actionProfile : job.getActionProfiles()) {
            updateProfileOnKeyserver(actionProfile, job.getUser().getPassword());
        }
    }

    private void updateProfileOnKeyserver(Profile profile, String password) {    
        if(keyserverClient.isServiceRegistered(profile.getId())){
            keyserverClient.deleteService(profile.getId());
        }

        if (keyserverClient.isAuthInformationAvailable(profile, password)) {
            keyserverClient.deleteAuthInfo(profile.getId());
        }

        // For now, store auth data and props together
        Properties props = new Properties();
        // Otherwise, we cannot retrieve the token later on.
        // If no property is available the keyserver throws internally an IndexOutOfBoundsException
        props.put("dummy", "dummy"); 
        if (profile.getAuthData() != null && profile.getAuthData().getProperties() != null) {
            props.putAll(profile.getAuthData().getProperties());
        }
        if (profile.getProperties() != null) {
            props.putAll(profile.getProperties());
        }

        keyserverClient.addService(profile.getId());
        keyserverClient.addAuthInfo(profile, password, props);
    }
}
