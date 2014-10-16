package org.backmeup.dal.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.backmeup.dal.AuthDataDao;
import org.backmeup.model.AuthData;

public class AuthDataDaoImpl extends BaseDaoImpl<AuthData> implements AuthDataDao {

	public AuthDataDaoImpl(EntityManager em) {
		super(em);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AuthData> findAuthDataByUserId(Long userId) {
		Query q = em.createQuery("SELECT a FROM " + entityClass.getName() +" a WHERE a.user.userId = :userId");
	    q.setParameter("userId", userId);            
		List<AuthData> auths = q.getResultList();
		return auths;
	}
}
