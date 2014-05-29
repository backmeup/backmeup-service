package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DatasourceProfile {
	private long datasourceId;
	private String identification;
	private String description;
	private List<String> datasourceOptions;

	public DatasourceProfile() {

	}

	public DatasourceProfile(long datasourceId, String identification, String description, List<String> datasourceOptions) {
		this.setDatasourceId(datasourceId);
		this.identification = identification;
		this.description = description;
		this.datasourceOptions = datasourceOptions;
	}
	
	public long getDatasourceId() {
		return datasourceId;
	}

	public void setDatasourceId(long datasourceId) {
		this.datasourceId = datasourceId;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getDatasourceOptions() {
		return datasourceOptions;
	}

	public void setDatasourceOptions(List<String> datasourceOptions) {
		this.datasourceOptions = datasourceOptions;
	}
	
	public void addOption(String option) {
		if(datasourceOptions == null) {
			datasourceOptions = new ArrayList<>();
		}
		this.datasourceOptions.add(option);
	}
}
