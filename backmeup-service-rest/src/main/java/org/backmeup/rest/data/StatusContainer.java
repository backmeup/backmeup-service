package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.Status;
import org.backmeup.model.dto.JobStatus;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@XmlRootElement
@JsonSerialize(include = Inclusion.NON_NULL)
public class StatusContainer {
	private List<JobStatus> backupStatus;

	public StatusContainer() {
	}

	public StatusContainer(List<Status> backupStatus) {
		this.backupStatus = new ArrayList<JobStatus>();
		for (Status s : backupStatus) {			
			this.backupStatus.add(new JobStatus(s.getMessage(), s.getType(), s.getCategory (), s.getTimeStamp().getTime()+"", s.getProgress(), s.getFiles(), s.getJob ().getId ()));
		}
	}

	public List<JobStatus> getBackupStatus() {
		return backupStatus;
	}

	public void setBackupStatus(List<JobStatus> backupStatus) {
		this.backupStatus = backupStatus;
	}
}
