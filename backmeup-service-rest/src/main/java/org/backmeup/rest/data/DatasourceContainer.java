package org.backmeup.rest.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.dto.Datasource;

@XmlRootElement
public class DatasourceContainer {
	private List<Datasource> sources;
	
	public DatasourceContainer() {
	}
	
	public DatasourceContainer(List<Datasource> sources) {
		super();
		this.sources = sources;
	}

	public List<Datasource> getSources() {
		return sources;
	}

	public void setSources(List<Datasource> sources) {
		this.sources = sources;
	}
}
