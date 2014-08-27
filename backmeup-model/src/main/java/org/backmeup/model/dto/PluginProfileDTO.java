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
	private String title;
	private String pluginId;
	private PluginType profileType;
	private long modified;
	private Map<String, String> configProperties;
	private List<String> options;
	
	public PluginProfileDTO() {
		
	}

	public long getProfileId() {
		return profileId;
	}

	public void setProfileId(long profileId) {
		this.profileId = profileId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public Map<String, String> getConfigProperties() {
		return configProperties;
	}

	public void setConfigProperties(Map<String, String> configProperties) {
		this.configProperties = configProperties;
	}
	
	public void addConfigProperties(String key, String value) {
		if(configProperties == null) {
			configProperties = new HashMap<>();
		}
		configProperties.put(key, value);
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
