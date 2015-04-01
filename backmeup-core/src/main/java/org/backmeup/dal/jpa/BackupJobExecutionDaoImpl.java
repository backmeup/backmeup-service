package org.backmeup.dal.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.backmeup.dal.BackupJobExecutionDao;
import org.backmeup.model.BackupJobExecution;

public class BackupJobExecutionDaoImpl extends BaseDaoImpl<BackupJobExecution> implements BackupJobExecutionDao {

    public BackupJobExecutionDaoImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<BackupJobExecution> findByUserId(Long userId) {
        TypedQuery<BackupJobExecution> q = em.createQuery(
                "SELECT j FROM " + entityClass.getName() + " j WHERE j.user.userId = :userId", entityClass);
        q.setParameter("userId", userId);
        List<BackupJobExecution> jobs = q.getResultList();
        return jobs;
    }
    
    @Override
    public List<BackupJobExecution> findAll() {
        TypedQuery<BackupJobExecution> q = em.createQuery(
                "SELECT j FROM " + entityClass.getName() + " j", entityClass);
        List<BackupJobExecution> jobs = q.getResultList();
        return jobs;
    }

}
