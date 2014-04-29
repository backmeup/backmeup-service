package org.backmeup.rest.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.dto.Datasink;

@XmlRootElement
public class DatasinkContainer {
	private List<Datasink> sinks;

	public DatasinkContainer() {
	}
	
	public DatasinkContainer(List<Datasink> sinks) {
		super();
		this.sinks = sinks;
	}
	
	public List<Datasink> getSinks() {
		return sinks;
	}

	public void setSinks(List<Datasink> sinks) {
		this.sinks = sinks;
	}
}
