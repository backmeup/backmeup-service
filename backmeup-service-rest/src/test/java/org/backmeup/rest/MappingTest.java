package org.backmeup.rest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.constants.BackupJobStatus;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.BackupJobDTO.JobFrequency;
import org.backmeup.model.dto.BackupJobDTO.JobStatus;
import org.backmeup.model.dto.PluginConfigurationDTO;
import org.backmeup.model.dto.PluginDTO;
import org.backmeup.model.dto.PluginInputFieldDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.UserDTO;
import org.backmeup.model.spi.FakePluginDescribable;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.osgi.PluginImpl;
import org.dozer.Mapper;
import org.junit.Test;

public class MappingTest {

    private MapperProducer mapperProducer = new MapperProducer();

	@Test
	public void testUserMapping() {
		Long userId = 1L;
		String username = "johndoe";
		String firstname = "John";
		String lastname = "Doe";
		String email = "johndoe@example.com";
		boolean activated = true;
		String verificationKey = "123ABC";
		String password = "john123!#";
		
		BackMeUpUser srcUser = new BackMeUpUser(userId, username, firstname, lastname, email, password);
		srcUser.setActivated(activated);
		srcUser.setVerificationKey(verificationKey);
		
		Mapper mapper = getMapper();
		
		UserDTO destUser = mapper.map(srcUser, UserDTO.class);
		
		assertEquals(srcUser.getUserId(), destUser.getUserId());
		assertEquals(srcUser.getUsername(), destUser.getUsername());
		assertEquals(srcUser.getFirstname(), destUser.getFirstname());
		assertEquals(srcUser.getLastname(), destUser.getLastname());
		assertEquals(srcUser.getEmail(), destUser.getEmail());
		assertEquals(srcUser.isActivated(), destUser.isActivated());
	}
	
	@Test
	public void testPluginMapping() {
		String pluginId = "org.backmeup.dropbox";
		// Plugin plugin = setupPluginInfrastructure();
		// PluginDescribable  pluginModel = plugin.getPluginDescribableById(pluginId);
	    PluginDescribable  pluginModel = createFakeModelFor(pluginId);
		
		Mapper mapper = getMapper();
		
		PluginDTO pluginDTO = mapper.map(pluginModel, PluginDTO.class);
		
		assertEquals(pluginModel.getId(), pluginDTO.getPluginId());
		assertEquals(pluginModel.getTitle(), pluginDTO.getTitle());
		assertEquals(pluginModel.getDescription(), pluginDTO.getDescription());
		// TODO metadata
		assertEquals(pluginModel.getImageURL(), pluginDTO.getImageURL());
		
		// ((PluginImpl) plugin).shutdown();
	}
	
    private PluginDescribable createFakeModelFor(String pluginId) {
        return new FakePluginDescribable(pluginId);
    }

    @Test
	public void testPluginProfileMapping() {
		String username = "johndoe";
		String firstname = "John";
		String lastname = "Doe";
		String email = "johndoe@example.com";
		String password = "john123!#";
		
		
		Long profileId = 1L;
		BackMeUpUser user = new BackMeUpUser(username, firstname, lastname, email, password);
		String profileName = "TestProfile";
		String description = "Description of test profile";
		String identification = "identification";
		PluginType profileTypeModel = PluginType.Source;
		PluginType profileTypeDTO = PluginType.Source;
		
		Profile profile = new Profile(profileId, user, profileName, description, PluginType.Source);
		profile.setIdentification(identification);
		
		Mapper mapper = getMapper();
		
		PluginProfileDTO profileDTO = mapper.map(profile, PluginProfileDTO.class);
		
		assertEquals(profile.getProfileId().longValue(), profileDTO.getProfileId());
		assertEquals(profile.getProfileName(), profileDTO.getTitle());
		assertEquals(profile.getType(), profileTypeModel);
		assertEquals(profileDTO.getProfileType(), profileTypeDTO);
	}
	
