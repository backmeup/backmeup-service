package org.backmeup.model.dto;

import java.util.Set;

import org.backmeup.model.FileItem;

//@JsonSerialize(include = Inclusion.NON_NULL)
public class JobStatus {
	private String message;
	private String type;
	private String category;
	private String timeStamp;
	private String progress;
	private Set<FileItem> files;
	private Long jobId;

	public JobStatus() {
	}

	public JobStatus(String message, String type, String category,
			String timeStamp, String progress, Set<FileItem> files, Long jobId) {
		this.message = message;
		this.type = type;
		this.category = category;
		this.timeStamp = timeStamp;
		this.progress = progress;
		this.files = files;
		this.jobId = jobId;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public Set<FileItem> getFiles() {
		return files;
	}

	public void setFiles(Set<FileItem> files) {
		this.files = files;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
