package org.backmeup.rest;

import java.util.Date;

import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.BackupJobDTO.JobFrequency;
import org.backmeup.model.dto.BackupJobDTO.JobStatus;
import org.backmeup.model.dto.JobProtocolDTO;
import org.backmeup.model.dto.PluginConfigurationDTO;
import org.backmeup.model.dto.PluginDTO;
import org.backmeup.model.dto.PluginInputFieldDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.TokenDTO;
import org.backmeup.model.dto.UserDTO;
import org.backmeup.model.dto.PluginConfigurationDTO.PluginConfigurationType;
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
		pluginProfile.setProfileType(PluginType.Source);
		pluginProfile.setModified(1401099707142L);
		
		if(expandConfig) {
			pluginProfile.addProperty("mail.username", "john.doe");
			pluginProfile.addProperty("mail.password", "JoHn123!");
			
			pluginProfile.addOption("Inbox");
			pluginProfile.addOption("Sent Items");
		}

		return pluginProfile;
	}
	
	public static AuthDataDTO getAuthDataDTO(boolean expandProperties) {
		AuthDataDTO authData = new AuthDataDTO();
		authData.setAuthDataId(54L);
		authData.setName("Email Work");

		if (expandProperties) {
			authData.addProperty("mail.username", "u1024");
			authData.addProperty("mail.username", "s3cr3tPW!");
		}

		return authData;
	}
	
	public static TokenDTO getTokenDTO() {
		TokenDTO token = new TokenDTO();
		token.setTokenId(11254L);
		token.setToken("Vm9lSEliY...t1S3c9PQ==");
		return token;
	}
	
	public static JobProtocolDTO getJobProtocolDTOSuccessful() {
		JobProtocolDTO prot = new JobProtocolDTO();
		prot.setProtocolId(3L);
		prot.setTimestamp(1402025132472L);
		prot.setStart(1401099531932L);
		prot.setExecutionTime(925600540L);
		prot.setSuccessful(true);
		prot.setProcessedItems(19463L);
		prot.setSpace(128000);
		prot.setMessage("");
		return prot;
	}
	
	public static JobProtocolDTO getJobProtocolDTOError() {
		JobProtocolDTO prot = new JobProtocolDTO();
		prot.setProtocolId(62L);
		prot.setTimestamp(1401101262264L);
		prot.setStart(1401099641492L);
		prot.setExecutionTime(1620772L);
		prot.setSuccessful(false);
		prot.setProcessedItems(0);
		prot.setSpace(0);
		prot.setMessage("java.lang.NullPointerException at ...");
		return prot;
	}
	
	public static BackupJobDTO getBackupJobDTO(
			boolean expandUser, boolean expandToken, 
			boolean expandProfiles, boolean expandProtocol) {
		BackupJobDTO job = new BackupJobDTO();
		job.setJobId(1L);
		job.setJobTitle("BackupJob1");
		job.setJobStatus(JobStatus.queued);
		job.setSchedule(JobFrequency.weekly);
		job.setCreated(new Date(1401201920089L));
		job.setModified(new Date(1401201921774L));
		job.setStart(new Date(1401201920087L));
		job.setNext(new Date(1401806728634L));
		job.setDelay(604800000);
		
		if (expandUser) {
			job.setUser(getUserDTO());
		}

		if (expandToken) {
			job.setToken(getTokenDTO());
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

		if (expandProtocol) {
			job.addProtocol(getJobProtocolDTOSuccessful());
			job.addProtocol(getJobProtocolDTOError());
		}
		
		return job;
	}
}