	@Test
	public void testPluginConfigurationMapping() {
		String username = "johndoe";
		String firstname = "John";
		String lastname = "Doe";
		String email = "johndoe@example.com";
		String password = "john123!#";
		
		
		Long profileId = 1L;
		String profileName = "TestProfile";
		String description = "Description of test profile";
		String identification = "identification";
		
		String inputName = "username";
		String inputLabel = "Username";
		String inputDesc = "Username for the service";
		boolean inputRequired = true;
		int inputOrder = 0;
		RequiredInputField.Type inputType = RequiredInputField.Type.String;
		
		
		String redirectUrl = "http://redirecturl";
		
		BackMeUpUser user = new BackMeUpUser(username, firstname, lastname, email, password);
		
		Profile profile = new Profile(profileId, user, profileName, description, PluginType.Source);
		profile.setIdentification(identification);
		
		RequiredInputField inputModel = new RequiredInputField(inputName, inputLabel, inputDesc, inputRequired, inputOrder, inputType);
		List<RequiredInputField> inputFields = new ArrayList<>();
		inputFields.add(inputModel);
		
		AuthRequest authRequest = new AuthRequest(inputFields, null, redirectUrl, profile);
		
		Mapper mapper = getMapper();
		
		PluginConfigurationDTO pluginConfigDTO = mapper.map(authRequest, PluginConfigurationDTO.class);	
		
		assertEquals(authRequest.getRedirectURL(), pluginConfigDTO.getRedirectURL());
		
		PluginInputFieldDTO inputDTO = pluginConfigDTO.getRequiredInputs().get(0);
		assertEquals(inputModel.getLabel(), inputDTO.getLabel());
		assertEquals(inputModel.getName(), inputDTO.getName());
		assertEquals(inputModel.getDescription(), inputDTO.getDescription());
		assertEquals(inputModel.isRequired(), inputDTO.isRequired());
		assertEquals(inputModel.getOrder(), inputDTO.getOrder());
		assertEquals(inputModel.getType(), inputDTO.getType());
	}
	
	@Test
	public void testBackupJobMapping() {
		BackupJob job = new BackupJob();
		job.setStatus(BackupJobStatus.queued);
		job.setId(9L);
		job.setTimeExpression("daily");
		Date next = new Date();
		job.setNextExecutionTime(next);
		
		Mapper mapper = getMapper();
		
		BackupJobDTO jobDTO = mapper.map(job, BackupJobDTO.class);
		
		assertEquals(job.getId(), jobDTO.getJobId());
		assertEquals(JobStatus.queued, jobDTO.getJobStatus());
		assertEquals(JobFrequency.daily, jobDTO.getSchedule());
		assertEquals(job.getNextExecutionTime(), jobDTO.getNext());
	}
	
	@Test
	public void testBackupJobStatusMapping() {
		JobStatus jobStatus = JobStatus.queued;
		BackupJobStatus expectetJobStatus = BackupJobStatus.queued;
		
		Mapper mapper = getMapper();
		BackupJobStatus actualJobStatus = mapper.map(jobStatus, BackupJobStatus.class);
		
		assertEquals(expectetJobStatus, actualJobStatus);
	}
	
	private Mapper getMapper() {
        return mapperProducer.getMapper();
	}
	
	private Plugin setupPluginInfrastructure() {
		Plugin plugin = new PluginImpl(
				"/data/backmeup-service/autodeploy",
				"/data/backmeup-service/osgi-tmp",
				"org.backmeup.plugin.spi org.backmeup.model org.backmeup.model.spi "
				+ "org.backmeup.plugin.api.connectors org.backmeup.plugin.api.storage "
				+ "com.google.gson org.backmeup.plugin.api");

		plugin.startup();
		((PluginImpl) plugin).waitForInitialStartup();

		try {
			Thread.sleep(2000);
		} catch (Exception e) {

		}
		return plugin;
	}
}
