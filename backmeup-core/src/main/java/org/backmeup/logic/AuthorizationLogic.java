package org.backmeup.logic;

import java.util.List;
import java.util.Properties;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;

/**
 * Authorization related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface AuthorizationLogic {

    void register(BackMeUpUser user);

    void unregister(BackMeUpUser user);

    void authorize(BackMeUpUser user, String password);

//    void updatePasswords(BackMeUpUser user, String oldPassword, String newPassword, String oldKeyRingPassword, String newKeyRingPassword);

    Properties getProfileAuthInformation(Profile profile, String keyRing);

    @Deprecated void initProfileAuthInformation(Profile profile, Properties entries, String keyRing);

    void overwriteProfileAuthInformation(Profile profile, Properties entries, String keyRing);

    @Deprecated void appendProfileAuthInformation(Profile profile, Properties entries, String keyRing);

    Properties fetchProfileAuthenticationData(Profile profile, String keyRingPassword);

    @Deprecated List<KeyserverLog> getLogs(BackMeUpUser user);

}