package org.backmeup.logic;

import java.util.List;
import java.util.Set;

import org.backmeup.model.AuthData;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;
import org.backmeup.model.spi.PluginDescribable.PluginType;

/**
 * Profile related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface ProfileLogic {

    Profile save(Profile profile);
    
	Profile updateProfile(Profile profile);
    
    @Deprecated Profile createNewProfile(BackMeUpUser user, String uniqueDescIdentifier, String profileName, PluginType type);

    List<Profile> getProfilesOf(Long userId);
    
    Profile getExistingUserProfile(Long profileId, Long userId);
    
    Profile queryExistingProfile(Long profileId);
    
    @Deprecated void deleteProfilesOf(Long userId);
    
    void deleteProfile(Long profileId);

    Profile deleteProfile(Long profileId, Long userId);

    List<Profile> getDatasinkProfilesOf(Long userId);

    Set<Profile> getSourceProfilesOptionsFor(List<Profile> sourceProfiles);

    List<String> getProfileOptions(Long profileId, Profile sourceProfile);

    void setProfileOptions(Long profileId, Profile sourceProfiles, List<String> sourceOptions);

    @Deprecated void setIdentification(Profile profile, String identification);
    
    AuthData addAuthData(AuthData authData);
    
    AuthData getAuthData(Long authDataId);
    
    List<AuthData> getAuthDataOf(Long userId);
    
    void deleteAuthData(Long authDataId);

}