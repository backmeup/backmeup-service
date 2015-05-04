package org.backmeup.job;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJobExecution;
import org.backmeup.model.Token;
import org.backmeup.model.constants.JobStatus;
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

    private static final ActorSystem SYSTEM = ActorSystem.create();

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJobManager.class);
    
    @Inject
    protected Connection conn;

    @Inject
    protected DataAccessLayer dal;

    @Inject
    private Keyserver keyserver;
    
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
//        List<BackupJob> jobs = conn.txNewReadOnly(new Callable<List<BackupJob>>() {
//            @Override
//            public List<BackupJob> call() {
//                return getBackupJobDao().findAll();
//            }
//        });
//
//        for (BackupJob storedJob : jobs) {
//            queueJob(storedJob);
//        }
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
    public void runBackupJob(BackupJob job) {
        queueJob(job);
    }

    protected abstract void runJob(BackupJobExecution job);
 
    // Don't call this method within a database transaction!
    private void queueJob(BackupJob job) {
        try {
            // Compute next job execution time
            long currentTime = new Date().getTime();
            long executeIn = calcNextExecutionTime(job);

            // If we missed the last execution slot, schedule on 
            // the next occasion defined by getStart() and DELAY
            if (executeIn < 0) {
                // delay represents the jobfrequency (daily, weekly, ...) in milliseconds
                long delay = job.getDelay();
                executeIn += Math.ceil((double) Math.abs(executeIn) / (double) delay) * delay;
                conn.begin();
                job = getBackUpJob(job.getId());
                
                // Update these jobs' access tokens
                job.getToken().setBackupdate(currentTime + executeIn);
                AuthDataResult authenticationData = keyserver.getData(job.getToken());

                // the token for the next getData call
                Token newToken = authenticationData.getNewToken();
                job.setToken(newToken);
                conn.commit();
            }

            // Add the scheduler id to the job. If the job gets executed it will
            // be possible to check if this job is still valid
            conn.begin();
            job = getBackUpJob(job.getId());
            job.setStatus(JobStatus.ACTIVE);
            UUID schedulerID = UUID.randomUUID();
            job.setValidScheduleID(schedulerID);
            conn.commit();

            // We can use the 'cancellable' to terminate later on
            SYSTEM.scheduler().scheduleOnce(
                    Duration.create(executeIn, TimeUnit.MILLISECONDS),
                    new RunAndReschedule(job.getId(), dal, schedulerID));

        } catch (Exception e) {
            // TODO there must be error handling defined in the JobManager!^
            LOGGER.error("Error during startup", e);
            // throw new BackMeUpException(e);
        } finally {
            conn.rollback();
        }
    }
    
    private long calcNextExecutionTime(BackupJob job) {
     // Compute next job execution time
        long currentTime = new Date().getTime();
        long executeIn = job.getNextExecutionTime().getTime() - currentTime;

        // If job execution was scheduled for within the past 5 mins, 
        // still schedule now. We start with a small delay of 2 secs. 
        if (executeIn >= -300000 && executeIn < 0) {
            // executeIn = 0;
            executeIn = 2000;
        }
        
        return executeIn;
    }

    private class RunAndReschedule implements Runnable {
        private final Logger LOGGER = LoggerFactory.getLogger(RunAndReschedule.class);

        private final Long jobId;
        private final DataAccessLayer dal;
        private final UUID schedulerID;

        RunAndReschedule(Long jobId, DataAccessLayer dal, UUID schedulerID) {
            this.jobId = jobId;
            this.dal = dal;
            this.schedulerID = schedulerID;
        }

        @Override
        public void run() {
            if (!shutdownInProgress) {
                try {
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
                        BackupJobExecution jobExecution = new BackupJobExecution(job);
                        jobExecution = dal.createBackupJobExecutionDao().save(jobExecution);
                        job.getJobExecutions().add(jobExecution);
                        
                        runJob(jobExecution);
                    }

                    if(!job.getTimeExpression().equals("realtime")) {
                        LOGGER.debug(String.format("Rescheduling job: execute in %d ms", job.getDelay()));
                        Date execTime = new Date(new Date().getTime() + job.getDelay());
                        job.setNextExecutionTime(execTime);
                        SYSTEM.scheduler().scheduleOnce(
                                Duration.create(job.getDelay(),TimeUnit.MILLISECONDS),
                                new RunAndReschedule(job.getId(), dal,schedulerID));
                    }
                    conn.commit();
                } finally {
                    conn.rollback();
                }
            }
        }
    }
}
