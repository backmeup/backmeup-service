package org.backmeup.rest;

import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.dto.PluginConfigurationDTO;
import org.backmeup.model.dto.PluginDTO;
import org.backmeup.model.dto.PluginInputFieldDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.PluginConfigurationDTO.PluginConfigurationType;
import org.backmeup.model.dto.PluginDTO.PluginType;

public class DummyDataManager {
	public static PluginDTO getPluginDTO(boolean expandProfiles) {
		PluginDTO plugin = new PluginDTO();
		plugin.setPluginId("org.backmeup.mail");
		plugin.setTitle("BackMeUp Mail Plug-In");
		plugin.setDescription("A plug-in that is capable of downloading e-mails");
		plugin.setImageURL("http://about:blank");
		plugin.setPluginType(PluginType.source);
		plugin.addMetadata("META_BACKUP_FREQUENCY", "daily");

		plugin.setConfig(getPluginConfigurationDTO());
		
		if (expandProfiles) {
			plugin.addProfile(getPluginProfileDTO(false));
		}

		return plugin;
	}

	public static PluginConfigurationDTO getPluginConfigurationDTO() {
		PluginConfigurationDTO pluginConfig = new PluginConfigurationDTO(PluginConfigurationType.input);

		pluginConfig.addRequiredInput(new PluginInputFieldDTO("mail.username", "Username", "Username of your email account", true, 0, Type.String));
		pluginConfig.addRequiredInput(new PluginInputFieldDTO("mail.password", "Password", "Password of your email account", true, 1, Type.Password));

		return pluginConfig;
	}

	public static PluginProfileDTO getPluginProfileDTO(boolean expandConfig) {
		PluginProfileDTO pluginProfile = new PluginProfileDTO();

		pluginProfile.setProfileId(1);
		pluginProfile.setTitle("MailProfile");
		pluginProfile.setPluginId("org.backmeup.mail");
		pluginProfile.setProfileType(PluginType.source);
		pluginProfile.setModified(1401099707142L);
		
		if(expandConfig) {
			pluginProfile.addConfigProperties("mail.username", "john.doe");
			pluginProfile.addConfigProperties("mail.password", "JoHn123!");
			
			pluginProfile.addOption("Inbox");
			pluginProfile.addOption("Sent Items");
		}

		return pluginProfile;
	}
}
