package org.backmeup.logic.impl;

import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.dal.AuthDataDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.ProfileDao;
import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.logic.ProfileLogic;
import org.backmeup.model.AuthData;
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
    public Profile getProfile(Long profileId) {
        Profile profile = getProfileDao().findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_PROFILE), profileId));
        }
        return profile;
    }
    
    @Override
    public List<Profile> getProfilesOf(Long userId) {
        return getProfileDao().findDatasourceProfilesByUserId(userId);
    }

    @Override
    public void deleteProfilesOf(Long userId) {
        ProfileDao profileDao = getProfileDao();
        for (Profile p : profileDao.findProfilesByUserId(userId)) {
            profileDao.delete(p);
        }
    }

    @Override
    public void deleteProfile(Long profileId) {
        Profile profile = getProfileDao().findById(profileId);
        getProfileDao().delete(profile);
    }

    // AuthData ---------------------------------------------------------------

    @Override
    public AuthData addAuthData(AuthData authData) {
        if (authData == null) {
            throw new IllegalArgumentException("AuthData must not be null");
        }
        AuthData ad = getAuthDataDao().save(authData);
        ad.setProperties(authData.getProperties());
        
        storeAuthDataOnKeyserver(ad);
        
        return ad;
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
    
    private void storeAuthDataOnKeyserver(AuthData authData) {
        if(authData.getProperties() == null) {
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        for (Entry<String,String> entry : authData.getProperties().entrySet()) {
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append(';');
        }
        String data = sb.toString(); 
        
        try {
            keyserverClient.updatePluginData(null, authData.getPluginId() + ".AUTH", data);
        } catch (KeyserverException e) {
           throw new BackMeUpException("Canot store auth data on keyserver", e);
        }
    }
    
    private void storeProfileOnKeyserver(Profile profile) {
        StringBuilder sb = null;
        String data = "";
        
        // Store profile properties on keyserver
        if(profile.getProperties() != null) {
            sb = new StringBuilder();
            for (Entry<String,String> entry : profile.getProperties().entrySet()) {
                sb.append(entry.getKey());
                sb.append('=');
                sb.append(entry.getValue());
                sb.append(';');
            }
            data = sb.toString(); 
            try {
                keyserverClient.updatePluginData(null, profile.getPluginId() + ".PROPS", data);
            } catch (KeyserverException e) {
                throw new BackMeUpException("Canot store auth data on keyserver", e);
            }
        }
        
        // Store profile options on keyserver
        if(profile.getOptions() != null) {
            sb = new StringBuilder();
            for (String entry : profile.getOptions()) {
                sb.append(entry);
                sb.append(';');
            }
            data = sb.toString(); 
            try {
                keyserverClient.updatePluginData(null, profile.getPluginId() + ".OPTIONS", data);
            } catch (KeyserverException e) {
                throw new BackMeUpException("Canot store auth data on keyserver", e);
            }
        }
    }
}
