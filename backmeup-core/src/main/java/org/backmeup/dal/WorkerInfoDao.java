package org.backmeup.dal;

import org.backmeup.model.WorkerInfo;

/**
 * The WorkerInfoDao contains all database relevant operations for the model class WorkerInfo.
 * 
 */
public interface WorkerInfoDao extends BaseDao<WorkerInfo> {

    WorkerInfo findById(String workerId);

}
