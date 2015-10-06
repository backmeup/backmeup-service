package org.backmeup.logic;

import java.util.List;

import org.backmeup.model.Token;
import org.backmeup.model.WorkerInfo;
import org.backmeup.model.WorkerMetric;
import org.backmeup.model.dto.WorkerConfigDTO;

public interface WorkerLogic {
    
//    String register(String workerId, String workerSecret);
//    
//    String delete(String workerId);

    Token authorize(String workerId, String workerSecret);

    WorkerConfigDTO initializeWorker(WorkerInfo workerInfo);
    
    WorkerInfo getWorkerByWorkerId(String workerId);
    WorkerInfo getWorkerByWorkerId(String workerId, boolean throwIfUnknown);

    void addWorkerMetrics(List<WorkerMetric> workerMetrics);

}
