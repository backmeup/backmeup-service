package org.backmeup.rest;

import java.util.Date;

import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.constants.JobFrequency;
import org.backmeup.model.constants.JobStatus;
import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.PluginConfigurationDTO;
import org.backmeup.model.dto.PluginConfigurationDTO.PluginConfigurationType;
import org.backmeup.model.dto.PluginDTO;
import org.backmeup.model.dto.PluginInputFieldDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.UserDTO;
import org.backmeup.model.spi.PluginDescribable.PluginType;

public class DummyDataManager {
	public static UserDTO getUserDTO() {
		UserDTO user = new UserDTO("john.doe", "John", "Doe", null, "john.doe@example.com");
		return user;
	}
	
	public static PluginDTO getPluginDTO(boolean expandProfiles) {
		PluginDTO plugin = new PluginDTO();
		plugin.setPluginId("org.backmeup.mail");
		plugin.setTitle("BackMeUp Mail Plug-In");
		plugin.setDescription("A plug-in that is capable of downloading e-mails");
		plugin.setImageURL("http://about:blank");
		plugin.setPluginType(PluginType.Source);
		plugin.addMetadata("META_BACKUP_FREQUENCY", "daily");

		plugin.setAuthDataDescription(getPluginConfigurationDTO());
		
		if (expandProfiles) {
			plugin.addProfile(getPluginProfileDTO(false));
		}

		return plugin;
	}

	public static PluginConfigurationDTO getPluginConfigurationDTO() {
		PluginConfigurationDTO pluginConfig = new PluginConfigurationDTO(PluginConfigurationType.input);

		pluginConfig.addRequiredInput(new PluginInputFieldDTO("mail.username", "Username", "Username of your email account", true, 0, Type.String, ""));
		pluginConfig.addRequiredInput(new PluginInputFieldDTO("mail.password", "Password", "Password of your email account", true, 1, Type.Password, ""));

		return pluginConfig;
	}

	public static PluginProfileDTO getPluginProfileDTO(boolean expandConfig) {
		PluginProfileDTO pluginProfile = new PluginProfileDTO();

		pluginProfile.setProfileId(1);
		pluginProfile.setPluginId("org.backmeup.mail");
		pluginProfile.setProfileType(PluginType.Source);
		pluginProfile.setModified(1401099707142L);
		
		if(expandConfig) {
			AuthDataDTO authData = new AuthDataDTO(1L, "Email Work");
			authData.addProperty("mail.username", "john.doe");
			authData.addProperty("mail.password", "JoHn123!");
			
			pluginProfile.addProperty("includeAttachments", "true");
			
			pluginProfile.addOption("Inbox");
			pluginProfile.addOption("Sent Items");
		}

		return pluginProfile;
	}
	
	public static AuthDataDTO getAuthDataDTO(boolean expandProperties) {
		AuthDataDTO authData = new AuthDataDTO();
		authData.setId(54L);
		authData.setName("Email Work");

		if (expandProperties) {
			authData.addProperty("mail.username", "u1024");
			authData.addProperty("mail.username", "s3cr3tPW!");
		}

		return authData;
	}
			
    public static BackupJobDTO getBackupJobDTO(boolean expandUser,
            boolean expandToken, boolean expandProfiles) {
		BackupJobDTO job = new BackupJobDTO();
		job.setJobId(1L);
		job.setJobTitle("BackupJob1");
		job.setStatus(JobStatus.ACTIVE);
		job.setSchedule(JobFrequency.WEEKLY);
		job.setCreated(new Date(1401201920089L));
		job.setModified(new Date(1401201921774L));
		job.setStart(new Date(1401201920087L));
		job.setNext(new Date(1401806728634L));
		job.setDelay(604800000);
		
		if (expandUser) {
			job.setUser(getUserDTO());
		}

		if (expandToken) {
			job.setToken("Vm9lSEliY...t1S3c9PQ==");
		}

		if (expandProfiles) {
			PluginProfileDTO source = getPluginProfileDTO(false);
			source.setProfileType(PluginType.Source);
			job.setSource(source);

			PluginProfileDTO action = getPluginProfileDTO(false);
			action.setProfileType(PluginType.Action);
			job.addAction(action);

			PluginProfileDTO sink = getPluginProfileDTO(false);
			sink.setProfileType(PluginType.Sink);
			job.setSink(sink);
		}
		
		return job;
	}
}
