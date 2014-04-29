package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;
import org.backmeup.model.dto.InternalProfile;

@XmlRootElement
public class DatasinkProfilesContainer {

	private UserContainer user;

	private List<InternalProfile> sinkProfiles;

	public DatasinkProfilesContainer() {
	}

	public DatasinkProfilesContainer(List<Profile> profiles, BackMeUpUser user) {
		setSinkProfiles(new ArrayList<InternalProfile>());
		for (Profile p : profiles) {
			getSinkProfiles().add(
					new InternalProfile(p.getProfileName(), p.getProfileId(), p
							.getDescription(), p.getCreated().getTime(), p
							.getModified().getTime(), p.getIdentification()));
		}
		this.user = new UserContainer(user);
	}

	public UserContainer getUser() {
		return user;
	}

	public void setUser(UserContainer user) {
		this.user = user;
	}

	public List<InternalProfile> getSinkProfiles() {
		return sinkProfiles;
	}

	public void setSinkProfiles(List<InternalProfile> sourceProfiles) {
		this.sinkProfiles = sourceProfiles;
	}
}
