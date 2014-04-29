package org.backmeup.model.dto;

import java.util.Date;

import org.backmeup.model.Profile;
import org.backmeup.model.spi.SourceSinkDescribable.Type;

public class ProfileDTO {
	private Long profileId;

	private UserDTO user;
	private String profileName;
	private String description;

	private Date created;

	private Date modified;

	private String identification;

	private Type sourceAndOrSink;

	public ProfileDTO() {
	}

	public ProfileDTO(Profile profile) {
		this.profileId = profile.getProfileId();
		this.user = new UserDTO(profile.getUser());
		this.profileName = profile.getProfileName();
		this.description = profile.getDescription();
		this.created = profile.getCreated();
		this.modified = profile.getModified();
		this.identification = profile.getIdentification();
		this.sourceAndOrSink = profile.getType();
	}

	public Long getProfileId() {
		return profileId;
	}

	public void setProfileId(Long profileId) {
		this.modified = new Date();
		this.profileId = profileId;
	}

	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.modified = new Date();
		this.user = user;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.modified = new Date();
		this.profileName = profileName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.modified = new Date();
		this.description = desc;
	}

	public Type getType() {
		return sourceAndOrSink;
	}

	public void setType(Type sourceAndOrSink) {
		this.modified = new Date();
		this.sourceAndOrSink = sourceAndOrSink;
	}

	public Date getCreated() {
		return created;
	}

	public Date getModified() {
		return modified;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}
}
