package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.constants.DelayTimes;
import org.backmeup.model.dto.BackupJobCreationDTO;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.BackupJobDTO.JobFrequency;
import org.backmeup.model.dto.BackupJobDTO.JobStatus;
import org.backmeup.rest.DummyDataManager;
import org.backmeup.rest.auth.BackmeupPrincipal;


@Path("/backupjobs")
public class BackupJobs extends Base {	
	@Context
    private SecurityContext securityContext;
	
	@RolesAllowed("user")
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<BackupJobDTO> listBackupJobs(@QueryParam("jobStatus") JobStatus jobStatus) {
		boolean expandUser = false;
		boolean expandToken = false;
		boolean expandProfiles = false;
		boolean expandProtocol = false;
		
		List<BackupJobDTO> jobList = new ArrayList<>();
		
		BackupJobDTO job =  DummyDataManager.getBackupJobDTO(expandUser, expandToken, expandProfiles, expandProtocol);
		if ((jobStatus == null) || (jobStatus == job.getJobStatus())) {
			jobList.add(job);
		}
		
		return jobList;
	}
	
	@RolesAllowed("user")
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BackupJobDTO createBackupJob(BackupJobCreationDTO backupJob) {
		boolean expandUser = false;
		boolean expandToken = false;
		boolean expandProfiles = false;
		boolean expandProtocol = false;
		
//		return  DummyDataManager.getBackupJobDTO(expandUser, expandToken, expandProfiles, expandProtocol);
		BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();
		
		Profile sourceProfile = getLogic().getPluginProfile(backupJob.getSource());
		Set<ProfileOptions> sourceProfiles = new HashSet<>();
		sourceProfiles.add(new ProfileOptions(sourceProfile, new String[0]));
		
		Profile sinkProfile = getLogic().getPluginProfile(backupJob.getSink());
		
		// TODO: actions ignored
		List<ActionProfile> actionProfiles = new ArrayList<>();
		
		long delay = DelayTimes.DELAY_MONTHLY;
		boolean reschedule = false;
		String timeExpression = "monthly";
		
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
			
		} else if (backupJob.getSchedule().equals(JobFrequency.onece)) {
			delay = DelayTimes.DELAY_REALTIME;
			timeExpression = "realtime";
			reschedule = false;
			
		} 
		
		BackupJob job = new BackupJob(activeUser, sourceProfiles, sinkProfile, actionProfiles, backupJob.getStart(), delay, backupJob.getJobTitle(), reschedule);
		job.setTimeExpression(timeExpression);
		ValidationNotes vn = getLogic().createBackupJob(job);
		
		if(vn.getValidationEntries().size() > 0) {
//			List<ValidationEntry> entries = vn.getValidationEntries();
//			throw new WebApplicationException("Validation threw " + entries.size() + " errors", Status.INTERNAL_SERVER_ERROR);
			
		} 
		
		job = vn.getJob();
		return getMapper().map(job, BackupJobDTO.class);
		
	}
	
	@RolesAllowed("user")
	@GET
	@Path("/{jobId}")
	@Produces(MediaType.APPLICATION_JSON)
	public BackupJobDTO getBackupJob(
			@PathParam("jobId") String jobId, 
			@QueryParam("expandUser") @DefaultValue("false") boolean expandUser,
			@QueryParam("expandToken") @DefaultValue("false") boolean expandToken,
			@QueryParam("expandProfiles") @DefaultValue("false") boolean expandProfiles,
			@QueryParam("expandProtocol") @DefaultValue("false") boolean expandProtocol) {
		return DummyDataManager.getBackupJobDTO(expandUser, expandToken, expandProfiles, expandProtocol);
	}
	
	@RolesAllowed("user")
	@PUT
	@Path("/{jobId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BackupJobDTO updateBackupJob(@PathParam("jobId") String pluginId, BackupJobDTO backupjob) {
		return backupjob;
	}
	
	@RolesAllowed("user")
	@DELETE
	@Path("/{jobId}")
	public void deleteBackupJob(@PathParam("jobId") String jobId) {
		
	}
}
