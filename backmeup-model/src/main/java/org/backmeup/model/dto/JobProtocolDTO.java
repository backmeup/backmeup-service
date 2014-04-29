package org.backmeup.model.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.backmeup.model.JobProtocol;

public class JobProtocolDTO {
	private Long id;
	private User user;
	private Long jobId;
	private String sinkTitle;
	private Date executionTime;
	private boolean successful;
	private long totalStoredEntries;
	private Set<JobProtocolMember> members = new HashSet<JobProtocolMember>();

	public JobProtocolDTO() {
	}

	public JobProtocolDTO(JobProtocol jobProtocol) {

	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getSinkTitle() {
		return sinkTitle;
	}

	public void setSinkTitle(String sinkTitle) {
		this.sinkTitle = sinkTitle;
	}

	public Date getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(Date executionTime) {
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

	public Set<JobProtocolMember> getMembers() {
		return members;
	}

	public void addMembers(Set<JobProtocolMember> members) {
		this.members.addAll(members);
	}

	public void addMember(JobProtocolMember member) {
		this.members.add(member);
	}

	public Long getId() {
		return id;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public static class JobProtocolMember {
		private Long id;
		private JobProtocolDTO protocol;
		private String title;
		private double space;

		public JobProtocolMember() {
		}

		public JobProtocolMember(JobProtocolDTO protocol, String title,
				double space) {
			this.protocol = protocol;
			this.title = title;
			this.space = space;
		}

		public JobProtocolDTO getProtocol() {
			return protocol;
		}

		public void setProtocol(JobProtocolDTO protocol) {
			this.protocol = protocol;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public double getSpace() {
			return space;
		}

		public void setSpace(double space) {
			this.space = space;
		}

		public Long getId() {
			return id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JobProtocolMember other = (JobProtocolMember) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}
	}
}
