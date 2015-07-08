package org.backmeup.logic;

import java.util.List;

import org.backmeup.model.WorkerInfo;
import org.backmeup.model.WorkerMetric;
import org.backmeup.model.dto.WorkerConfigDTO;

public interface WorkerLogic {

    WorkerConfigDTO initializeWorker(WorkerInfo workerInfo);

    void addWorkerMetrics(List<WorkerMetric> workerMetrics);

}
