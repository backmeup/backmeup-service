package org.backmeup.dal.jpa;

import javax.persistence.EntityManager;

import org.backmeup.dal.WorkerInfoDao;
import org.backmeup.model.WorkerInfo;

/**
 * The WorkerInfoDaoImpl realizes the WorkerInfoDao interface with 
 * JPA specific operations.
 * 
 */
public class WorkerInfoDaoImpl extends BaseDaoImpl<WorkerInfo> implements WorkerInfoDao {

    public WorkerInfoDaoImpl(EntityManager em) {
        super(em);
    }
    
    @Override
    public WorkerInfo findById(long id) {
        return findById(String.valueOf(id));
    }

    @Override
    public WorkerInfo findById(String workerId) {
        return em.find(entityClass, workerId);
    }
}
