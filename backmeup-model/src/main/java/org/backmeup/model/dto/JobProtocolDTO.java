package org.backmeup.model.dto;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.JobProtocol;

@XmlRootElement
public class JobProtocolDTO {
	private Long id;
	private User user;
	private Long jobId;
	private String sinkTitle;
	private Long executionTime;
	private boolean successful;
	private long totalStoredEntries;
	private Set<JobProtocolMemberDTO> members = new HashSet<JobProtocolMemberDTO>();

	public JobProtocolDTO() {
	}
	
	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public String getSinkTitle() {
		return sinkTitle;
	}

	public void setSinkTitle(String sinkTitle) {
		this.sinkTitle = sinkTitle;
	}

	public Long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(Long executionTime) {
		this.executionTime = executionTime;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public long getTotalStoredEntries() {
		return totalStoredEntries;
	}

	public void setTotalStoredEntries(long totalStoredEntries) {
		this.totalStoredEntries = totalStoredEntries;
	}

	public Set<JobProtocolMemberDTO> getMembers() {
		return members;
	}

	public void addMembers(Set<JobProtocolMemberDTO> members) {
		this.members.addAll(members);
	}

	public void addMember(JobProtocolMemberDTO member) {
		this.members.add(member);
	}
}
