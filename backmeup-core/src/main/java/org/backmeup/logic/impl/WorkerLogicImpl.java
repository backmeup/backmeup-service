package org.backmeup.logic.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.WorkerInfoDao;
import org.backmeup.dal.WorkerMetricDao;
import org.backmeup.logic.WorkerLogic;
import org.backmeup.model.WorkerInfo;
import org.backmeup.model.WorkerMetric;
import org.backmeup.model.constants.WorkerState;
import org.backmeup.model.dto.WorkerConfigDTO;
import org.backmeup.model.dto.WorkerConfigDTO.DistributionMechanism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker management related business logic.
 */
@ApplicationScoped
public class WorkerLogicImpl implements WorkerLogic{
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerLogicImpl.class);

    @Inject
    @Configuration(key = "backmeup.message.queue.host")
    private String mqHost;

    @Inject
    @Configuration(key = "backmeup.message.queue.name")
    private String mqName;

    @Inject
    @Configuration(key = "backmeup.job.backupname")
    private String backupNameTemplate;

    @Inject
    @Configuration(key = "backmeup.osgi.exportedPackages")
    private String pluginsExportedPackages;

    @Inject
    private DataAccessLayer dal;

    private WorkerInfoDao getWorkerInfoDao() {
        return dal.createWorkerInfoDao();
    }
    
    private WorkerMetricDao getWorkerMetricDao() {
        return dal.createWorkerMetricDao();
    }

    @Override
    public WorkerConfigDTO initializeWorker(WorkerInfo workerInfo) {

        WorkerInfo worker = getWorkerInfoDao().findById(workerInfo.getWorkerId());
        if (worker == null) {
            worker = addWorkerInfo(workerInfo);
            LOGGER.info(String.format("Added new worker with id=%s", worker.getWorkerId()));
        } else {
            worker = getWorkerInfoDao().merge(workerInfo);
        }
        
        worker.setState(WorkerState.OFFLINE);
        worker.setLastHeartbeatNow();
        worker = getWorkerInfoDao().merge(worker);

        WorkerConfigDTO workerConfig = new WorkerConfigDTO();
        workerConfig.setDistributionMechanism(DistributionMechanism.QUEUE);
        workerConfig.setConnectionInfo(mqHost + ";" + mqName);
        workerConfig.setBackupNameTemplate(backupNameTemplate);
        workerConfig.setPluginsExportedPackages(pluginsExportedPackages);

        return workerConfig;
    }

    private WorkerInfo addWorkerInfo(WorkerInfo workerInfo) {
        if (workerInfo.getWorkerId() == null) {
            throw new IllegalArgumentException("WorkerId must not be null");
        }

        if (workerInfo.getWorkerName() == null) {
            throw new IllegalArgumentException("WorkerName must not be null");
        }

        return getWorkerInfoDao().save(workerInfo);
    }

    @Override
    public void addWorkerMetrics(List<WorkerMetric> workerMetrics) {
        for (WorkerMetric m : workerMetrics) {
            getWorkerMetricDao().save(m);
        }
    }

}
