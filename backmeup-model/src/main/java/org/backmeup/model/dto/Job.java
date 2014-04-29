package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.BackupJob.JobStatus;
import org.backmeup.model.constants.DelayTimes;

@XmlRootElement
public class Job {
	private Long backupJobId;
	private List<DatasourceProfile> datasources;
	private DatasinkProfile datasink;
	private Long startDate;
	private Long createDate;
	private Long modifyDate;
	private String jobTitle;
	private Long delay;
	private String timeExpression;
	private Long lastBackup;
	private Long nextBackup;
	private Long lastSuccessful;
	private Long lastFail;
	private JobStatus status;
	private boolean isOnHold;

	public Job() {
	}

	public Job(long backupJobId, Set<ProfileOptions> datasourceIds,
			Profile datasinkProfile, Long startDate, Long createDate,
			Long modifyDate, String jobTitle, Long delay, boolean isOnHold) {
		this.backupJobId = backupJobId;
		this.setDatasources(new ArrayList<DatasourceProfile>());
		for (ProfileOptions po : datasourceIds) {
			this.getDatasources().add(
					new DatasourceProfile(po.getProfile().getProfileId(), po
							.getProfile().getIdentification()));
		}
		this.setDatasink(new DatasinkProfile(datasinkProfile.getProfileId(),
				datasinkProfile.getIdentification()));
		this.startDate = startDate;
		this.createDate = createDate;
		this.modifyDate = modifyDate;
		this.jobTitle = jobTitle;
		this.delay = delay;
		this.isOnHold = isOnHold;

		if (delay == DelayTimes.DELAY_DAILY)
			setTimeExpression("daily");
		else if (delay == DelayTimes.DELAY_MONTHLY)
			setTimeExpression("monthly");
		else if (delay == DelayTimes.DELAY_WEEKLY)
			setTimeExpression("weekly");
		else if (delay == DelayTimes.DELAY_YEARLY)
			setTimeExpression("yearly");
		else
			setTimeExpression("realtime");
	}

	public Long getBackupJobId() {
		return backupJobId;
	}

	public void setBackupJobId(Long backupJobId) {
		this.backupJobId = backupJobId;
	}

	public List<DatasourceProfile> getDatasources() {
		return datasources;
	}

	public void setDatasources(List<DatasourceProfile> datasources) {
		this.datasources = datasources;
	}

	public DatasinkProfile getDatasink() {
		return datasink;
	}

	public void setDatasink(DatasinkProfile datasink) {
		this.datasink = datasink;
	}

	public Long getStartDate() {
		return startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	public Long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}

	public Long getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Long modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public Long getDelay() {
		return delay;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	public Long getLastBackup() {
		return lastBackup;
	}

	public void setLastBackup(Long lastBackup) {
		this.lastBackup = lastBackup;
	}

	public Long getNextBackup() {
		return nextBackup;
	}

	public void setNextBackup(Long nextBackup) {
		this.nextBackup = nextBackup;
	}

	public String getTimeExpression() {
		return timeExpression;
	}

	public void setTimeExpression(String timeExpression) {
		this.timeExpression = timeExpression;
	}

	public Long getLastSuccessful() {
		return lastSuccessful;
	}

	public void setLastSuccessful(Long lastSuccessful) {
		this.lastSuccessful = lastSuccessful;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public Long getLastFail() {
		return lastFail;
	}

	public void setLastFail(Long lastFail) {
		this.lastFail = lastFail;
	}

	public boolean isOnHold() {
		return isOnHold;
	}

	public void setOnHold(boolean onHold) {
		this.isOnHold = onHold;
	}
}
