package org.backmeup.logic;

import java.util.List;
import java.util.Set;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.spi.SourceSinkDescribable.Type;

/**
 * Profile related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface ProfileLogic {

    Profile save(Profile profile);

    void deleteProfilesOf(String username);

    List<Profile> getProfilesOf(String username);

    Profile deleteProfile(Long profileId, String username);

    Profile getExistingUserProfile(Long profileId, String username);

    Profile queryExistingProfile(Long profileId);

    List<Profile> getDatasinkProfilesOf(String username);

    Set<ProfileOptions> getSourceProfilesOptionsFor(List<Profile> sourceProfiles);

    List<String> getProfileOptions(Long profileId, Set<ProfileOptions> sourceProfiles);

    void setProfileOptions(Long profileId, Set<ProfileOptions> sourceProfiles, List<String> sourceOptions);

    Profile createNewProfile(BackMeUpUser user, String uniqueDescIdentifier, String profileName, Type type);

    void setIdentification(Profile profile, String userId);

}