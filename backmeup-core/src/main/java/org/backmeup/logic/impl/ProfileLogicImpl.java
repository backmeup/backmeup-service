package org.backmeup.logic.impl;

import java.util.List;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.dal.AuthDataDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.ProfileDao;
import org.backmeup.logic.ProfileLogic;
import org.backmeup.model.AuthData;
import org.backmeup.model.Profile;

@ApplicationScoped
public class ProfileLogicImpl implements ProfileLogic {

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

    @Override
    public Profile getProfile(Long profileId) {
        Profile profile = getProfileDao().findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_PROFILE), profileId));
        }
        return profile;
    }

    @Override
    public Profile save(Profile profile) {
        // TODO: Store (auth) data in keyserver
        // authorization.overwriteProfileAuthInformation(p, props, profile.getUser().getPassword());

        return getProfileDao().save(profile);
    }

    @Override
    public Profile updateProfile(Profile profile) {
        return getProfileDao().merge(profile);
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
