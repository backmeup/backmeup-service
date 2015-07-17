package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.WorkerInfo;
import org.backmeup.model.WorkerMetric;
import org.backmeup.model.dto.WorkerConfigDTO;
import org.backmeup.model.dto.WorkerInfoDTO;
import org.backmeup.model.dto.WorkerMetricDTO;
import org.backmeup.rest.auth.BackmeupPrincipal;

@Path("/workers")
public class Workers extends Base {
    @Context
    private SecurityContext securityContext;
    
    @RolesAllowed({ "worker" })
    @PUT
    @Path("/hello")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WorkerConfigDTO initializeWorker(WorkerInfoDTO workerInfo) {
        WorkerInfo workerInfoModel = getMapper().map(workerInfo, WorkerInfo.class);
        return getLogic().initializeWorker(workerInfoModel);
    }
    
    @RolesAllowed({ "worker" })
    @POST
    @Path("/metrics")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addWorkerMetric(List<WorkerMetricDTO> metrics) {
        BackMeUpUser activeWorker = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();
        
        List<WorkerMetric> metricsList = new ArrayList<WorkerMetric>(metrics.size());
        for(WorkerMetricDTO m : metrics) {
            WorkerMetric model = getMapper().map(m, WorkerMetric.class);
            model.setWorkerId(Long.toString(activeWorker.getUserId()));
            metricsList.add(model);
        }
        
        getLogic().addWorkerMetrics(metricsList);
    }
}
