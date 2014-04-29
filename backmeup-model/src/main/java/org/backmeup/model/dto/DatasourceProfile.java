package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DatasourceProfile {
	private long datasourceId;
	private String identification;

	public DatasourceProfile() {

	}

	public DatasourceProfile(long datasourceId, String identification) {
		this.setDatasourceId(datasourceId);
		this.identification = identification;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public long getDatasourceId() {
		return datasourceId;
	}

	public void setDatasourceId(long datasourceId) {
		this.datasourceId = datasourceId;
	}
}
