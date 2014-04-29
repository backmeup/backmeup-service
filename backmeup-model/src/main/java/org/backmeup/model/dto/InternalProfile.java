package org.backmeup.model.dto;

public class InternalProfile {

	private String title;
	private String identification;
	private String pluginName;
	private long profileId;
	private long createDate;
	private long modifyDate;

	public InternalProfile() {
	}

	public InternalProfile(String title, long profileId, String pluginName,
			long createDate, long modifyDate, String identification) {
		this.title = title;
		this.profileId = profileId;
		this.pluginName = pluginName;
		this.createDate = createDate;
		this.modifyDate = modifyDate;
		this.identification = identification;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getProfileId() {
		return profileId;
	}

	public void setProfileId(long profileId) {
		this.profileId = profileId;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public long getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(long modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}
}