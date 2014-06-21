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

import org.backmeup.model.dto.PluginDTO;
import org.backmeup.model.dto.PluginDTO.PluginType;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.rest.DummyDataManager;

@Path("/plugins")
public class Plugins extends Base {
	public enum PluginSelectionType {
	    source,
	    sink,
	    action,
	    all
	}
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PluginDTO> listPlugins(
			@QueryParam("types") @DefaultValue("all") PluginSelectionType pluginType,
			@QueryParam("expandProfiles") @DefaultValue("false") boolean expandProfiles) {
		PluginDTO plugin = DummyDataManager.getPluginDTO(expandProfiles);

		if(pluginType == PluginSelectionType.all) {
			// ...
		} else if(pluginType == PluginSelectionType.source) {
			plugin.setPluginType(PluginType.source);
		} else if(pluginType == PluginSelectionType.sink) {
			plugin.setPluginType(PluginType.sink);
		} else if(pluginType == PluginSelectionType.action) {
			plugin.setPluginType(PluginType.action);
		}

		List<PluginDTO> pluginList= new ArrayList<>();
		pluginList.add(plugin);
		return pluginList;
	}
	
	@GET
	@Path("/{pluginId}")
	@Produces(MediaType.APPLICATION_JSON)
	public PluginDTO getPlugin(@PathParam("pluginId") String pluginId, @QueryParam("expandProfiles") @DefaultValue("false") boolean expandProfiles) {
		return DummyDataManager.getPluginDTO(expandProfiles);
	}
	
	@POST
	@Path("/{pluginId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public PluginProfileDTO addPluginProfile(@PathParam("pluginId") String pluginId, PluginProfileDTO pluginProfile) {
		pluginProfile.setProfileId(1L);
		return pluginProfile;
	}
	
	@GET
	@Path("/{pluginId}/{profileId}")
	@Produces(MediaType.APPLICATION_JSON)
	public PluginProfileDTO getPluginProfile(
			@PathParam("pluginId") String pluginId, 
			@PathParam("profileId") String profileId, 
			@QueryParam("expandConfig") @DefaultValue("false") boolean expandConfig) {
		return DummyDataManager.getPluginProfileDTO(expandConfig);
	}
	
	@PUT
	@Path("/{pluginId}/{profileId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public PluginProfileDTO updatePluginProfile(@PathParam("pluginId") String pluginId, @PathParam("profileId") String profileId, PluginProfileDTO pluginProfile) {
		pluginProfile.setProfileId(1L);
		return pluginProfile;
	}
	
	@DELETE
	@Path("/{pluginId}/{profileId}")
	@Produces(MediaType.APPLICATION_JSON)
	public void deletePluginProfile(@PathParam("pluginId") String pluginId, @PathParam("profileId") String profileId) {
		
	}
}
