package org.backmeup.service.scheduler;

import java.util.Date;
import java.util.List;

/**
 * This is the main interface of a Backmeup Service Scheduler
 * 
 * A Scheduler maintains a set of BackupJobs and is responsible
 * for executing them when their schedule time arrives. 
 *
 */
public interface Scheduler {
    ///////////////////////////////////////////////////////////////////////////
    ///
    /// Scheduler State Management
    ///
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Starts up the Scheduler.
     * 
     * When a scheduler is created for the first time and empty,
     * it is in "stand-by" mode and will not trigger any job executions.
     * 
     * If the scheduler is already managing jobs (eg. there are jobs in it's 
     * db tables), the recovery process will be started
     * 
     */
    void start();
    
    /**
     * Indicates whether the scheduler has been started. 
     * 
     */
    boolean isStarted();
    
    /**
     * Temporarily halts the scheduler and prevents it from triggering job
     * executions. The scheduler can be restarted at any time by calling 
     * start() method;
     * 
     */
    void standby();
    
    /**
     * Indicates whether the scheduler is in stand-by mode. 
     * 
     */
    boolean isInStandby();
    
    /**
     * Halts the scheduler and cleans up all resources.
     * From this state, the scheduler can not be restarted. 
     * 
     * @param waitForJobsToComplete
     *          if true the scheduler will not allow this method
     *          to return until all currently executing jobs have completed.
     * 
     */
    void shutdown(boolean waitForJobsToComplete);

    /**
     * Indicates whether the scheduler has been shutdown.
     */
    boolean isShutdown();
    
    /**
     * Returns a list of objects that the scheduler is currently executing.
     * 
     */
    List<Object> getCurrentlyExecutingJobs();
    
    ///////////////////////////////////////////////////////////////////////////
    ///
    /// Scheduling Methods
    ///
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Add the give BackupJob to the scheduler and associate the given Trigger 
     * with it.
     * 
     */
    Date scheduleJob(Object job, Object trigger);
    
    /**
     * Remove the given trigger from the scheduler. 
     * It its associated job doesn't have any other triggers, 
     * it is also deleted.
     * 
     */
    boolean unscheduleJob(Object triggerKey);
    
    /**
     * Add the given BackupJob to the scheduler with no associated trigger!
     * The job will be 'dormant' until it is associated with a trigger. 
     */
    void addJob(Object job, boolean replace);
    
    /**
     * Deleted the given BackupJob from the scheduler and 
     * any associated data. 
     * 
     * @return true if the job was found and deleted.
     */
    boolean deleteJob(Object jobKey);
    
    /**
     * Trigger the given BackupJob and execute it now.
     */
    void triggerJob(Object jobKey);
    
    /**
     * Pause the given BackupJob by pausing all of its triggers. 
     * Use the resumeJob() method to un-pause the job. 
     */
    void pauseJob(Object jobKey);
    
    /**
     * Resume (un-pause) the given BackupJob. 
     */
    void resumeJob(Object jobKey);
    
    /**
     * Pause all triggers. 
     * Use resumeAll() after this method
     */
    void pauseAll();
    
    /**
     * Resume all triggers. 
     */
    void resumeAll();
    
    /**
     * Clears (deletes!) all scheduling data.
     */
    void clear();
    
}
