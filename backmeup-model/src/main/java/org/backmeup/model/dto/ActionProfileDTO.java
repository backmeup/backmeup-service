package org.backmeup.model.dto;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ActionProfileDTO {
	private String actionId;
	private int priority;
	private Map<String, String> options = new HashMap<>();

	public ActionProfileDTO() {
	}

	public ActionProfileDTO(String actionId, int priority, Map<String, String> options) {
		this.actionId = actionId;
		this.priority = priority;
		this.options = options;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}
}
