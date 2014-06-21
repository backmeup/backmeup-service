package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.dto.PluginDTO.PluginType;

@XmlRootElement
public class BackupJobDTO {
	public static enum JobStatus {
		queued, running, successful, error
	}
	
	public static enum JobFrequency {
		onece, daily, weekly, montly
	}

	private Long jobId;
	private String jobTitle;
	private JobStatus jobStatus;
	private boolean onHold = false;
	
	private UserDTO user;
	private TokenDTO token;
	private JobFrequency schedule;
	
	private Date created;
	private Date modified;
	private Date start;
	private Date next;
	private long delay;

	private PluginProfileDTO source;
	private List<PluginProfileDTO> actions;
	private PluginProfileDTO sink;
	
	private Set<JobProtocolDTO> protocol;

	public BackupJobDTO() {
		
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public JobStatus getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(JobStatus jobStatus) {
		this.jobStatus = jobStatus;
	}

	public boolean isOnHold() {
		return onHold;
	}

	public void setOnHold(boolean onHold) {
		this.onHold = onHold;
	}

	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}

	public TokenDTO getToken() {
		return token;
	}

	public void setToken(TokenDTO token) {
		this.token = token;
	}

	public JobFrequency getSchedule() {
		return schedule;
	}

	public void setSchedule(JobFrequency schedule) {
		this.schedule = schedule;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getNext() {
		return next;
	}

	public void setNext(Date next) {
		this.next = next;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public PluginProfileDTO getSource() {
		return source;
	}

	public void setSource(PluginProfileDTO source) {
		if((source.getProfileType() != PluginType.source) && (source.getProfileType() != PluginType.sourcesink)) {
			throw new IllegalArgumentException("Only profiles from a source plugin can be assigned, but type is: " + source.getProfileType());
		}
		this.source = source;
	}

	public List<PluginProfileDTO> getActions() {
		return actions;
	}

	public void setActions(List<PluginProfileDTO> actions) {
		this.actions = actions;
	}
	
	public void addAction(PluginProfileDTO action) {
		if (action.getProfileType() != PluginType.action) {
			throw new IllegalArgumentException("Only profiles from action plugins can be assigned, but type is: " + action.getProfileType());
		}
		
		if (actions == null) {
			actions = new ArrayList<>();
		}
		this.actions.add(action);
	}

	public PluginProfileDTO getSink() {
		return sink;
	}

	public void setSink(PluginProfileDTO sink) {
		if ((sink.getProfileType() != PluginType.sink) && (sink.getProfileType() != PluginType.sourcesink)) {
			throw new IllegalArgumentException("Only profiles from a sink plugin can be assigned, but type is: " + source.getProfileType());
		}
		this.sink = sink;
	}

	public Set<JobProtocolDTO> getProtocol() {
		return protocol;
	}
	
	public void setProtocol(Set<JobProtocolDTO> protocol) {
		this.protocol = protocol;
	}

	public void addProtocol(JobProtocolDTO jobProtocol) {
		if(this.protocol == null) {
			this.protocol = new HashSet<>();
		}
		protocol.add(jobProtocol);
	}
	
	public JobProtocolDTO lastProtocol() {
		JobProtocolDTO last = null;
		if(protocol != null) {
			for (JobProtocolDTO jp : protocol) {
				if (last == null || jp.getExecutionTime().compareTo(last.getExecutionTime()) > 0) {
					last = jp;
				}
			}
		}
		return last;
	}
}
