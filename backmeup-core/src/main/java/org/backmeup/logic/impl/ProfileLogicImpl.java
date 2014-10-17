package org.backmeup.logic.impl;

import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.dal.AuthDataDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.ProfileDao;
import org.backmeup.logic.ProfileLogic;
import org.backmeup.model.AuthData;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;
import org.backmeup.model.spi.PluginDescribable.PluginType;

@ApplicationScoped
public class ProfileLogicImpl implements ProfileLogic {

    private static final String USER_HAS_NO_PROFILE = "org.backmeup.logic.impl.BusinessLogicImpl.USER_HAS_NO_PROFILE";
    private static final String UNKNOWN_PROFILE = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_PROFILE";

    private final ResourceBundle textBundle = ResourceBundle.getBundle("ProfileLogicImpl");

    @Inject
    private DataAccessLayer dal;

    private ProfileDao getProfileDao() {
        return dal.createProfileDao();
    }
    
    private AuthDataDao getAuthDataDao() {
    	return dal.createAuthDataDao();
    }

    @Override
    public void deleteProfilesOf(Long userId) {
        ProfileDao profileDao = getProfileDao();
        for (Profile p : profileDao.findProfilesByUserId(userId)) {
            profileDao.delete(p);
        }
    }

    @Override
    public List<Profile> getProfilesOf(Long userId) {
        return getProfileDao().findDatasourceProfilesByUserId(userId);
    }

    @Override
    public void deleteProfile(Long profileId) {
        Profile profile = getProfileDao().findById(profileId);
        getProfileDao().delete(profile);
    }
    
    @Deprecated
    @Override
    public Profile deleteProfile(Long profileId, Long userId) {
        Profile profile = getExistingUserProfile(profileId, userId);
        getProfileDao().delete(profile);
        return profile;
    }

    @Override
    public Profile getExistingUserProfile(Long profileId, Long userId) {
        Profile profile = queryExistingProfile(profileId);
        if (profile.getUser().getUserId() != userId) {
            throw new IllegalArgumentException(String.format(textBundle.getString(USER_HAS_NO_PROFILE), userId, profileId));
        }
        return profile;
    }

    @Override
    public Profile queryExistingProfile(Long profileId) {
        Profile profile = getProfileDao().findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_PROFILE), profileId));
        }
        return profile;
    }

    @Override
    public List<Profile> getDatasinkProfilesOf(Long userId) {
        return getProfileDao().findDatasinkProfilesByUserId(userId);
    }

    @Override
    public Set<Profile> getSourceProfilesOptionsFor(List<Profile> sourceProfileEntries) {
        if (sourceProfileEntries.size() == 0) {
            throw new IllegalArgumentException("There must be at least one source profile to download data from!");
        }

        Set<Profile> profileOptions = new HashSet<>();
        for (Profile sourceEntry : sourceProfileEntries) {
            Profile sourceProfile = queryExistingProfile(sourceEntry.getId());
            //TODO
//            profileOptions.add(new ProfileOptions(sourceProfile, sourceEntry.getOptions().keySet().toArray(new String[0])));
            profileOptions.add(sourceProfile);
        }
        return profileOptions;
    }

    @Override
    public List<String> getProfileOptions(Long profileId, Profile sourceProfile) {
        Profile po = sourceProfile;
        if (po.getId().equals(profileId)) {
            return po.getOptions();
        }

        throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_PROFILE), profileId));
    }

    @Override
    public void setProfileOptions(Long profileId, Profile sourceProfile, List<String> sourceOptions) {
        queryExistingProfile(profileId);
        
        Profile option = sourceProfile;
        if (option.getId().equals(profileId)) {
            option.setOptions(sourceOptions);
        }
    }
    
    @Override
    public Profile save(Profile profile) {
        return getProfileDao().save(profile);
    }

    @Deprecated
    @Override
    public Profile createNewProfile(BackMeUpUser user, String uniqueDescIdentifier, String profileName, PluginType type) {
        Profile profile = new Profile(user, profileName, uniqueDescIdentifier, type);
        profile = getProfileDao().save(profile);
        return profile;
    }

    @Override
    public void setIdentification(Profile profile, String identification) {
        if (identification != null) {
            profile.setIdentification(identification);
        }
        getProfileDao().save(profile);
    }

	@Override
	public AuthData addAuthData(AuthData authData) {
        if (authData == null) {
            throw new IllegalArgumentException("AuthData must not be null");
        }
		return getAuthDataDao().save(authData);
	}

	@Override
	public AuthData getAuthData(Long authDataId) {
        if (authDataId == null) {
            throw new IllegalArgumentException("AuthDataId must not be null");
        }
        AuthData authData = getAuthDataDao().findById(authDataId);
        if (authData == null) {
            throw new IllegalArgumentException("No auth data found with id: " + authDataId);
        }
        return authData;
	}
	
	@Override
	public List<AuthData> getAuthDataOf(Long userId) {
		if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
		return getAuthDataDao().findAuthDataByUserId(userId);
	}

	@Override
	public void deleteAuthData(Long authDataId) {
		AuthData authData = getAuthData(authDataId);
		getAuthDataDao().delete(authData);
	}

}
