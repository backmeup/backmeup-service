package org.backmeup.logic.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.BackupJobExecutionDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.keyserver.model.dto.AuthResponseDTO;
import org.backmeup.logic.BackupLogic;
import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJobExecution;
import org.backmeup.model.Profile;
import org.backmeup.model.constants.JobStatus;

@ApplicationScoped
public class BackupLogicImpl implements BackupLogic {

    private static final String JOB_USER_MISSMATCH = "org.backmeup.logic.impl.BusinessLogicImpl.JOB_USER_MISSMATCH";
    private static final String NO_SUCH_JOB = "org.backmeup.logic.impl.BusinessLogicImpl.NO_SUCH_JOB";

    @Inject
    private DataAccessLayer dal;

    @Inject
    private KeyserverClient keyserverClient;

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
        job.setStatus(JobStatus.CREATED);

        // Adding and starting a backup job are be two distinct methods.
        // The following steps are necessary when a job is started and therefore a 
        // jobexecution is created and scheduled. 
        Calendar scheduledExecutionTime = Calendar.getInstance();
        scheduledExecutionTime.setTimeInMillis(job.getStartTime().getTime() + job.getDelay());

//        storePluginConfigOnKeyserver(job);

        // Obtain an access token from the keyserver. 
        // We have to do this now, because the user password is necessary for this step.
        // reusable=true means, that we can get the data for the token + a new token for the next backup       
        //Collect all necessary ids
        List<String> ids = new ArrayList<String>();
        collectIdsForKeyserver(job.getSourceProfile(), ids);
        collectIdsForKeyserver(job.getSinkProfile(), ids);
        for(Profile actionProfile : job.getActionProfiles()) {
            collectIdsForKeyserver(actionProfile, ids);
        }
        AuthResponseDTO response;
        try {
            response = keyserverClient.createOnetime(null, ids.toArray(new String[ids.size()]), scheduledExecutionTime);
            response.getToken().getB64Token();
        } catch (KeyserverException e) {
            
        }


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
    
    @Override
    public BackupJobExecution updateBackupJobExecution(BackupJobExecution jobExec) {
        BackupJobExecutionDao jobExeDao = getBackupJobExecutionDao();
        if(jobExeDao.findById(jobExec.getId()) == null){
            throw new IllegalArgumentException("Unknown BackupJobExecution id");
        }

        return jobExeDao.merge(jobExec);
    }

    // Helper methods ---------------------------------------------------------

//    private void storePluginConfigOnKeyserver(BackupJob job) {
//        // Active user (password is set) is stored in job.getUser()
//        // profile users (job.getXProfile().getUser() password is null!
//        updateProfileOnKeyserver(job.getSourceProfile(), job.getUser().getPassword());
//        updateProfileOnKeyserver(job.getSinkProfile(), job.getUser().getPassword());
//        for (Profile actionProfile : job.getActionProfiles()) {
//            updateProfileOnKeyserver(actionProfile, job.getUser().getPassword());
//        }
//    }

//    private void updateProfileOnKeyserver(Profile profile, String password) {    
//        // Otherwise, we cannot retrieve the token later on.
//        // If no property is available the keyserver throws internally an IndexOutOfBoundsException
//        props.put("dummy", "dummy"); 
//        if (profile.getAuthData() != null && profile.getAuthData().getProperties() != null) {
//            props.putAll(profile.getAuthData().getProperties());
//        }
//        if (profile.getProperties() != null) {
//            props.putAll(profile.getProperties());
//        }
//
//        keyserverClient.addService(profile.getId());
//        keyserverClient.addAuthInfo(profile, password, props);
//        keyserverClient.updatePluginData(null, profile.getPluginId(), data);
//    }
    
    private void collectIdsForKeyserver(Profile profile, List<String> ids) {
        if (profile.getAuthData() != null) {
            ids.add(profile.getPluginId() + ".AUTH");
        }
        if(profile.getProperties() != null) {
            ids.add(profile.getPluginId() + ".PROPS");
        }
        if(profile.getOptions() != null) {
            ids.add(profile.getPluginId() + ".OPTIONS");
        }
    }
}
