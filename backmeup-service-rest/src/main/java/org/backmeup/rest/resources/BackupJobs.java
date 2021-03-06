package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.BackupJobExecution;
import org.backmeup.model.Profile;
import org.backmeup.model.WorkerInfo;
import org.backmeup.model.constants.JobStatus;
import org.backmeup.model.dto.BackupJobCreationDTO;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.BackupJobExecutionDTO;
import org.backmeup.rest.auth.BackmeupPrincipal;


@Path("/backupjobs")
public class BackupJobs extends Base {
    @Context
    private SecurityContext securityContext;

    @RolesAllowed("user")
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BackupJobDTO> listBackupJobs(@QueryParam("status") JobStatus jobStatus) {
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);
        activeUser.setPassword(principal.getAuthToken().getB64Token());

        List<BackupJob> allJobsOfUser = getLogic().getBackupJobs(activeUser.getUserId());
        List<BackupJobDTO> jobList = new ArrayList<>();

        for(BackupJob job : allJobsOfUser) {
            if ((jobStatus == null) || (jobStatus == job.getStatus())) {
                BackupJobDTO jobDTO = getMapper().map(job, BackupJobDTO.class);
                jobList.add(jobDTO);
            }
        }

        return jobList;
    }

    @RolesAllowed("user")
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public BackupJobDTO createBackupJob(BackupJobCreationDTO backupJob) {
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);
        activeUser.setPassword(principal.getAuthToken().getB64Token());

        Profile sourceProfile = getLogic().getPluginProfile(activeUser, backupJob.getSource());
        Profile sinkProfile = getLogic().getPluginProfile(activeUser, backupJob.getSink());
        List<Profile> actionProfiles = new ArrayList<>();
        if (backupJob.getActions() != null) {
            for (Long actionId : backupJob.getActions()) {
                Profile actionProfile = getLogic().getPluginProfile(activeUser, actionId);
                actionProfiles.add(actionProfile);
            }
        }

        BackupJob job = new BackupJob(activeUser, backupJob.getJobTitle(), sourceProfile, sinkProfile, actionProfiles, backupJob.getStart(), backupJob.getSchedule());
        job = getLogic().createBackupJob(activeUser, job);

        return getMapper().map(job, BackupJobDTO.class);

    }

    @RolesAllowed("user")
    @GET
    @Path("/{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public BackupJobDTO getBackupJob(@PathParam("jobId") String jobId) {
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);
        activeUser.setPassword(principal.getAuthToken().getB64Token());

        BackupJob job = getLogic().getBackupJob(Long.parseLong(jobId));
        if (!activeUser.getUserId().equals(job.getUser().getUserId())) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        return getMapper().map(job, BackupJobDTO.class);
    }

    @RolesAllowed("user")
    @PUT
    @Path("/{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public BackupJobDTO updateBackupJob(@PathParam("jobId") String jobId, BackupJobDTO backupjob) {
        if(Long.parseLong(jobId) != backupjob.getJobId()) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);

        BackupJob job = getLogic().getBackupJob(backupjob.getJobId());
        if (!activeUser.getUserId().equals(job.getUser().getUserId())) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        // TODO:
        job.setStatus(backupjob.getStatus());
        getLogic().updateBackupJob(job.getUser().getUserId(), job);
        return backupjob;
    }

    @RolesAllowed("user")
    @DELETE
    @Path("/{jobId}")
    public void deleteBackupJob(@PathParam("jobId") String jobId) {
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);

        BackupJob job = getLogic().getBackupJob(Long.parseLong(jobId));
        if (!job.getUser().getUserId().equals(activeUser.getUserId())) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        getLogic().deleteBackupJob(activeUser.getUserId(), Long.parseLong(jobId));
    }
    
    @RolesAllowed("user")
    @GET
    @Path("/{jobId}/executions/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BackupJobExecutionDTO> listBackupJobExecutions(@PathParam("jobId") String jobId) {
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);

        List<BackupJobExecution> allJobExecsOfUser = getLogic().getBackupJobExecutions(Long.parseLong(jobId));
        List<BackupJobExecutionDTO> jobExecsList = new ArrayList<>();

        for(BackupJobExecution exec : allJobExecsOfUser) {
            if(!exec.getUser().getUserId().equals(activeUser.getUserId())) {
                throw new WebApplicationException(Status.FORBIDDEN);
            }
            BackupJobExecutionDTO execDTO = getMapper().map(exec, BackupJobExecutionDTO.class);
            jobExecsList.add(execDTO);
        }

        return jobExecsList;
    }
    
    @RolesAllowed("user")
    @POST
    @Path("/{jobId}/executions/")
    public void executeBackupJob(@PathParam("jobId") String jobId) {
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);
        activeUser.setPassword(principal.getAuthToken().getB64Token());
        
        BackupJob job = getLogic().getBackupJob(Long.parseLong(jobId));
        if (!activeUser.getUserId().equals(job.getUser().getUserId())) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        getLogic().startBackupJob(activeUser, job);
    }
    
    @RolesAllowed({"user", "worker"})
    @GET
    @Path("/{jobId}/executions/{jobExecutionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public BackupJobExecutionDTO getBackupJobExecution(
            @PathParam("jobId") String jobId, 
            @PathParam("jobExecutionId") String jobExecutionId) {
        return getBackupJobExecution(jobExecutionId);
    }
    
    @RolesAllowed("user")
    @GET
    @Path("/executions/{jobExecutionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public BackupJobExecutionDTO getBackupJobExecution(@PathParam("jobExecutionId") String jobExecutionId) {
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        BackMeUpUser activeUser = principal.getEntity(BackMeUpUser.class);
        
        BackupJobExecution exec = getLogic().getBackupJobExecution(Long.parseLong(jobExecutionId), false);
        if (!activeUser.getUserId().equals(exec.getUser().getUserId())) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        return getMapper().map(exec, BackupJobExecutionDTO.class);
    }
    
    @RolesAllowed("worker")
    @PUT
    @Path("/executions/{jobExecutionId}/redeem-token")
    @Produces(MediaType.APPLICATION_JSON)
    public BackupJobExecutionDTO getBackupJobExecutionWithProfileData(@PathParam("jobExecutionId") String jobExecutionId) {
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        @SuppressWarnings("unused")
        WorkerInfo worker = principal.getEntity(WorkerInfo.class);
        
        BackupJobExecution exec = getLogic().getBackupJobExecution(Long.parseLong(jobExecutionId), true);
        return getMapper().map(exec, BackupJobExecutionDTO.class);
    }
    
    @RolesAllowed({"worker"})
    @PUT
    @Path("/{jobId}/executions/{jobExecutionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public BackupJobExecutionDTO updateBackupJobExecution(@PathParam("jobId") String jobId, @PathParam("jobExecutionId") String jobExecutionId, BackupJobExecutionDTO jobExecution) {
        if (Long.parseLong(jobId) != jobExecution.getJobId() || (Long.parseLong(jobExecutionId) != jobExecution.getId())) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        BackmeupPrincipal principal = ((BackmeupPrincipal) this.securityContext.getUserPrincipal());
        @SuppressWarnings("unused")
        WorkerInfo worker = principal.getEntity(WorkerInfo.class);

        BackupJobExecution jobExec = getLogic().getBackupJobExecution(jobExecution.getId(), false);

        if (jobExecution.getStart() != null) {
            jobExec.setStartTime(jobExecution.getStart());
        }
        if (jobExecution.getEnd() != null) {
            jobExec.setEndTime(jobExecution.getEnd());
        }

        jobExec.setStatus(jobExecution.getStatus());
        getLogic().updateBackupJobExecution(jobExec);

        return jobExecution;
    }
}
