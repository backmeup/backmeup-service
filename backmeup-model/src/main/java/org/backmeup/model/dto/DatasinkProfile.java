package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DatasinkProfile {
	private long profileId;
	private String identification;
	private String profileName;
	private String description;
	
	public DatasinkProfile() {

	}

	public DatasinkProfile(long profileId, String identification, String profileName, String description) {
		this.profileId = profileId;
		this.identification = identification;
		this.profileName = profileName;
		this.description = description;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public long getProfileId() {
		return profileId;
	}

	public void setProfileId(long profileId) {
		this.profileId = profileId;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
