package org.backmeup.dal.jpa;

import javax.persistence.EntityManager;

import org.backmeup.dal.AuthDataDao;
import org.backmeup.model.AuthData;

public class AuthDataDaoImpl extends BaseDaoImpl<AuthData> implements AuthDataDao {

	public AuthDataDaoImpl(EntityManager em) {
		super(em);
	}
}
