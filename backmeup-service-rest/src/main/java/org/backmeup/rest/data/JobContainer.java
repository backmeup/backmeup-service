package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.logic.impl.helper.BackUpJobConverter;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.dto.Job;

@XmlRootElement
public class JobContainer {

	private UserContainer user;

	private Long lastBackup;

	private Long nextBackup;

	private List<Job> backupJobs;

	public JobContainer() {
	}

	public JobContainer(List<BackupJob> backupJobs, BackMeUpUser user) {
		this.backupJobs = new ArrayList<>();
		for (BackupJob j : backupJobs) {
			Job job = BackUpJobConverter.convertToJob(j);
			this.backupJobs.add(job);
		}

		if (this.backupJobs.size() > 0) {
			Collections.sort(this.backupJobs, new Comparator<Job>() {
				@Override
				public int compare(Job a, Job b) {
					return (int) (a.getStartDate() - b.getStartDate());
				}
			});

			Job lastJob = this.backupJobs.get(this.backupJobs.size() - 1);
			this.lastBackup = lastJob.getStartDate();
		}

		this.user = new UserContainer(user);
	}

	public UserContainer getUser() {
		return user;
	}

	public void setUser(UserContainer user) {
		this.user = user;
	}

	public Long getLastBackup() {
		return this.lastBackup;
	}

	public void setLastBackup(Long lastBackup) {
		this.lastBackup = lastBackup;
	}

	public Long getNextBackup() {
		return this.nextBackup;
	}

	public void setNextBackup(Long nextBackup) {
		this.nextBackup = nextBackup;
	}

	public List<Job> getBackupJobs() {
		return backupJobs;
	}

	public void setBackupJobs(List<Job> backupJobs) {
		this.backupJobs = backupJobs;
	}
}
