package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobProtocolMemberDTO {
	private Long id;
	private Long jobProtocolId;
	private String title;
	private double space;

	public JobProtocolMemberDTO() {
	}

	public JobProtocolMemberDTO(Long jobProtocolId, String title, double space) {
		this.jobProtocolId = jobProtocolId;
		this.title = title;
		this.space = space;
	}

	public Long getId() {
		return id;
	}

	public Long getJobProtocolId() {
		return jobProtocolId;
	}

	public void setJobProtocolId(Long jobProtocolId) {
		this.jobProtocolId = jobProtocolId;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JobProtocolMemberDTO other = (JobProtocolMemberDTO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
