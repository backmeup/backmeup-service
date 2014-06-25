package org.backmeup.logic;

import org.backmeup.model.BackMeUpUser;

/**
 * User registration related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface UserRegistration {

    BackMeUpUser getUserByUsername(String username, boolean ensureActivated);
	
	BackMeUpUser getUserByUserId(String userId);
    
    BackMeUpUser getUserByUserId(String userId, boolean ensureActivated);
    
    BackMeUpUser register(BackMeUpUser user);
    
    BackMeUpUser update(BackMeUpUser user);
    
    void delete(BackMeUpUser user);

    void setNewVerificationKeyTo(BackMeUpUser user);

    void sendVerificationEmailFor(BackMeUpUser user);

    BackMeUpUser requestNewVerificationEmail(String username);

    BackMeUpUser activateUserFor(String verificationKey);

    void ensureNewValuesAvailable(BackMeUpUser user, String newUsername, String newEmail);
}