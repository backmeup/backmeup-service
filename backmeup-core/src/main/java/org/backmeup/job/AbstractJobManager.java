package org.backmeup.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.keyserver.model.Token.Kind;
import org.backmeup.keyserver.model.dto.AuthResponseDTO;
import org.backmeup.keyserver.model.dto.TokenDTO;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJobExecution;
import org.backmeup.model.Profile;
import org.backmeup.model.constants.JobFrequency;
import org.backmeup.model.constants.JobStatus;
import org.backmeup.model.exceptions.BackMeUpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorSystem;
import akka.util.Duration;

/**
 * An abstract {@link JobManager} implementation that supports scheduled execution 
 * backed by the Akka actor framework.
 * 
 * Subclasses of this class need to define what should happen when the job is
 * triggered by implementing the 'newJobRunner' method.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public abstract class AbstractJobManager implements JobManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJobManager.class);
    
    private static final ActorSystem SYSTEM = ActorSystem.create();
    
    private static final long INITIAL_DELAY = 2000;
    
    @Inject
    protected Connection conn;

    @Inject
    protected DataAccessLayer dal;

    @Inject
    private KeyserverClient keyserverClient;
    
    private boolean shutdownInProgress = false;
    
    // DAOs -------------------------------------------------------------------

    private BackupJobDao getBackupJobDao() {
        return dal.createBackupJobDao();
    }

    private BackupJob getBackUpJob(Long jobId) {
        return getBackupJobDao().findById(jobId);
    }
    
    // Lifecycle methods ------------------------------------------------------

    @PostConstruct
    public void start() {
        
    }
    
    @PreDestroy
    public void shutdown() {
        shutdownInProgress = true;
        // Shutdown system component
        SYSTEM.shutdown();
        SYSTEM.awaitTermination();
    }
    
    // ========================================================================
    
    @Override
    public void scheduleBackupJob(BackMeUpUser activeUser, BackupJob job) {
        LOGGER.debug(String.format("Schedule job with id=%d", job.getId()));
        queueJob(activeUser, job);
    }
    
    @Override
    public void executeBackupJob(BackMeUpUser activeUser, BackupJob job) {
        LOGGER.debug(String.format("Execute job with id=%d", job.getId()));
        executeJob(activeUser, job);
        
    }

    protected abstract void runJob(BackupJobExecution job);
 
    // Don't call this method within a database transaction!
    private void queueJob(BackMeUpUser activeUser, BackupJob job) {
        try {
            // Calculate and set next execution time
            Calendar scheduledExecutionTime = Calendar.getInstance();
            scheduledExecutionTime.setTimeInMillis(job.getStartTime().getTime());
            long executeIn = calcNextExecutionTime(job);
            
            // Obtain an access token from the keyserver.
            List<String> ids = getKeyserverIdsForJob(job);
            TokenDTO token = new TokenDTO(Kind.INTERNAL, activeUser.getPassword());
            AuthResponseDTO response = keyserverClient.createOnetime(token, ids.toArray(new String[ids.size()]), scheduledExecutionTime);
            
            // Add the scheduler id to the job. If the job gets executed it will
            // be possible to check if this job is still valid
            conn.begin();
            BackupJob backupJob = getBackUpJob(job.getId());
            backupJob.setStatus(JobStatus.ACTIVE);
            backupJob.setValidScheduleID(UUID.randomUUID());
            backupJob.setNextExecutionTime(scheduledExecutionTime.getTime());
            backupJob.setToken(response.getToken().getB64Token());
            conn.commit();

            // We can use the 'cancellable' to terminate later on
            SYSTEM.scheduler().scheduleOnce(
                    Duration.create(executeIn, TimeUnit.MILLISECONDS),
                    new JobExecutor(backupJob.getId(), dal, backupJob.getValidScheduleID()));

        } catch (Exception e) {
            throw new BackMeUpException("Cannot schedule backupjob", e);
        } finally {
            conn.rollback();
        }
    }
    
    private void executeJob(BackMeUpUser activeUser, BackupJob job) {
        try {
            conn.begin();
            job = getBackUpJob(job.getId());

            if (job == null) {
                throw new BackMeUpException("BackupJob is not available");
            }
            
            if (!job.isActive()) {
                throw new BackMeUpException("BackupJob is not active");
            }

            try {
                Calendar currentTime = Calendar.getInstance();
                List<String> ids = getKeyserverIdsForJob(job);
                TokenDTO token = new TokenDTO(Kind.INTERNAL, activeUser.getPassword());
                AuthResponseDTO response = keyserverClient.createOnetime(token, ids.toArray(new String[ids.size()]), currentTime);
                
                // Run the job by creating a JobExecution
                BackupJobExecution jobExecution = new BackupJobExecution(job);
                jobExecution.setToken(response.getToken().getB64Token());
                jobExecution = dal.createBackupJobExecutionDao().save(jobExecution);
                job.getJobExecutions().add(jobExecution);

                runJob(jobExecution);
            } catch (KeyserverException e) {
               throw new BackMeUpException("Cannot create backup token", e);
            }

            conn.commit();
        } finally {
            conn.rollback();
        }
    }
    
    // Helper methods ---------------------------------------------------------
    
    private long calcNextExecutionTime(BackupJob job) {
     // Compute next job execution time
        long currentTime = new Date().getTime();
        long executeIn = job.getStartTime().getTime() - currentTime;

        // If job execution was scheduled for within the past 5 mins, 
        // still schedule now. We start with a small delay of 2 secs. 
        if (executeIn >= -300000 && executeIn < 0) {
            executeIn = INITIAL_DELAY;
        }
        
        return executeIn;
    }
    
    private List<String> getKeyserverIdsForJob(BackupJob job) {
        List<String> ids = new ArrayList<String>();
        for(Profile profile : job.getProfileSet()) {
            collectIdsForKeyserver(profile, ids);
        }
        return ids;
    }
    
    private void collectIdsForKeyserver(Profile profile, List<String> ids) {
        if (profile.getAuthData() != null) {
            ids.add(profile.getAuthData().getId().toString());
        }
        ids.add(profile.getId().toString());
    }

    private class JobExecutor implements Runnable {
        private final Logger LOGGER = LoggerFactory.getLogger(JobExecutor.class);

        private final Long jobId;
        private final DataAccessLayer dal;
        private final UUID schedulerID;

        JobExecutor(Long jobId, DataAccessLayer dal, UUID schedulerID) {
            this.jobId = jobId;
            this.dal = dal;
            this.schedulerID = schedulerID;
        }

        @Override
        public void run() {
            try {
                if (shutdownInProgress) {
                    return;
                }

                conn.begin();
                BackupJob job = dal.createBackupJobDao().findById(jobId);

                // Check if job still exists. Could be deleted in the meantime. 
                if(job == null) {
                    return;
                }

                // check if the scheduler is still valid. If not a new scheduler
                // was created and this one should not be executed
                if (job.getValidScheduleID().compareTo(schedulerID) != 0) {
                    return;
                }

                // Run the job by creating a JobExecution
                if (job.isActive()) {
                    TokenDTO token = new TokenDTO(Kind.ONETIME,job.getToken());
                    AuthResponseDTO response;
                    if(job.getJobFrequency() != JobFrequency.ONCE) {
                        Calendar nextExecutionTime = Calendar.getInstance();
                        nextExecutionTime.setTimeInMillis(new Date().getTime() + job.getDelay());

                        // Obtain new access token from the keyserver
                        response =  keyserverClient.authenticateWithOnetime(token, nextExecutionTime);
                        job.setToken(response.getNext().getToken().getB64Token());
                        job.setNextExecutionTime(nextExecutionTime.getTime());
                    } else {
                        response =  keyserverClient.authenticateWithOnetime(token);
                    }

                    BackupJobExecution jobExecution = new BackupJobExecution(job);
                    jobExecution.setToken(response.getToken().getB64Token());
                    jobExecution = dal.createBackupJobExecutionDao().save(jobExecution);
                    job.getJobExecutions().add(jobExecution);

                    runJob(jobExecution);
                }

                if(job.getJobFrequency() != JobFrequency.ONCE) {
                    LOGGER.debug(String.format("Rescheduling job: execute in %d ms", job.getDelay()));

                    SYSTEM.scheduler().scheduleOnce(
                            Duration.create(job.getDelay(),TimeUnit.MILLISECONDS),
                            new JobExecutor(job.getId(), dal,schedulerID));
                }
                conn.commit();
            } catch (Exception e) {
                throw new BackMeUpException("Cannot run backupjob", e);
            } finally {
                conn.rollback();
            }
        }
    }
}
