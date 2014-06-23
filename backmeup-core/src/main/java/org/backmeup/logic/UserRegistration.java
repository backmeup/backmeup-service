package org.backmeup.logic;

import org.backmeup.model.User;

/**
 * User registration related business logic.
 * 
 * @author <a href="http://www.code-cop.org/">Peter Kofler</a>
 */
public interface UserRegistration {

	User getExistingUser(String username);

    void ensureUserIsActive(String username);
    
    User getActiveUser(String username);
    
    User register(String username, String email);

    void setNewVerificationKeyTo(User user);

    void sendVerificationEmailFor(User user);

    User requestNewVerificationEmail(String username);

    User activateUserFor(String verificationKey);

    void ensureNewValuesAvailable(User user, String newUsername, String newEmail);

    void updateValues(User user, String newUsername, String newEmail);

    void delete(User user);

}