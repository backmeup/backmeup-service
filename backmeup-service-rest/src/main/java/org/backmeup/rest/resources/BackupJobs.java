package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.List;

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
import javax.ws.rs.core.MediaType;

import org.backmeup.model.dto.BackupJobCreationDTO;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.BackupJobDTO.JobStatus;
import org.backmeup.rest.DummyDataManager;

@Path("/backupjobs")
public class BackupJobs extends Base {	
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
		if(jobStatus == null){
			jobList.add(job);
		} else if (jobStatus == job.getJobStatus()) {
			jobList.add(job);
		}
		
		return jobList;
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BackupJobDTO createBackupJob(BackupJobCreationDTO backupJob) {
		boolean expandUser = false;
		boolean expandToken = false;
		boolean expandProfiles = false;
		boolean expandProtocol = false;
		
		return  DummyDataManager.getBackupJobDTO(expandUser, expandToken, expandProfiles, expandProtocol);
	}
	
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
	
	@PUT
	@Path("/{jobId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BackupJobDTO updateBackupJob(@PathParam("jobId") String pluginId, BackupJobDTO backupjob) {
		return backupjob;
	}
	
	@DELETE
	@Path("/{jobId}")
	public void deleteBackupJob(@PathParam("jobId") String jobId) {
		
	}
}
