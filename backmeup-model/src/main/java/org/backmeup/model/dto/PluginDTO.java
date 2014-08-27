package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.spi.PluginDescribable.PluginType;

@XmlRootElement
public class PluginDTO {
	private String pluginId;
	private String title;
	private String description;
	private String imageURL;
	private PluginType pluginType;
	private Map<String, String> metadata;
	private PluginConfigurationDTO config;
	private List<PluginProfileDTO> profiles;

	public PluginDTO() {
		
	}

	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public PluginType getPluginType() {
		return pluginType;
	}

	public void setPluginType(PluginType pluginType) {
		this.pluginType = pluginType;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}
	
	public void addMetadata(String key, String value) {
		if(metadata == null) {
			metadata = new HashMap<>();
		}
		this.metadata.put(key, value);
	}

	public PluginConfigurationDTO getConfig() {
		return config;
	}

	public void setConfig(PluginConfigurationDTO config) {
		this.config = config;
	}

	public List<PluginProfileDTO> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<PluginProfileDTO> profiles) {
		this.profiles = profiles;
	}
	
	public void addProfile(PluginProfileDTO profile) {
		if(profiles == null) {
			profiles = new ArrayList<>();
		}
		profiles.add(profile);
	}
}
