package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.ActionProfile.ActionProperty;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.constants.BackupJobStatus;
import org.backmeup.model.constants.DelayTimes;

@XmlRootElement
public class Job {
	private Long jobId;
	private String jobTitle;
	
	private User user;
	
	private List<DatasourceProfile> datasources;
	private List<ActionProfileDTO> actions;
	private DatasinkProfile datasink;
	
	private Long tokenId;
	private String token;
	private Long backupDate;
	
	private Long startDate;
	private Long createDate;
	private Long modifyDate;
	
	private Long delay;
	private String timeExpression;
	
	private Long lastBackup;
	private Long lastSuccessful;
	private Long lastFailed;
	private Long nextBackup;

	private BackupJobStatus status;
	private boolean isOnHold;

	public Job() {
	}

	public Job(
			long backupJobId, Set<ProfileOptions> datasourceIds, Profile datasinkProfile, 
			List<ActionProfile> actionProfiles, Long startDate, Long createDate, 
			Long modifyDate, String jobTitle, Long delay, boolean isOnHold) {
		this.jobId = backupJobId;
		this.setDatasources(new ArrayList<DatasourceProfile>());
		for (ProfileOptions po : datasourceIds) {
			this.getDatasources().add(
					new DatasourceProfile(
							po.getProfile().getProfileId(), 
							po.getProfile().getIdentification(),
							po.getProfile().getDescription(),
							new ArrayList<String>(Arrays.asList(po.getOptions()))));
		}
		this.setActions(new ArrayList<ActionProfileDTO>());
		for(ActionProfile ap : actionProfiles){
			Map<String, String> apOptions = new HashMap<String, String>();
			for(ActionProperty property : ap.getActionOptions()) {
				apOptions.put(property.getKey(), property.getValue());
			}
			ActionProfileDTO apDTO = new ActionProfileDTO(ap.getActionId(), ap.getPriority(), apOptions);
			this.getActions().add(apDTO);
		}
		this.setDatasink(
				new DatasinkProfile(
						datasinkProfile.getProfileId(),
						datasinkProfile.getIdentification(),
						datasinkProfile.getProfileName(),
						datasinkProfile.getDescription()));
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

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public List<ActionProfileDTO> getActions() {
		return actions;
	}

	public void setActions(List<ActionProfileDTO> actions) {
		this.actions = actions;
	}

	public Long getTokenId() {
		return tokenId;
	}

	public void setTokenId(Long tokenId) {
		this.tokenId = tokenId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getBackupDate() {
		return backupDate;
	}

	public void setBackupDate(Long backupDate) {
		this.backupDate = backupDate;
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

	public BackupJobStatus getStatus() {
		return status;
	}

	public void setStatus(BackupJobStatus status) {
		this.status = status;
	}

	public Long getLastFailed() {
		return lastFailed;
	}

	public void setLastFailed(Long lastFail) {
		this.lastFailed = lastFail;
	}

	public boolean isOnHold() {
		return isOnHold;
	}

	public void setOnHold(boolean onHold) {
		this.isOnHold = onHold;
	}
}
