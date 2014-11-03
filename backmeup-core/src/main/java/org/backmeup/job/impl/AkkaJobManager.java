package org.backmeup.job.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.job.JobManager;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.logic.BackupLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.Token;
import org.backmeup.model.constants.BackupJobStatus;
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
abstract public class AkkaJobManager implements JobManager {
	
	private static final ActorSystem system = ActorSystem.create();
	
	@Inject
	protected Connection conn;
	
	@Inject
	protected DataAccessLayer dal;
	
	@Inject
	protected BackupLogic backupLogic;
	
	@Inject
	private Keyserver keyserver;
	
	private final Logger logger = LoggerFactory.getLogger(AkkaJobManager.class);

	private BackupJobDao getBackupJobDao() {
		return dal.createBackupJobDao();
	}

	@Override
	public BackupJob createBackupJob(BackMeUpUser user,
			Profile sourceProfiles, Profile sinkProfile,
			List<Profile> requiredActions, Date start, long delayInMs, 
			String jobTitle, boolean reschedule, String timeExpression) {
		try {
			conn.begin();

			// Create BackupJob entity in DB...
			BackupJob job = new BackupJob(user, sourceProfiles, sinkProfile, requiredActions, start, delayInMs, jobTitle, reschedule);
			job.setTimeExpression(timeExpression);
			job.setStatus(BackupJobStatus.queued);

			Long firstExecutionDate = start.getTime() + delayInMs;

//			String encryptionPwd = null;
//			Properties p = new Properties();
//			for (Profile ap : requiredActions) {
//				for (Entry<String, String> prop : ap.getProperties().entrySet()) {
//					p.put(prop.getKey(), prop.getValue());
//				}
//			}
//			encryptionPwd = (String) p.get("org.backmeup.encryption.password");

			// reusable=true means, that we can get the data for the token + a
			// new token for the next backup
//			Token t = keyserver.getToken(job, user.getPassword(), firstExecutionDate, true, encryptionPwd);
//			job.setToken(t);
			Token dummyToken = new Token("1111-222-333", 1l, firstExecutionDate);
			job.setToken(dummyToken);
			
			job = getBackupJobDao().save(job);
			
			conn.commit();
			
			// Queue new job immediately
			// TODO: currently disabled due to major refactoring
//			queueJob(job);
			return job;
		} finally {
			conn.rollback();
		}

	}
	
	@Override
	public BackupJob createBackupJob(BackupJob backupJob) {
		try {
			conn.begin();
			BackupJob job = backupLogic.createJob(backupJob);
			conn.commit();
			
			// Queue new job immediately
			queueJob(job);
			return job;
		} finally {
			conn.rollback();
		}

	}
	
	private BackupJob getBackUpJob(Long jobId) {
		return getBackupJobDao().findById(jobId);
	}

	@Override
	public void start() {
		// TODO only take N next recent ones (at least if allJobs has an
		// excessive length)
		try {
			conn.begin();
			List<BackupJob> storedJobs = getBackupJobDao().findAll();
			conn.rollback();
			for (BackupJob storedJob : storedJobs) {
				queueJob(storedJob);
			}
		} finally {
			conn.rollback();
		}
	}

	@Override
	public void shutdown() {
		// Shutdown system component
		system.shutdown();
		system.awaitTermination();
	}

	protected abstract void runJob(BackupJob job);

	@Override
    public void runBackUpJob(BackupJob job) {
		queueJob(job);
	}
	
	// Don't call this method within a database transaction!
	private void queueJob(BackupJob job) {
		try {
			// Compute next job execution time
			long currentTime = new Date().getTime();
			long executeIn = job.getStart().getTime() - currentTime;
			if (job.getNextExecutionTime() != null) {
				executeIn = job.getNextExecutionTime().getTime() - currentTime;
			}

			// If job execution was scheduled for within the past 5 mins, still
			// schedule now...
			if (executeIn >= -300000 && executeIn < 0) {
				executeIn = 0;
			}

			// ...otherwise, schedule on the next occasion defined by .getStart
			// and .getDelay
			if (executeIn < 0) {
				executeIn += Math.ceil((double) Math.abs(executeIn) / (double) job.getDelay()) * job.getDelay();
				conn.begin();
				job = getBackUpJob(job.getId());
				// TODO we need to update these jobs' tokens - but where do we
				// get keyRing password from?
				job.getToken().setBackupdate(currentTime + executeIn);

				// get access data + new token for next access
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
			UUID schedulerID = UUID.randomUUID();
			job.setValidScheduleID(schedulerID);
			conn.commit();

			// TODO we can use the 'cancellable' to terminate later on
			system.scheduler().scheduleOnce(
					Duration.create(executeIn, TimeUnit.MILLISECONDS), // Initial delay
					new RunAndReschedule(job, dal, schedulerID));

		} catch (Exception e) {
			// TODO there must be error handling defined in the JobManager!^
			logger.error("Error during startup", e);
			// throw new BackMeUpException(e);
		} finally {
			conn.rollback();
		}
	}
	
	private class RunAndReschedule implements Runnable {
		private final Logger logger = LoggerFactory.getLogger(RunAndReschedule.class);

		private final BackupJob job;
		private final DataAccessLayer dal;
		private final UUID schedulerID;

		RunAndReschedule(BackupJob job, DataAccessLayer dal, UUID schedulerID) {
			this.job = job;
			this.dal = dal;
			this.schedulerID = schedulerID;
		}

		@Override
		public void run() {
			// check if the scheduler is still valid. If not a new scheduler was
			// created and this one should not be executed
			if (job.getValidScheduleID().compareTo(schedulerID) != 0) {
				return;
			}

			// Run the job
			if (!job.isOnHold()) {
				runJob(job);
			}

			// Reschedule if it's still in the DB
			try {
				conn.beginOrJoin();

				BackupJob nextJob = dal.createBackupJobDao().findById(job.getId());
				if (nextJob != null && nextJob.isReschedule()) {
					logger.debug("Rescheduling job for execution in "+ job.getDelay() + "ms");
					Date execTime = new Date(new Date().getTime() + job.getDelay());
					nextJob.setNextExecutionTime(execTime);
					system.scheduler().scheduleOnce(
							Duration.create(job.getDelay(),
									TimeUnit.MILLISECONDS),
							new RunAndReschedule(job, dal, schedulerID));
					// store the next execution time
					conn.commit();
				} else {
					logger.debug("Job deleted in the mean time - no re-scheduling.");
				}
			} finally {
				conn.rollback();
			}
		}
	}
}
