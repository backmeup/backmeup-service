package org.backmeup.job.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The thread is responsible for performing the main processing
 * work for the scheduler. 
 */
public class SchedulerThread extends Thread {
    private static long DEFAULT_IDLE_WAIT_TIME = 30L * 1000L;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SchedulerThread.class);

    private final Object lock = new Object();
    private boolean signaled;
    private long signaledNextFireTime;

    private boolean paused;
    private AtomicBoolean halted;

    private long idleWaitTime = DEFAULT_IDLE_WAIT_TIME;

    // Constructors -----------------------------------------------------------

    public SchedulerThread() {
        LOGGER.info("Starting scheduler thread");
        paused = true;
        halted = new AtomicBoolean(false);
    }

    // ========================================================================

    /**
     * Signal the main processing loop to pause at the next possible occasion.
     */
    public void setPause(boolean pause) {
        synchronized (lock) {
            paused = pause;

            if (paused) {
                signalSchedulingChange(0);
            } else {
                lock.notifyAll();
            }
        }
    }
    
    public boolean isPaused() {
        return paused;
    }

    /**
     * Singal the main processing loop to halt
     */
    public void halt(boolean wait) {
        synchronized (lock) {
            halted.set(true);
            if (paused) {
                lock.notifyAll();
            } else {
                signalSchedulingChange(0);
            }
        }

        if (wait) {
            boolean interrupted = false;
            try {
                while (true) {
                    try {
                        join();
                        break;
                    } catch (InterruptedException ie) {
                        interrupted = true;
                    }
                }
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void signalSchedulingChange(long candidateNewNextFireTime) {
        synchronized (lock) {
            signaled = true;
            signaledNextFireTime = candidateNewNextFireTime;
            lock.notifyAll();
        }
    }

    public void clearSignaledSchedulingChange() {
        synchronized (lock) {
            signaled = false;
            signaledNextFireTime = 0;
        }
    }

    public boolean isScheduleChanged() {
        synchronized (lock) {
            return signaled;
        }
    }

    public long getSignaledNextFireTime() {
        synchronized (lock) {
            return signaledNextFireTime;
        }
    }
    
    /**
     * The main processing loop of the scheduler thread
     */
    @Override
    public void run() {
        while (!halted.get()){
            try {
                // Check if we have to pause. If pause is true, wait
                // until setPause(false) is called.
                synchronized (lock) {
                    while (paused && !halted.get()) {
                        try {
                            lock.wait(1000L);
                        } catch (InterruptedException ignore) {
                        }
                    }

                    // We are free to continue.
                    // Check if halt was called in the meantime.
                    if (halted.get()) {
                        break;
                    }
                    
                    clearSignaledSchedulingChange();
                    
                    long now = System.currentTimeMillis();
                    List<Object> jobs  = new ArrayList<>();
                    // set scheduler info of jobs to e.g. 'reserved' or 'acquired'
//                    jobs = Store.acquireNextJobs(now + idleWaitTime)
                    
                    if(jobs != null && !jobs.isEmpty()) {
                        now = System.currentTimeMillis();
                        long triggerTime = 1L;
//                        triggerTime = jobs.get(0).getNextStartTime().getTime();
                        long timeUntilTrigger = triggerTime - now;

                        while(timeUntilTrigger > 2) {
                            synchronized (lock) {
                                try {
                                    // we could have blocked a long while on 
                                    // synchronize, so we must recompute
                                    now = System.currentTimeMillis();
                                    timeUntilTrigger = triggerTime - now;
                                    if (timeUntilTrigger >= 1) {
                                        lock.wait(timeUntilTrigger);
                                    }
                                } catch (InterruptedException ignore) {
                                }
                            }
                            now = System.currentTimeMillis();
                            timeUntilTrigger = triggerTime - now;
                        }
                        
                        for(Object job : jobs) {
                            // run job e.g. queue it or send it to worker
                            // set scheduler info of job to e.g. 'executing'
                            
                        }
                        
                        continue;
                    }
                    
                    now = System.currentTimeMillis();
                    long waitTime = now + idleWaitTime;
                    long timeUntilContinue = waitTime - now;
                    synchronized (lock) {
                        try {
                            if (!halted.get()) {
                                lock.wait(timeUntilContinue);
                            }
                        } catch (InterruptedException ignore) {
                        }
                    }
                }
            } catch (RuntimeException re) {
                LOGGER.error("Error in the main processing loop.", re);
            }
        }
    }
}
