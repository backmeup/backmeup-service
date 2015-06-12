package org.backmeup.dal.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.model.BackupJob;

public class BackupJobDaoImpl extends BaseDaoImpl<BackupJob> implements BackupJobDao {

    public BackupJobDaoImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<BackupJob> findByUserId(Long userId) {
        TypedQuery<BackupJob> q = em.createQuery("SELECT j FROM " + entityClass.getName() +" j WHERE j.user.userId = :userId", entityClass);
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    @Override
    public List<BackupJob> findAll() {
        TypedQuery<BackupJob> q = em.createQuery("SELECT j FROM " + entityClass.getName() +" j", entityClass);
        return q.getResultList();   
    }
}
