package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.spi.PluginDescribable.PluginType;

@XmlRootElement
public class PluginProfileDTO {
	private long profileId;
	private String pluginId;
	private PluginType profileType;
	private long modified;
	private AuthDataDTO authData;
	// Init with HashMap is necessary as long as the props are 
	// stored within the service db (jpa mapping). Otherwise, 
	// dozer maps the persistentmap from the domain model
	// which results in a lazyinitexception in dozer. 
	private Map<String, String> properties = new HashMap<>();
	private List<String> options;
	
	public PluginProfileDTO() {
		
	}

	public long getProfileId() {
		return profileId;
	}

	public void setProfileId(long profileId) {
		this.profileId = profileId;
	}

	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public PluginType getProfileType() {
		return profileType;
	}

	public void setProfileType(PluginType profileType) {
		this.profileType = profileType;
	}

	public long getModified() {
		return modified;
	}

	public void setModified(long modified) {
		this.modified = modified;
	}

	public AuthDataDTO getAuthData() {
		return authData;
	}

	public void setAuthData(AuthDataDTO authData) {
		this.authData = authData;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> configProperties) {
		this.properties = configProperties;
	}
	
	public void addProperty(String key, String value) {
		if(properties == null) {
			properties = new HashMap<>();
		}
		properties.put(key, value);
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}
	
	public void addOption(String option) {
		if(options == null) {
			options = new ArrayList<>();
		}
		options.add(option);
	}
	
}
