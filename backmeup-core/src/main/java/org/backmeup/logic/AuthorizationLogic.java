package org.backmeup.logic;

import java.util.List;
import java.util.Properties;

import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;
import org.backmeup.model.User;

/**
 * Authorization related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface AuthorizationLogic {

    void register(User user, String password, String keyRingPassword);

    void unregister(User user);

    void authorize(User user, String keyRingPassword);

    void updatePasswords(User user, String oldPassword, String newPassword, String oldKeyRingPassword, String newKeyRingPassword);

    Properties getProfileAuthInformation(Profile profile, String keyRing);

    void initProfileAuthInformation(Profile profile, Properties entries, String keyRing);

    void overwriteProfileAuthInformation(Profile profile, Properties entries, String keyRing);

    void appendProfileAuthInformation(Profile profile, Properties entries, String keyRing);

    Properties fetchProfileAuthenticationData(Profile profile, String keyRingPassword);

    List<KeyserverLog> getLogs(User user);

}