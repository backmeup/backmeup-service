package org.backmeup.logic.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.WorkerInfoDao;
import org.backmeup.dal.WorkerMetricDao;
import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.keyserver.model.dto.AuthResponseDTO;
import org.backmeup.logic.WorkerLogic;
import org.backmeup.model.Token;
import org.backmeup.model.WorkerInfo;
import org.backmeup.model.WorkerMetric;
import org.backmeup.model.constants.WorkerState;
import org.backmeup.model.dto.WorkerConfigDTO;
import org.backmeup.model.dto.WorkerConfigDTO.DistributionMechanism;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.UnknownWorkerException;
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
    private KeyserverClient keyserverClient;

    @Inject
    private DataAccessLayer dal;

    private WorkerInfoDao getWorkerInfoDao() {
        return dal.createWorkerInfoDao();
    }
    
    private WorkerMetricDao getWorkerMetricDao() {
        return dal.createWorkerMetricDao();
    }
    
    @Override
    public Token authorize(String workerId, String workerSecret) {
        try {
            AuthResponseDTO response = keyserverClient.authenticateApp(workerId, workerSecret);
            String token = response.getToken().getB64Token();
            
            // AuthResponse for authenticated APPs does not contain a TTL.
            // Therefore, make Token valid for 'today + 1 Year'.
            Date ttl = new Date();
            Calendar c = Calendar.getInstance(); 
            c.setTime(ttl); 
            c.add(Calendar.YEAR, 1);
            ttl = c.getTime();
            
            return new Token(token, ttl.getTime());
        } catch (KeyserverException ex) {
            LOGGER.warn("Cannot authenticate on keyserver", ex);
            throw new InvalidCredentialsException();
        }
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

    @Override
    public WorkerInfo getWorkerByWorkerId(String workerId) {
        return getWorkerByWorkerId(workerId, false);
    }
    
    @Override
    public WorkerInfo getWorkerByWorkerId(String workerId, boolean throwIfUnknown) {
        WorkerInfo worker = getWorkerInfoDao().findById(workerId);
        if (throwIfUnknown && worker == null) {
            throw new UnknownWorkerException(workerId);
        }
        return worker;
    }

}
