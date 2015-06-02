package org.backmeup.logic.impl;

import java.util.List;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.dal.AuthDataDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.ProfileDao;
import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.keyserver.model.Token.Kind;
import org.backmeup.keyserver.model.dto.TokenDTO;
import org.backmeup.logic.ProfileLogic;
import org.backmeup.model.AuthData;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;
import org.backmeup.model.exceptions.BackMeUpException;

@ApplicationScoped
public class ProfileLogicImpl implements ProfileLogic {

    private static final String UNKNOWN_PROFILE = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_PROFILE";

    private final ResourceBundle textBundle = ResourceBundle.getBundle("ProfileLogicImpl");
    
    @Inject
    private KeyserverClient keyserverClient;

    @Inject
    private DataAccessLayer dal;

    private ProfileDao getProfileDao() {
        return dal.createProfileDao();
    }

    private AuthDataDao getAuthDataDao() {
        return dal.createAuthDataDao();
    }
    
    // Profiles ---------------------------------------------------------------
    
    @Override
    public Profile saveProfile(Profile p) {
        Profile profile = getProfileDao().save(p);
        profile.setUser(p.getUser());
        profile.setProperties(p.getProperties());
        profile.setOptions(p.getOptions());
        storeProfileOnKeyserver(profile);
        return profile;
    }

    @Override
    public Profile updateProfile(Profile p) {
        Profile profile =  getProfileDao().merge(p);
        storeProfileOnKeyserver(profile);
        return profile;
    }
    
    @Override
    public Profile getProfile(BackMeUpUser currentUser, Long profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("ProfileId must not be null");
        }
        
        Profile profile = getProfileDao().findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_PROFILE), profileId));
        }
        
        try {
            TokenDTO token = new TokenDTO(Kind.INTERNAL, currentUser.getPassword());
            String encodedData = keyserverClient.getPluginData(token, profile.getId().toString());
            profile.setPropertiesAndOptionsFromEncodedString(encodedData);
        } catch (KeyserverException e) {
           throw new BackMeUpException("Canot store auth data on keyserver", e);
        }
        
        return profile;
    }
    
    @Override
    public List<Profile> getProfilesOf(Long userId) {
        return getProfileDao().findDatasourceProfilesByUserId(userId);
    }

    @Override
    public void deleteProfilesOf(BackMeUpUser currentUser, Long userId) {
        ProfileDao profileDao = getProfileDao();
        for (Profile p : profileDao.findProfilesByUserId(userId)) {
            profileDao.delete(p);
            deleteProfileOnKeyserver(currentUser, p);
        }
    }

    @Override
    public void deleteProfile(BackMeUpUser currentUser, Long profileId) {
        Profile profile = getProfileDao().findById(profileId);
        getProfileDao().delete(profile);
        deleteProfileOnKeyserver(currentUser, profile);
    }

    // AuthData ---------------------------------------------------------------

    @Override
    public AuthData addAuthData(AuthData authData) {
        if (authData == null) {
            throw new IllegalArgumentException("AuthData must not be null");
        }
        AuthData ad = getAuthDataDao().save(authData);
        ad.setUser(authData.getUser());
        ad.setProperties(authData.getProperties());
        
        storeAuthDataOnKeyserver(ad);
        
        return ad;
    }

    @Override
    public AuthData getAuthData(BackMeUpUser currentUser, Long authDataId) {
        if (authDataId == null) {
            throw new IllegalArgumentException("AuthDataId must not be null");
        }
        AuthData authData = getAuthDataDao().findById(authDataId);
        if (authData == null) {
            throw new IllegalArgumentException("No auth data found with id: " + authDataId);
        }
        
        try {
            TokenDTO token = new TokenDTO(Kind.INTERNAL, currentUser.getPassword());
            String encodedData = keyserverClient.getPluginData(token, authData.getId().toString());
            authData.setPropertiesFromEncodedString(encodedData);
        } catch (KeyserverException e) {
           throw new BackMeUpException("Canot store auth data on keyserver", e);
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
    public void deleteAuthData(BackMeUpUser currentUser, Long authDataId) {
        AuthData authData = getAuthData(currentUser, authDataId);
        getAuthDataDao().delete(authData);
        
        deleteAuthDataOnKeyserver(currentUser, authData);
    }
    
    private void storeAuthDataOnKeyserver(AuthData authData) {                
        try {
            String data = authData.getPropertiesAsEncodedString();
            TokenDTO token = new TokenDTO(Kind.INTERNAL,authData.getUser().getPassword());
            keyserverClient.createPluginData(token, authData.getId().toString(), data);
        } catch (KeyserverException e) {
           throw new BackMeUpException("Canot store auth data on keyserver", e);
        }
    }
    
    private void deleteAuthDataOnKeyserver(BackMeUpUser currentUser, AuthData authData) {
        try {
            keyserverClient.removePluginData(new TokenDTO(Kind.INTERNAL,currentUser.getPassword()), authData.getId().toString());
        } catch (KeyserverException e) {
            throw new BackMeUpException("Canot delete auth data on keyserver",e);
        }
    }
    
    private void storeProfileOnKeyserver(Profile profile) {                
        try {
            String data = profile.getPropertiesAndOptionsAsEncodedString();
            TokenDTO token = new TokenDTO(Kind.INTERNAL,profile.getUser().getPassword());
            keyserverClient.createPluginData(token, profile.getId().toString(), data);
        } catch (KeyserverException e) {
           throw new BackMeUpException("Canot store profile on keyserver", e);
        }
    }
    
    private void deleteProfileOnKeyserver(BackMeUpUser currentUser, Profile profile) {
        try {
            keyserverClient.removePluginData(new TokenDTO(Kind.INTERNAL,currentUser.getPassword()), profile.getId().toString());
        } catch (KeyserverException e) {
            throw new BackMeUpException("Canot delete profile on keyserver",e);
        }
    }
}
