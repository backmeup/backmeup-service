package org.backmeup.dal;

import java.util.List;

import org.backmeup.model.AuthData;

/**
 * The AuthDataDao contains all database relevant operations for the model class AuthData.
 */
public interface AuthDataDao extends BaseDao<AuthData> {

    List<AuthData> findAuthDataByUserId(Long userId);
}
