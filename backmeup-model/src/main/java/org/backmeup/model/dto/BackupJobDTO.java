package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BackupJobDTO {
	public static enum JobStatus {
		queued, running, successful, error
	}

	private Long id;
	private User user;
	private String jobTitle;

	private Set<SourceProfileEntry> sourceProfiles = new HashSet<SourceProfileEntry>();
	private ProfileDTO sinkProfile;
	private List<ActionProfileEntry> actions = new ArrayList<ActionProfileEntry>();

	private Set<JobProtocolDTO> jobProtocols = new HashSet<JobProtocolDTO>();
	private Date start;
	private long delay;
	private TokenDTO token;
	private Date created;
	private Date modified;

	private String timeExpression;
	private Date nextExecutionTime;
	private boolean reschedule;
	private Date lastSuccessful;
	private Date lastFailed;
	private JobStatus status;
	private boolean onHold = false;
	private UUID validScheduleID = null;
	
	public BackupJobDTO() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.modified = new Date();
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.modified = new Date();
		this.user = user;
	}

	public Set<SourceProfileEntry> getSourceProfiles() {
		return sourceProfiles;
	}

	public void setSourceProfiles(Set<SourceProfileEntry> sourceProfiles) {
		this.modified = new Date();
		this.sourceProfiles = sourceProfiles;
	}

	public ProfileDTO getSinkProfile() {
		return sinkProfile;
	}

	public void setSinkProfile(ProfileDTO sinkProfile) {
		this.modified = new Date();
		this.sinkProfile = sinkProfile;
	}

	public List<ActionProfileEntry> getRequiredActions() {
		return actions;
	}

	public void setRequiredActions(List<ActionProfileEntry> requiredActions) {
		this.modified = new Date();
		this.actions = requiredActions;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.modified = new Date();
		this.start = start;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.modified = new Date();
		this.delay = delay;
	}

	public TokenDTO getToken() {
		return token;
	}

	public void setToken(TokenDTO token) {
		this.modified = new Date();
		this.token = token;
	}

	public Date getCreated() {
		return created;
	}

	public Date getModified() {
		return modified;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.modified = new Date();
		this.jobTitle = jobTitle;
	}

	public JobProtocolDTO lastProtocol() {
		JobProtocolDTO last = null;
		for (JobProtocolDTO jp : jobProtocols) {
			if (last == null
					|| jp.getExecutionTime().compareTo(last.getExecutionTime()) > 0) {
				last = jp;
			}
		}
		return last;
	}

	public Date getNextExecutionTime() {
		return nextExecutionTime;
	}

	public void setNextExecutionTime(Date nextExecutionTime) {
		this.nextExecutionTime = nextExecutionTime;
	}

	public boolean isReschedule() {
		return reschedule;
	}

	public void setReschedule(boolean reschedule) {
		this.reschedule = reschedule;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public Date getLastSuccessful() {
		return lastSuccessful;
	}

	public void setLastSuccessful(Date lastSuccessful) {
		this.lastSuccessful = lastSuccessful;
	}

	public Date getLastFailed() {
		return lastFailed;
	}

	public void setLastFailed(Date lastFailed) {
		this.lastFailed = lastFailed;
	}

	public boolean isOnHold() {
		return onHold;
	}

	public void setOnHold(boolean onHold) {
		this.onHold = onHold;
	}

	public String getTimeExpression() {
		return timeExpression;
	}

	public void setTimeExpression(String timeExpression) {
		this.timeExpression = timeExpression;
	}

	public UUID getValidScheduleID() {
		return validScheduleID;
	}

	public void setValidScheduleID(UUID validScheduleID) {
		this.validScheduleID = validScheduleID;
	}
}
