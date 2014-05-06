package org.backmeup.logic;

import org.backmeup.model.BackMeUpUser;

/**
 * User registration related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface UserRegistration {

    BackMeUpUser getExistingUser(String username);

    void ensureUserIsActive(String username);
    
    BackMeUpUser getActiveUser(String username);
    
    BackMeUpUser register(String username, String email);

    void setNewVerificationKeyTo(BackMeUpUser user);

    void sendVerificationEmailFor(BackMeUpUser user);

    BackMeUpUser requestNewVerificationEmail(String username);

    BackMeUpUser activateUserFor(String verificationKey);

    void ensureNewValuesAvailable(BackMeUpUser user, String newUsername, String newEmail);

    void updateValues(BackMeUpUser user, String newUsername, String newEmail);

    void delete(BackMeUpUser user);

}