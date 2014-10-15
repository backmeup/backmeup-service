package org.backmeup.model.dto;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthDataDTO {
	private Long authDataId;
	private String name;
	private Map<String, String> properties;
	
	public AuthDataDTO() {
		
	}
	
	public AuthDataDTO(long id, String name) {
		this.authDataId = id;
		this.name = name;
	}

	public Long getAuthDataId() {
		return authDataId;
	}

	public void setAuthDataId(Long authDataId) {
		this.authDataId = authDataId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
}
