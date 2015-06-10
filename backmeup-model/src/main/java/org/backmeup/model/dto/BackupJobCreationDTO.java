package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.constants.JobFrequency;

@XmlRootElement
public class BackupJobCreationDTO {
	private String jobTitle;
	private JobFrequency schedule;
	private Date start;

	private Long source;
	private List<Long> actions;
	private Long sink;

	public BackupJobCreationDTO() {

	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public JobFrequency getSchedule() {
		return schedule;
	}

	public void setSchedule(JobFrequency schedule) {
		this.schedule = schedule;
	}

    public Date getStart() {
        if (this.start == null) {
            return null;
        }
        return (Date) start.clone();
    }

	public void setStart(Date start) {
		this.start = (Date) start.clone();
	}

	public Long getSource() {
		return source;
	}

	public void setSource(Long source) {
		this.source = source;
	}

	public List<Long> getActions() {
		return actions;
	}

	public void setActions(List<Long> actions) {
		this.actions = actions;
	}
	
	public void addAction(Long action) {
		if(actions == null) {
			actions = new ArrayList<>();
		}
		actions.add(action);
	}

	public Long getSink() {
		return sink;
	}

	public void setSink(Long sink) {
		this.sink = sink;
	}
}
