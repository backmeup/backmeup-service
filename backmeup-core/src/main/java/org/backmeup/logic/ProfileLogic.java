package org.backmeup.logic;

import java.util.List;

import org.backmeup.model.AuthData;
import org.backmeup.model.Profile;

/**
 * Profile related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface ProfileLogic {

    Profile saveProfile(Profile profile);

    List<Profile> getProfilesOf(Long userId);
        
    Profile getProfile(Long profileId);
    
	Profile updateProfile(Profile profile);
    
    void deleteProfilesOf(Long userId);
    
    void deleteProfile(Long profileId);


    AuthData addAuthData(AuthData authData);
    
    AuthData getAuthData(Long authDataId);
    
    List<AuthData> getAuthDataOf(Long userId);
    
    void deleteAuthData(Long authDataId);
    
}