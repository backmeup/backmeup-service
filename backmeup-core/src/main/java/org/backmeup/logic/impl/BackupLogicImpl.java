package org.backmeup.logic.impl;

import java.util.List;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotSupportedException;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.BackupJobExecutionDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.keyserver.model.Token.Kind;
import org.backmeup.keyserver.model.dto.TokenDTO;
import org.backmeup.logic.BackupLogic;
import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJobExecution;
import org.backmeup.model.Profile;
import org.backmeup.model.constants.JobStatus;
import org.backmeup.model.exceptions.BackMeUpException;

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
        throw new NotSupportedException();
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
    public BackupJobExecution getBackupJobExecution(Long jobExecId, boolean loadProfileDataWithToken) {
        BackupJobExecution exec = getBackupJobExecutionDao().findById(jobExecId);
        if (exec == null) {
            throw new IllegalArgumentException(String.format("No job execution with id: %d", jobExecId));
        }
        
        if(loadProfileDataWithToken && exec.isTokenRedeemed()) {
            throw new BackMeUpException("Token already redeemed");
        }
        
        if (loadProfileDataWithToken) {
            TokenDTO token = new TokenDTO(Kind.INTERNAL, exec.getToken());
            for (Profile profile : exec.getProfileSet()) {
                loadProfileDataFromKeyserver(token, profile);
            }
            exec.setTokenRedeemed(true);
        }
        
        return exec;
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
    
    private void loadProfileDataFromKeyserver(TokenDTO token, Profile profile) {
        if(profile.getAuthData() != null) {
            try {
                String encodedAuthData = keyserverClient.getPluginData(token, profile.getAuthData().getId().toString());
                profile.getAuthData().setPropertiesFromEncodedString(encodedAuthData);
            } catch (KeyserverException e) {
                // Nothing to do here.
                // Indicates that no profile data is stored on keyserver. 
            } catch (Exception e) {
                throw new BackMeUpException("Cannot load profile auth data", e);
            }
        }
        
        try {
            String encodedSource = keyserverClient.getPluginData(token, profile.getId().toString());
            profile.setPropertiesAndOptionsFromEncodedString(encodedSource);
        } catch (KeyserverException e) {
            // Nothing to do here.
            // Indicates that no profile data is stored on keyserver. 
        } catch (Exception e) {
            throw new BackMeUpException("Cannot load profile data", e);
        }
    }
}
