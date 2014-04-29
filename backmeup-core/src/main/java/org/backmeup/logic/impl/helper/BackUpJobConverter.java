package org.backmeup.logic.impl.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.ActionProfile.ActionProperty;
import org.backmeup.model.BackupJob;
import org.backmeup.model.JobProtocol;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.dto.ActionProfileEntry;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.Job;
import org.backmeup.model.dto.JobUpdateRequest;
import org.backmeup.model.dto.SourceProfileEntry;
import org.backmeup.model.dto.User;

public class BackUpJobConverter {

	private static ActionProfileEntry findPreviousEntry(String id,
			List<ActionProfileEntry> profiles) {
		for (ActionProfileEntry ape : profiles) {
			if (ape.getId().equals(id)) {
				return ape;
			}
		}
		return null;
	}

	public static JobUpdateRequest convertToUpdateRequest(BackupJob job) {
		JobUpdateRequest jur = new JobUpdateRequest();
		jur.setJobId(job.getId());
		jur.setJobTitle(job.getJobTitle());
		jur.setSinkProfileId(job.getSinkProfile().getProfileId());
		jur.setTimeExpression(job.getTimeExpression());

		// convert the action profiles
		List<ActionProfileEntry> actions = new ArrayList<>();
		for (ActionProfile ap : job.getRequiredActions()) {
			ActionProfileEntry ape = findPreviousEntry(ap.getActionId(),
					actions);
			if (ape == null) {
				ape = new ActionProfileEntry();
				ape.setId(ap.getActionId());
				actions.add(ape);
			}
			for (ActionProperty property : ap.getActionOptions()) {
				ape.getOptions().put(property.getKey(), property.getValue());
			}
		}

		// convert the source profiles
		List<SourceProfileEntry> sources = new ArrayList<>();
		for (ProfileOptions po : job.getSourceProfiles()) {
			SourceProfileEntry entry = new SourceProfileEntry(po.getProfile()
					.getProfileId());
			for (String option : po.getOptions()) {
				entry.getOptions().put(option, "true");
			}
			sources.add(entry);
		}

		jur.setActions(actions);
		jur.setSourceProfiles(sources);
		return jur;
	}

	public static BackupJobDTO convertToDTO(BackupJob job) {
		BackupJobDTO backupJob = new BackupJobDTO();

		return backupJob;
	}

	public static Job convertToJob(BackupJob backupJob) {
		Date nextExecTime = null;
		if (backupJob.getNextExecutionTime() != null) {
			nextExecTime = backupJob.getNextExecutionTime();
		}
		Job job = new Job(
				backupJob.getId(), 
				backupJob.getSourceProfiles(),
				backupJob.getSinkProfile(), 
				backupJob.getStart().getTime(),
				backupJob.getCreated().getTime(), 
				backupJob.getModified().getTime(), 
				backupJob.getJobTitle(),
				backupJob.getDelay(), 
				backupJob.isOnHold());
		
		job.setUser(new User(backupJob.getUser()));
		
		job.setLastFail(backupJob.getLastFailed() != null ? backupJob.getLastFailed().getTime() : null);
		job.setLastSuccessful(backupJob.getLastSuccessful() != null ? backupJob.getLastSuccessful().getTime() : null);
		job.setStatus(backupJob.getStatus());
		job.setTimeExpression(backupJob.getTimeExpression());
		
		job.setTokenId(backupJob.getToken().getTokenId());
		job.setToken(backupJob.getToken().getToken());
		job.setBackupDate(backupJob.getToken().getBackupdate());
		
		JobProtocol protocol = backupJob.lastProtocol();

		if (protocol != null) {
			job.setLastBackup(protocol.getExecutionTime().getTime());
		}

		if (nextExecTime != null && nextExecTime.getTime() > new Date().getTime()) {
			job.setNextBackup(nextExecTime.getTime());
		}

		return job;
	}
}
