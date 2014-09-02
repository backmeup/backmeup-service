package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;
import org.backmeup.model.dto.PluginConfigurationDTO;
import org.backmeup.model.dto.PluginConfigurationDTO.PluginConfigurationType;
import org.backmeup.model.dto.PluginDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.rest.auth.BackmeupPrincipal;

@Path("/plugins")
public class Plugins extends Base {
	public enum PluginSelectionType {
	    source,
	    sink,
	    action,
	    all
	}
	
	@Context
    private SecurityContext securityContext;
	
	@RolesAllowed("user")
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PluginDTO> listPlugins(
			@QueryParam("types") @DefaultValue("all") PluginSelectionType pluginType,
			@QueryParam("expandProfiles") @DefaultValue("false") boolean expandProfiles) {
		Set<String> pluginIds = new HashSet<>();

		if ((pluginType == PluginSelectionType.source) || (pluginType == PluginSelectionType.all)) {
			for (PluginDescribable desc : getLogic().getDatasources()) {
				pluginIds.add(desc.getId());
			}
		} else if ((pluginType == PluginSelectionType.sink) || (pluginType == PluginSelectionType.all)) {
			for(PluginDescribable desc : getLogic().getDatasinks()) {
				pluginIds.add(desc.getId());
			}
		} else if ((pluginType == PluginSelectionType.action) || (pluginType == PluginSelectionType.all)) {
			for(PluginDescribable desc : getLogic().getActions()) {
				pluginIds.add(desc.getId());
			}
		}
		
		List<PluginDTO> pluginList= new ArrayList<>();
		for(String pluginId : pluginIds) {
			pluginList.add(getPlugin(pluginId, false));
		}

		return pluginList;
	}
	
	@RolesAllowed("user")
	@GET
	@Path("/{pluginId}")
	@Produces(MediaType.APPLICATION_JSON)
	public PluginDTO getPlugin(@PathParam("pluginId") String pluginId, @QueryParam("expandProfiles") @DefaultValue("false") boolean expandProfiles) {
//		return DummyDataManager.getPluginDTO(expandProfiles);
		PluginDescribable pluginDescribable =  getLogic().getPluginDescribable(pluginId);
		PluginDTO pluginDTO = getMapper().map(pluginDescribable, PluginDTO.class);
		
		// TODO: check why id is not mapped automatically
		pluginDTO.setPluginId(pluginDescribable.getId());

		switch (pluginDescribable.getType()) {
		case Source:
			pluginDTO.setPluginType(PluginType.Source);
			break;
			
		case Sink:
			pluginDTO.setPluginType(PluginType.Sink);
			break;
			
		case SourceSink:
			pluginDTO.setPluginType(PluginType.SourceSink);

		default:
			break;
		}
		
		Set<Entry<Object, Object>> metadataProperties = pluginDescribable.getMetadata(new Properties()).entrySet();
		for (Entry<Object, Object> e : metadataProperties) {
			String key = (String) e.getKey();
			String value = (String) e.getValue();
			pluginDTO.addMetadata(key, value);
		} 
		
		AuthRequest authInfo = getLogic().getPluginConfiguration(pluginId);
		PluginConfigurationDTO pluginConfig = getMapper().map(authInfo, PluginConfigurationDTO.class);
		if ((authInfo.getRedirectURL() != null) && (authInfo.getRedirectURL() != "")) {
			pluginConfig.setConfigType(PluginConfigurationType.oauth);
		} else {
			pluginConfig.setConfigType(PluginConfigurationType.input);
		}
		pluginDTO.setConfig(pluginConfig);
		
		
		if(expandProfiles){
			
		}
		
		return pluginDTO;
	}
	
	@RolesAllowed("user")
	@POST
	@Path("/{pluginId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public PluginProfileDTO addPluginProfile(@PathParam("pluginId") String pluginId, PluginProfileDTO pluginProfile) {
//		pluginProfile.setProfileId(1L);
//		return pluginProfile;
		
		BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();
		
		Profile profile = new Profile();
		profile.setProfileName(pluginProfile.getTitle());
		if(pluginProfile.getProfileType().equals(PluginType.Source)) {
			profile.setType(PluginType.Source);
		} else if(pluginProfile.getProfileType().equals(PluginType.Sink)) {
			profile.setType(PluginType.Sink);
		}
		profile.setUser(activeUser);
		
		Properties profileProps = new Properties();
		if (pluginProfile.getConfigProperties() != null) {
			profileProps.putAll(pluginProfile.getConfigProperties());
		}
		
		List<String> profileOptions = pluginProfile.getOptions();
		if(profileOptions == null) {
			profileOptions = new ArrayList<>();
		}
		
		profile = getLogic().addPluginProfile(pluginId, profile, profileProps, profileOptions);
		
		PluginProfileDTO profileDTO = getMapper().map(profile, PluginProfileDTO.class);
		profileDTO.setPluginId(pluginId);
		profileDTO.setConfigProperties(pluginProfile.getConfigProperties());
		profileDTO.setOptions(pluginProfile.getOptions());
		
		return profileDTO;
	}
	
	@RolesAllowed("user")
	@GET
	@Path("/{pluginId}/{profileId}")
	@Produces(MediaType.APPLICATION_JSON)
	public PluginProfileDTO getPluginProfile(
			@PathParam("pluginId") String pluginId, 
			@PathParam("profileId") String profileId, 
			@QueryParam("expandConfig") @DefaultValue("false") boolean expandConfig) {
//		return DummyDataManager.getPluginProfileDTO(expandConfig);
		
		BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();
		
		Profile profile = getLogic().getPluginProfile(Long.parseLong(profileId));
		
		if(!profile.getUser().getUserId().equals(activeUser.getUserId())) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		
		PluginProfileDTO profileDTO = getMapper().map(profile, PluginProfileDTO.class);
		profileDTO.setPluginId(pluginId);
		
		return profileDTO;
	}
	
	@RolesAllowed("user")
	@PUT
	@Path("/{pluginId}/{profileId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public PluginProfileDTO updatePluginProfile(@PathParam("pluginId") String pluginId, @PathParam("profileId") String profileId, PluginProfileDTO pluginProfile) {
//		pluginProfile.setProfileId(1L);
//		return pluginProfile;
		
		BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();
		Profile profile = getLogic().getPluginProfile(Long.parseLong(profileId));
		if((!activeUser.getUserId().equals(profile.getUser().getUserId())) && 
		   (pluginProfile.getProfileId() != Long.parseLong(profileId))) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		Properties configProps = new Properties();
		configProps.putAll(pluginProfile.getConfigProperties());
		List<String> configOptions = pluginProfile.getOptions();
		getLogic().updatePluginProfile(pluginId, profile, configProps, configOptions);
		
		Profile profileModel = getLogic().getPluginProfile(Long.parseLong(profileId));
		PluginProfileDTO profileDTO = getMapper().map(profileModel, PluginProfileDTO.class);
		profileDTO.setPluginId(pluginId);
		
		return profileDTO;
	}
	
	@RolesAllowed("user")
	@DELETE
	@Path("/{pluginId}/{profileId}")
	@Produces(MediaType.APPLICATION_JSON)
	public void deletePluginProfile(@PathParam("pluginId") String pluginId, @PathParam("profileId") String profileId) {
		BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();
		
		Profile profile = getLogic().getPluginProfile(Long.parseLong(profileId));
		if(!activeUser.getUserId().equals(profile.getUser().getUserId())) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		
		// TODO check pluginId
		// if profile.getPluginId != pluginId -> FORBIDDEN
		
		getLogic().deleteProfile(activeUser.getUserId(), Long.parseLong(profileId));
	}
}
