package org.backmeup.dal.jpa;

import javax.persistence.EntityManager;

import org.backmeup.dal.WorkerMetricDao;
import org.backmeup.model.WorkerMetric;

/**
 * The WorkerMetricDaoImpl realizes the WorkerMetricDao interface with JPA
 * specific operations.
 * 
 */
public class WorkerMetricDaoImpl implements WorkerMetricDao {
    protected EntityManager em;

    public WorkerMetricDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public WorkerMetric save(WorkerMetric entity) {
        return em.merge(entity);
    }
}
