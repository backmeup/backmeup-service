package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.dto.PluginConfigurationDTO;
import org.backmeup.model.dto.PluginConfigurationDTO.PluginConfigurationType;
import org.backmeup.model.dto.PluginDTO;
import org.backmeup.model.dto.PluginDTO.PluginType;
import org.backmeup.model.dto.PluginInputFieldDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.elasticsearch.common.mvel2.optimizers.impl.refl.nodes.ArrayLength;

/**
 * All user specific operations will be handled within this class.
 */
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
		PluginDTO plugin = new PluginDTO();
		plugin.setPluginId("org.backmeup.mail");
		plugin.setTitle("BackMeUp Mail Plug-In");
		plugin.setDescription("A plug-in that is capable of downloading e-mails");
		plugin.setImageURL("http://about:blank");
		plugin.setPluginType(PluginType.source);
		plugin.addMetadata("META_BACKUP_FREQUENCY", "daily");
		
		PluginConfigurationDTO pluginConfig = new PluginConfigurationDTO(PluginConfigurationType.input);
		pluginConfig.addRequiredInput(new PluginInputFieldDTO("mail.username", "Username", "Username of your email account", true, 0, Type.String));
		pluginConfig.addRequiredInput(new PluginInputFieldDTO("mail.password", "Password", "Password of your email account", true, 1, Type.Password));
		plugin.setConfig(pluginConfig);
		
		if(expandProfiles) {
			PluginProfileDTO pluginProfile = new PluginProfileDTO();
			pluginProfile.setProfileId(1);
			pluginProfile.setTitle("MailProfile");
			pluginProfile.setPluginId("org.backmeup.mail");
			pluginProfile.setProfileType(PluginType.source);
			pluginProfile.setModified(1401099707142L);
			plugin.addProfile(pluginProfile);
		}
		
		
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
	
	@POST
	@Path("/{pluginId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public PluginProfileDTO addPluginProfile(@PathParam("pluginId") String pluginId, PluginProfileDTO pluginProfile) {
		return pluginProfile;
	}
	
	@GET
	@Path("/{pluginId}")
	@Produces(MediaType.APPLICATION_JSON)
	public PluginDTO getPlugin(@PathParam("pluginId") String pluginId, @QueryParam("expandProfiles") @DefaultValue("false") boolean expandProfiles) {
		PluginDTO plugin = new PluginDTO();
		plugin.setPluginId("org.backmeup.mail");
		plugin.setTitle("BackMeUp Mail Plug-In");
		plugin.setDescription("A plug-in that is capable of downloading e-mails");
		plugin.setImageURL("http://about:blank");
		plugin.setPluginType(PluginType.source);
		plugin.addMetadata("META_BACKUP_FREQUENCY", "daily");
		
		PluginConfigurationDTO pluginConfig = new PluginConfigurationDTO(PluginConfigurationType.input);
		pluginConfig.addRequiredInput(new PluginInputFieldDTO("mail.username", "Username", "Username of your email account", true, 0, Type.String));
		pluginConfig.addRequiredInput(new PluginInputFieldDTO("mail.password", "Password", "Password of your email account", true, 1, Type.Password));
		plugin.setConfig(pluginConfig);
		
		if(expandProfiles) {
			PluginProfileDTO pluginProfile = new PluginProfileDTO();
			pluginProfile.setProfileId(1);
			pluginProfile.setTitle("MailProfile");
			pluginProfile.setPluginId("org.backmeup.mail");
			pluginProfile.setProfileType(PluginType.source);
			pluginProfile.setModified(1401099707142L);
			plugin.addProfile(pluginProfile);
		}
		
		return plugin;
	}
}
