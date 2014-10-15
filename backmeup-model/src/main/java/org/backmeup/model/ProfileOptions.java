package org.backmeup.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * The ProfileOptions class wraps a profile and items that shall be backed up.
 * E.g. options[0] = "My Data\Folder1" which means that this folder should be
 * backed up.
 * 
 * @author fschoeppl
 * 
 */
@Entity
public class ProfileOptions {
	@Id
	@GeneratedValue
	private Long optionId;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	private Profile profile;
	private String[] options;

	public ProfileOptions() {
	}

	public ProfileOptions(Profile profile, String[] options) {
		this.profile = profile;
		this.options = options;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

	public Long getOptionId() {
		return optionId;
	}

	public void setOptionId(Long optionId) {
		this.optionId = optionId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		if (obj instanceof ProfileOptions) {
			ProfileOptions b = (ProfileOptions) obj;
			if (this.getOptionId() != b.getOptionId()) {
				return false;
			}
			if (this.getOptions().length != b.getOptions().length) {
				return false;
			}
			for (int i = 0; i < this.getOptions().length; i++) {
				if (!this.getOptions()[i].equals(b.getOptions()[i])) {
					return false;
				}
			}
			// overwritten equals Method for Profile
			if (!this.getProfile().equals(b.getProfile())) {
				return false;
			}

		} else {
			return false;
		}
		return true;
	}
}