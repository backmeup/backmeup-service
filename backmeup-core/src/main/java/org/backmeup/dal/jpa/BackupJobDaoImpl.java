package org.backmeup.dal.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.model.BackupJob;
import org.backmeup.model.JobProtocol;

public class BackupJobDaoImpl extends BaseDaoImpl<BackupJob> implements
    BackupJobDao {

  public BackupJobDaoImpl(EntityManager em) {
    super(em);
  }

  @Override
  public List<BackupJob> findByUserId(Long userId) {
    //TODO: Change all queries to named parameter (instead of numbered)
    TypedQuery<BackupJob> q = em.createQuery("SELECT j FROM " + entityClass.getName() +" j WHERE j.user.userId = :userId", entityClass);
    q.setParameter("userId", userId);
    List<BackupJob> jobs = q.getResultList();   
    return jobs;
  }

  @Override
  public List<BackupJob> findAll() {
    TypedQuery<BackupJob> q = em.createQuery("SELECT j FROM " + entityClass.getName() +" j", entityClass);    
    List<BackupJob> jobs = q.getResultList();   
    return jobs;
  }

  @Override
  public BackupJob findLastBackupJob(Long userid) {
    TypedQuery<BackupJob> q = em.createQuery("SELECT jp.job FROM " + JobProtocol.class.getName() +" jp WHERE jp.user.userId = :userId ORDER BY jp.executionTime DESC", entityClass);
    q.setParameter("userId", userid);
    q.setMaxResults(1);
    List<BackupJob> jobs = q.getResultList();
    if (jobs.size() > 0) {
      return jobs.get(0);
    }
    return null;
  } 

}
