package org.backmeup.logic;

import java.util.List;
import java.util.Set;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.spi.PluginDescribable.PluginType;

/**
 * Profile related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface ProfileLogic {

    Profile save(Profile profile);

    void deleteProfilesOf(Long userId);

    List<Profile> getProfilesOf(Long userId);

    Profile deleteProfile(Long profileId, Long userId);

    Profile getExistingUserProfile(Long profileId, Long userId);

    Profile queryExistingProfile(Long profileId);

    List<Profile> getDatasinkProfilesOf(Long userId);

    Set<ProfileOptions> getSourceProfilesOptionsFor(List<Profile> sourceProfiles);

    List<String> getProfileOptions(Long profileId, ProfileOptions sourceProfile);

    void setProfileOptions(Long profileId, ProfileOptions sourceProfiles, List<String> sourceOptions);

    Profile createNewProfile(BackMeUpUser user, String uniqueDescIdentifier, String profileName, PluginType type);

    void setIdentification(Profile profile, String identification);

}