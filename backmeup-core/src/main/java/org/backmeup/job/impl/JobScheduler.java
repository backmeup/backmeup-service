package org.backmeup.job.impl;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main part of the scheduler component. It contains methods to
 * schedule and manage BackupJob instances.
 */
public class JobScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);
    private SchedulerThread schedulerThread;
    
    private ExecutorService executor;
    
    private volatile boolean closed = false;
    private volatile boolean shuttingDown = false;
    private Date initialStart;
    
    public JobScheduler() {
        this.closed = false;
        this.shuttingDown = false;
        
        this.schedulerThread = new SchedulerThread();
        
        executor = Executors.newSingleThreadExecutor();
        executor.execute(schedulerThread);
        
        LOGGER.info("JobScheduler created.");
    }
    
    // Properties -------------------------------------------------------------
    
    public boolean isShutdown() {
        return closed;
    }

    public boolean isShuttingDown() {
        return shuttingDown;
    }

    public boolean isStarted() {
        return !shuttingDown && !closed && !isInStandbyMode() && initialStart != null;
    }

    public boolean isInStandbyMode() {
        return schedulerThread.isPaused();
    }

    public Date getInitialStart() {
        if (initialStart == null)
            return null;
        return new Date(initialStart.getTime());
    }

    // State Management -------------------------------------------------------

    public void start() {
        if (shuttingDown || closed) {
            throw new SchedulerException(
                    "Scheduler cannot be restartet after shutdown() hase been called.");
        }

        if (initialStart == null) {
            initialStart = new Date();
        }

        schedulerThread.setPause(false);

        LOGGER.info("Scheduler started.");
    }

    public void standby() {
        schedulerThread.setPause(true);
        LOGGER.info("Scheduler paused.");
    }

    public void shutdown() {
        shutdown(false);
    }

    public void shutdown(boolean waitForJobsToComplete) {
        if (shuttingDown || closed) {
            return;
        }

        LOGGER.info("JobScheduler is shutting down.");

        this.shuttingDown = true;
        standby();
        schedulerThread.halt(waitForJobsToComplete);
        this.closed = true;
        
        LOGGER.info("JobScheduler shutdown completed.");
    }
}
