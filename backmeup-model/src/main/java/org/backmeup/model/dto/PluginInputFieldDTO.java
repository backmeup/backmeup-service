package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.api.RequiredInputField.Type;

@XmlRootElement
public class PluginInputFieldDTO {
	public enum PluginInputFieldType {
		string, number, password, bool
	}
	
	private String name;
	private String label;
	private String description;
	private boolean required;
	private int order;
	private Type type;
	
	public PluginInputFieldDTO() {
		
	}
	
	public PluginInputFieldDTO(String name, String label, String description, boolean required, int order, Type type) {
		this.name = name;
		this.label = label;
		this.description = description;
		this.required = required;
		this.order = order;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
