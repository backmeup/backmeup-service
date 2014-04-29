package org.backmeup.model.dto;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.FileItem;

@XmlRootElement
//@JsonSerialize(include = Inclusion.NON_NULL)
public class JobStatus {
	private Long statusId;
	private Long jobId;
	private String message;
	private String type;
	private String category;
	private Long timeStamp;
	private String progress;
    private Set<FileItem> files;


	public JobStatus() {
	}
	
	public JobStatus(Long jobId, String type, String category, Long timeStamp) {
		this(jobId, type, category, timeStamp, "", "", null);
	}

	public JobStatus(Long jobId, String type, String category, Long timeStamp, String progress) {
		this(jobId, type, category, timeStamp, progress, "", null);
	}
	
	public JobStatus(Long jobId, String type, String category, Long timeStamp, String progress, String message) {
		this(jobId, type, category, timeStamp, progress, message, null);
	}
	
	public JobStatus(Long jobId, String type, String category, Long timeStamp, String progress, String message, Set<FileItem> files) {
		this.jobId = jobId;
		this.type = type;
		this.category = category;
		this.timeStamp = timeStamp;
		this.progress = progress;
		this.message = message;
		this.files = files;
	}

	public Long getStatusId() {
		return statusId;
	}

	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Set<FileItem> getFiles() {
		return files;
	}

	public void setFiles(Set<FileItem> files) {
		this.files = files;
	}
}
