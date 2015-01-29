package org.backmeup.logic;

import java.util.Properties;

import org.backmeup.model.BackMeUpUser;
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

    Properties getProfileAuthInformation(Profile profile, String keyRing);

    void overwriteProfileAuthInformation(Profile profile, Properties entries, String keyRing);

    Properties fetchProfileAuthenticationData(Profile profile, String keyRingPassword);

}