package org.backmeup.rest.resources;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.model.dto.WorkerConfigDTO;
import org.backmeup.model.dto.WorkerConfigDTO.DistributionMechanism;
import org.backmeup.model.dto.WorkerInfoDTO;


@Path("/workers")
public class Workers extends Base {
    @Context
    private SecurityContext securityContext;
    
    @Inject
    @Configuration(key = "backmeup.message.queue.host")
    private String mqHost;

    @Inject
    @Configuration(key = "backmeup.message.queue.name")
    private String mqName;
    
    @Inject
    @Configuration(key = "backmeup.job.backupname")
    private String backupNameTemplate;
    
    @RolesAllowed({"worker"})
    @PUT
    @Path("/hello")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WorkerConfigDTO initializeWorker(WorkerInfoDTO workerInfo) {
        WorkerConfigDTO workerConfig = new WorkerConfigDTO();
        workerConfig.setDistributionMechanism(DistributionMechanism.QUEUE);
        workerConfig.setConnectionInfo(mqHost + ";" + mqName);
        workerConfig.setBackupNameTemplate(backupNameTemplate);
        
        return workerConfig;
    }
}
