package org.backmeup.rest.resources;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.rest.DummyDataManager;

@Path("/backupjobs")
public class BackupJobs extends Base {	
	@GET
	@Path("/{jobId}")
	@Produces(MediaType.APPLICATION_JSON)
	public BackupJobDTO getPlugin(
			@PathParam("jobId") String pluginId, 
			@QueryParam("expandUser") @DefaultValue("false") boolean expandUser,
			@QueryParam("expandToken") @DefaultValue("false") boolean expandToken,
			@QueryParam("expandProfiles") @DefaultValue("false") boolean expandProfiles,
			@QueryParam("expandProtocol") @DefaultValue("false") boolean expandProtocol) {
		return DummyDataManager.getBackupJobDTO(expandUser, expandToken, expandProfiles, expandProtocol);
	}
}
