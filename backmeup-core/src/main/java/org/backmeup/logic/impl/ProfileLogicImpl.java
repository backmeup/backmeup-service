package org.backmeup.logic.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.ProfileDao;
import org.backmeup.logic.ProfileLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.dto.SourceProfileEntry;
import org.backmeup.model.spi.SourceSinkDescribable.Type;

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

    @Override
    public Profile save(Profile profile) {
        return getProfileDao().save(profile);
    }

    @Override
    public void deleteAllProfilesOfUser(String username) {
        ProfileDao profileDao = getProfileDao();
        for (Profile p : profileDao.findProfilesByUsername(username)) {
            profileDao.delete(p);
        }
    }

    @Override
    public List<Profile> findAllProfilesOfUser(String username) {
        return getProfileDao().findDatasourceProfilesByUsername(username);
    }

    @Override
    public Profile deleteExistingUserProfile(Long profileId, String username) {
        Profile profile = queryExistingUserProfile(profileId, username);
        getProfileDao().delete(profile);
        return profile;
    }

    @Override
    public Profile queryExistingUserProfile(Long profileId, String username) {
        Profile profile = queryExistingProfile(profileId);
        if (!profile.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException(String.format(textBundle.getString(USER_HAS_NO_PROFILE), username, profileId));
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
    public List<Profile> findDatasinkProfilesOfUser(String username) {
        return getProfileDao().findDatasinkProfilesByUsername(username);
    }

    @Override
    public Set<ProfileOptions> getSourceProfilesOptionsFor(List<SourceProfileEntry> sourceProfileEntries) {
        if (sourceProfileEntries.size() == 0) {
            throw new IllegalArgumentException("There must be at least one source profile to download data from!");
        }

        Set<ProfileOptions> profileOptions = new HashSet<>();
        for (SourceProfileEntry sourceEntry : sourceProfileEntries) {
            Profile sourceProfile = queryExistingProfile(sourceEntry.getId());
            profileOptions.add(new ProfileOptions(sourceProfile, sourceEntry.getOptions().keySet().toArray(new String[0])));
        }
        return profileOptions;
    }

    @Override
    public List<String> getProfileOptions(Long profileId, Set<ProfileOptions> sourceProfiles) {
        for (ProfileOptions po : sourceProfiles) {
            if (po.getProfile().getProfileId().equals(profileId)) {
                String[] options = po.getOptions();
                if (options == null) {
                    return new ArrayList<>();
                }
                return Arrays.asList(options);
            }
        }

        throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_PROFILE), profileId));
    }

    @Override
    public void setProfileOptions(Long profileId, Set<ProfileOptions> sourceProfiles, List<String> sourceOptions) {
        for (ProfileOptions option : sourceProfiles) {
            if (option.getProfile().getProfileId().equals(profileId)) {
                String[] new_options = sourceOptions.toArray(new String[sourceOptions.size()]);
                option.setOptions(new_options);
            }
        }
    }

    @Override
    public Profile createNewProfile(BackMeUpUser user, String uniqueDescIdentifier, String profileName, Type type) {
        Profile profile = new Profile(user, profileName, uniqueDescIdentifier, type);
        profile = getProfileDao().save(profile);
        return profile;
    }

    @Override
    public void setIdentification(Profile profile, String userId) {
        if (userId != null) {
            profile.setIdentification(userId);
        }
        getProfileDao().save(profile);
    }

}
