package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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
import org.backmeup.model.constants.BackupJobStatus;
import org.backmeup.model.constants.DelayTimes;
import org.backmeup.model.dto.BackupJobCreationDTO;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.BackupJobDTO.JobFrequency;
import org.backmeup.model.dto.BackupJobDTO.JobStatus;
import org.backmeup.model.dto.BackupJobExecutionDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.rest.auth.BackmeupPrincipal;
import org.backmeup.rest.filters.SecurityInterceptor;


@Path("/backupjobs")
public class BackupJobs extends Base {
    @Context
    private SecurityContext securityContext;

    @RolesAllowed("user")
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BackupJobDTO> listBackupJobs(@QueryParam("jobStatus") JobStatus jobStatus) {
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        BackupJobStatus status = null;
        if (jobStatus != null) {
            status = getMapper().map(jobStatus, BackupJobStatus.class);
        }

        List<BackupJob> allJobsOfUser = getLogic().getBackupJobs(activeUser.getUserId());
        List<BackupJobDTO> jobList = new ArrayList<>();

        for(BackupJob job : allJobsOfUser) {
            if ((status == null) || (status == job.getStatus())) {
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
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        Profile sourceProfile = getLogic().getPluginProfile(backupJob.getSource());
        sourceProfile.setOptions(new ArrayList<String>());

        Profile sinkProfile = getLogic().getPluginProfile(backupJob.getSink());

        List<Profile> actionProfiles = new ArrayList<>();
        if (backupJob.getActions() != null) {
            for (Long actionId : backupJob.getActions()) {
                Profile actionProfile = getLogic().getPluginProfile(actionId);
                actionProfiles.add(actionProfile);
            }
        }

        long delay = DelayTimes.DELAY_MONTHLY;
        boolean reschedule = false;
        String timeExpression = "monthly";
//        long nextExecution = new Date().getTime(); 

        if (backupJob.getSchedule().equals(JobFrequency.daily)) {
            delay = DelayTimes.DELAY_DAILY;
            timeExpression = "daily";
            reschedule = true;
        } else if (backupJob.getSchedule().equals(JobFrequency.weekly)) {
            delay = DelayTimes.DELAY_WEEKLY;
            timeExpression = "weekly";
            reschedule = true;

        } else if (backupJob.getSchedule().equals(JobFrequency.montly)) {
            delay = DelayTimes.DELAY_MONTHLY;
            timeExpression = "monthly";
            reschedule = true;

        } else if (backupJob.getSchedule().equals(JobFrequency.once)) {
            delay = DelayTimes.DELAY_REALTIME;
            timeExpression = "realtime";
            reschedule = false;
        } 

        BackupJob job = new BackupJob(activeUser, sourceProfile, sinkProfile, actionProfiles, backupJob.getStart(), delay, backupJob.getJobTitle(), reschedule);
        job.setTimeExpression(timeExpression);
//        nextExecution += delay;
        job.setNextExecutionTime(new Date());
        job = getLogic().createBackupJob(job);

//        if(vn.getValidationEntries().size() > 0) {
//            List<ValidationEntry> entries = vn.getValidationEntries();
//            throw new WebApplicationException("Validation threw " + entries.size() + " errors", Status.INTERNAL_SERVER_ERROR);
//        } 
//
//        job = vn.getJob();
        return getMapper().map(job, BackupJobDTO.class);

    }

    @RolesAllowed({"user", "worker"})
    @GET
    @Path("/{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public BackupJobDTO getBackupJob(
            @PathParam("jobId") String jobId, 
            @QueryParam("expandUser") @DefaultValue("false") boolean expandUser,
            @QueryParam("expandToken") @DefaultValue("false") boolean expandToken,
            @QueryParam("expandProfiles") @DefaultValue("false") boolean expandProfiles,
            @QueryParam("expandProtocol") @DefaultValue("false") boolean expandProtocol) {
        
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        BackupJob job = getLogic().getBackupJob(Long.parseLong(jobId));
        if ((!activeUser.getUserId().equals(job.getUser().getUserId())) && (!activeUser.getUsername().equals(SecurityInterceptor.BACKMEUP_WORKER_NAME))) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        BackupJobDTO jobDTO = getMapper().map(job, BackupJobDTO.class);

        if(!expandUser) {
            jobDTO.setUser(null);
        }

        if(!expandToken) {
            jobDTO.setToken(null);
        }

        if(expandProfiles) {
            // get source profile
            Profile sourceProfile = getLogic().getPluginProfile(job.getSourceProfile().getId());
            PluginProfileDTO sourceProfileDTO = getMapper().map(sourceProfile, PluginProfileDTO.class);
            sourceProfileDTO.setPluginId(sourceProfile.getPluginId());
            jobDTO.setSource(sourceProfileDTO);

            // get sink profile
            Profile sinkProfile = getLogic().getPluginProfile(job.getSinkProfile().getId());		
            PluginProfileDTO sinkProfileDTO = getMapper().map(sinkProfile, PluginProfileDTO.class);
            sinkProfileDTO.setPluginId(sinkProfile.getPluginId());
            jobDTO.setSink(sinkProfileDTO);

            // get action profiles
            for(Profile action : job.getActionProfiles()) {
                Profile actionProfile = getLogic().getPluginProfile(action.getId());
                PluginProfileDTO actionProfileDTO = getMapper().map(actionProfile, PluginProfileDTO.class);
                actionProfileDTO.setPluginId(actionProfile.getPluginId());
                jobDTO.addAction(actionProfileDTO);
            }
        }

        return jobDTO;
    }

    @RolesAllowed({"user", "worker"})
    @PUT
    @Path("/{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public BackupJobDTO updateBackupJob(@PathParam("jobId") String jobId, BackupJobDTO backupjob) {
        if(Long.parseLong(jobId) != backupjob.getJobId()) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        BackupJob job = getLogic().getBackupJob(backupjob.getJobId());
        if ((!activeUser.getUserId().equals(job.getUser().getUserId())) && (!activeUser.getUsername().equals(SecurityInterceptor.BACKMEUP_WORKER_NAME))) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

//        job.getToken().setTokenId(backupjob.getToken().getTokenId());
        job.getToken().setToken(backupjob.getToken().getToken());
        job.getToken().setBackupdate(backupjob.getToken().getValidity());

        BackupJobStatus jobStatus = getMapper().map(backupjob.getJobStatus(), BackupJobStatus.class);
        job.setStatus(jobStatus);

        // TODO: Job protocol

        getLogic().updateBackupJob(job.getUser().getUserId(), job);

        return backupjob;

    }

    @RolesAllowed("user")
    @DELETE
    @Path("/{jobId}")
    public void deleteBackupJob(@PathParam("jobId") String jobId) {
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        BackupJob job = getLogic().getBackupJob(Long.parseLong(jobId));
        if (!job.getUser().getUserId().equals(activeUser.getUserId())) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        getLogic().deleteBackupJob(activeUser.getUserId(), Long.parseLong(jobId));
    }
    
    @RolesAllowed("user")
    @GET
    @Path("/{jobId}/executions/")
    public List<BackupJobExecutionDTO> listBackupJobExecutions(@PathParam("jobId") String jobId) {
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

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
        
    }
    
    @RolesAllowed({"user", "worker"})
    @GET
    @Path("/{jobId}/executions/{jobExecutionId}")
    public BackupJobExecutionDTO getBackupJobExecution(@PathParam("jobId") String jobId, @PathParam("jobExecutionId") String jobExecutionId) {
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();
        
        BackupJobExecution exec = getLogic().getBackupJobExecution(Long.parseLong(jobExecutionId));
        if ((!activeUser.getUserId().equals(exec.getUser().getUserId())) && (!activeUser.getUsername().equals(SecurityInterceptor.BACKMEUP_WORKER_NAME))) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        BackupJobExecutionDTO execDTO = getMapper().map(exec, BackupJobExecutionDTO.class);
        
        return execDTO;
    }
}
