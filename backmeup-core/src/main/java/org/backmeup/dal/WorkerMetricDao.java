package org.backmeup.dal;

import org.backmeup.model.WorkerMetric;

/**
 * The WorkerMetric contains all database relevant operations for the model class WorkerMetric.
 * 
 */
public interface WorkerMetricDao {

    WorkerMetric save(WorkerMetric entity);

}
