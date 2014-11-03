package org.backmeup.model;

import java.util.List;

import org.backmeup.model.api.RequiredInputField;

/**
 * The PluginConfigInfo class is used to transport the required information 
 * for a plugin configuration. Depending on what kind of plugin (source, sink 
 * or action) has been requested, following properties will be set: - if 
 * redirectURL is not null, the client must open a browser and enter this URL 
 * - if requiredInputs and propertiesDescription is not null, the client must 
 * enter all the values specified within this list.
 * 
 */
public class PluginConfigInfo {
	private String redirectURL;
	private List<RequiredInputField> requiredInputs;
	private List<RequiredInputField> propertiesDescription;
	private List<String> availableOptions;


	public PluginConfigInfo() {
	}
	
	public List<RequiredInputField> getRequiredInputs() {
		return requiredInputs;
	}

	public void setRequiredInputs(List<RequiredInputField> requiredInputs) {
		this.requiredInputs = requiredInputs;
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public List<RequiredInputField> getPropertiesDescription() {
		return propertiesDescription;
	}

	public void setPropertiesDescription(
			List<RequiredInputField> propertiesDescription) {
		this.propertiesDescription = propertiesDescription;
	}

	public List<String> getAvailableOptions() {
		return availableOptions;
	}

	public void setAvailableOptions(List<String> availableOptions) {
		this.availableOptions = availableOptions;
	}
	
	public boolean hasAuthData() {		
		return redirectURL != null || requiredInputs != null;
	}
	
	public boolean hasConfigData() {
		return propertiesDescription != null || availableOptions != null;
	}
}
