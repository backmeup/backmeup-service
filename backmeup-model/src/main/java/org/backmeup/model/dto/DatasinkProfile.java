package org.backmeup.model.dto;

public class DatasinkProfile {
	private long datasinkId;
	private String identification;
	
	public DatasinkProfile() {

	}

	public DatasinkProfile(long datasinkId, String identification) {
		this.datasinkId = datasinkId;
		this.identification = identification;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public long getDatasinkId() {
		return datasinkId;
	}

	public void setDatasinkId(long datasinkId) {
		this.datasinkId = datasinkId;
	}
}
