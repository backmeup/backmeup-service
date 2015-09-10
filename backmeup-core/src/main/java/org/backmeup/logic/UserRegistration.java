package org.backmeup.logic;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Token;

/**
 * User registration related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface UserRegistration {

    BackMeUpUser getUserByUsername(String username, boolean ensureActivated);

    BackMeUpUser getUserByUserId(Long userId);
    
    BackMeUpUser getUserByKeyserverUserId(String keyserverUserId);

    BackMeUpUser getUserByUserId(Long userId, boolean ensureActivated);

    BackMeUpUser register(BackMeUpUser user);
    
    BackMeUpUser registerAnonymous(BackMeUpUser activeUser);

    BackMeUpUser update(BackMeUpUser user);

    void delete(BackMeUpUser user);

    Token authorize(BackMeUpUser user, String password);

    Token authorize(String activationCode);

    void setNewVerificationKeyTo(BackMeUpUser user);

    void sendVerificationEmailFor(BackMeUpUser user);

    BackMeUpUser requestNewVerificationEmail(String username);

    BackMeUpUser activateUserFor(String verificationKey);

    void ensureNewValuesAvailable(BackMeUpUser user, String newUsername, String newEmail);

    String getActivationCode(BackMeUpUser currentUser, BackMeUpUser anonUser);

}
