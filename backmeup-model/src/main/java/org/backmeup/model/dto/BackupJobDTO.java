package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.constants.JobFrequency;
import org.backmeup.model.constants.JobStatus;
import org.backmeup.model.spi.PluginDescribable.PluginType;


@XmlRootElement
public class BackupJobDTO {
	private Long jobId;
	private String jobTitle;
	private JobStatus status;
	
	private UserDTO user;
	private JobFrequency schedule;
	
	private Date created;
	private Date modified;
	private Date start;
	private Date next;

	private PluginProfileDTO source;
	private List<PluginProfileDTO> actions;
	private PluginProfileDTO sink;
	
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

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus jobStatus) {
		this.status = jobStatus;
	}

    public boolean isActive() {
        return status == JobStatus.ACTIVE;
    }

    public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
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

	public PluginProfileDTO getSource() {
		return source;
	}

	public void setSource(PluginProfileDTO source) {
		if((source.getProfileType() != PluginType.Source) && (source.getProfileType() != PluginType.SourceSink)) {
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
		if (action.getProfileType() != PluginType.Action) {
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
		if ((sink.getProfileType() != PluginType.Sink) && (sink.getProfileType() != PluginType.SourceSink)) {
			throw new IllegalArgumentException("Only profiles from a sink plugin can be assigned, but type is: " + source.getProfileType());
		}
		this.sink = sink;
	}
}
