package org.backmeup.rest.resources;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.model.dto.WorkerInfoDTO;
import org.backmeup.model.dto.WorkerInfoResponseDTO;
import org.backmeup.model.dto.WorkerInfoResponseDTO.DistributionMechnaism;


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

    @RolesAllowed({"worker"})
    @PUT
    @Path("/{workerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkerInfoResponseDTO updateWorkerInfo(@PathParam("workerId") String workerId, WorkerInfoDTO workerInfo) {
        return new WorkerInfoResponseDTO(DistributionMechnaism.QUEUE, mqHost + ";" + mqName);
    }
}
