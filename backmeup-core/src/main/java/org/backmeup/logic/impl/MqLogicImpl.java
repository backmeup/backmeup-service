package org.backmeup.logic.impl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.keyserver.client.Keyserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MqLogicImpl {

    @Inject
    @Configuration(key = "backmeup.index.host")
    private String indexHost;

    @Inject
    @Configuration(key = "backmeup.index.port")
    private Integer indexPort;

    @Inject
    private Keyserver keyserverClient;

    @Inject
    private DataAccessLayer dal;

    @Inject
    @Configuration(key = "backmeup.message.queue.host")
    private String mqHost;

    @Inject
    @Configuration(key = "backmeup.message.queue.name")
    private String mqName;

    @Inject
    @Configuration(key = "backmeup.message.queue.receivers")
    private Integer numberOfJobWorker;

    @Inject
    @Configuration(key = "backmeup.job.backupname")
    private String backupName;

    @Inject
    @Configuration(key = "backmeup.job.temporaryDirectory")
    private String jobTempDir;

//    private List<RabbitMQJobReceiver> jobWorker;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @PostConstruct
    public void startup() {
        logger.info("Starting job workers");
//        try {
//            jobWorker = new ArrayList<>();
//            for (int i = 0; i < numberOfJobWorker; i++) {
//                RabbitMQJobReceiver rec = new RabbitMQJobReceiver(mqHost,
//                        mqName, indexHost, indexPort, backupName, jobTempDir,
//                        plugins, keyserverClient, dal);
//                rec.start();
//                jobWorker.add(rec);
//            }
//        } catch (Exception e) {
//            logger.error("Error while starting job receivers", e);
//        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down job workers!");
//        for (RabbitMQJobReceiver receiver : jobWorker) {
//            receiver.stop();
//        }
    }

}
