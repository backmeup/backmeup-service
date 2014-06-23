package org.backmeup.dal;

import org.backmeup.model.User;

/**
 * The UserDao contains all database relevant operations for the model class User.
 * 
 * @author fschoeppl
 */
public interface UserDao extends BaseDao<User> {

	User findByName(String username);

	User findByVerificationKey(String verificationKey);

	User findByEmail(String email);

}
