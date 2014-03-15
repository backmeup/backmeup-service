package org.backmeup.dal;

import org.backmeup.model.BackMeUpUser;

/**
 * The UserDao contains all database relevant operations for the model class User.
 * 
 * @author fschoeppl
 */
public interface UserDao extends BaseDao<BackMeUpUser> {

    BackMeUpUser findByName(String username);

    BackMeUpUser findByVerificationKey(String verificationKey);

    BackMeUpUser findByEmail(String email);

}
