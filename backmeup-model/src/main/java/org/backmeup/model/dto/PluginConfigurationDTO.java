package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PluginConfigurationDTO {
	public enum PluginConfigurationType {
		input, oauth
	}
	
	private PluginConfigurationType configType;
	private String redirectURL;
	private List<PluginInputFieldDTO> requiredInputs;
	
	public PluginConfigurationDTO() {
		
	}
	
	public PluginConfigurationDTO( PluginConfigurationType type) {
		this.configType = type;
	}

	public PluginConfigurationType getConfigType() {
		return configType;
	}

	public void setConfigType(PluginConfigurationType configType) {
		this.configType = configType;
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public List<PluginInputFieldDTO> getRequiredInputs() {
		return requiredInputs;
	}

	public void setRequiredInputs(List<PluginInputFieldDTO> requiredInputs) {
		this.requiredInputs = requiredInputs;
	}	
	
	public void addRequiredInput(PluginInputFieldDTO requiredInput) {
		if(requiredInputs == null) {
			requiredInputs = new ArrayList<>();
		}
		this.requiredInputs.add(requiredInput);
	}
}
