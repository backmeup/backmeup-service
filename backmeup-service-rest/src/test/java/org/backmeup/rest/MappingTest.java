package org.backmeup.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.backmeup.model.AuthData;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.PluginConfigInfo;
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
	    PluginDescribable  pluginModel = createFakeModelFor(pluginId);
		
		Mapper mapper = getMapper();
		
		PluginDTO pluginDTO = mapper.map(pluginModel, PluginDTO.class);
		
		assertEquals(pluginModel.getId(), pluginDTO.getPluginId());
		assertEquals(pluginModel.getTitle(), pluginDTO.getTitle());
		assertEquals(pluginModel.getDescription(), pluginDTO.getDescription());
		// TODO metadata
		assertEquals(pluginModel.getImageURL(), pluginDTO.getImageURL());
	}
	
    private PluginDescribable createFakeModelFor(String pluginId) {
        return new FakePluginDescribable(pluginId);
    }

    @Test
	public void testPluginProfileMapping() {
    	Long userId = 1L;
		String username = "johndoe";
		String firstname = "John";
		String lastname = "Doe";
		String email = "johndoe@example.com";
		String password = "john123!#";
		
		BackMeUpUser user = new BackMeUpUser(username, firstname, lastname, email, password);
		user.setUserId(userId);
		
		Long profileId = 1L;
		String profileName = "TestProfile";
		String pluginId = "org.backmeup.dummy";
		String identification = "identification";
		PluginType profileTypeModel = PluginType.Source;
		PluginType profileTypeDTO = PluginType.Source;
		String option = "-option1";
		String propKey = "includeAll";
		String propValue = "true";
		
		Long authDataId = 1L;
		String authDataName = "TestAuthData";
		String authDataKey = "password";
		String authDataValue = "s3cr3t";
		
		AuthData authData = new AuthData();
		authData.setId(authDataId);
		authData.setName(authDataName);
		authData.setUser(user);
		authData.setPluginId(pluginId);
		authData.addProperty(authDataKey, authDataValue);
		
		Profile profile = new Profile(profileId, user, profileName, pluginId, PluginType.Source);
		profile.setIdentification(identification);
		profile.setAuthData(authData);
		profile.addProperty(propKey, propValue);
		profile.addOption(option);
		
		
		Mapper mapper = getMapper();
		
		PluginProfileDTO profileDTO = mapper.map(profile, PluginProfileDTO.class);
		// auth data properties are excluded (jpa lazy init problem with mapping)
		profileDTO.getAuthData().setProperties(new HashMap<String, String>());
		profileDTO.getAuthData().getProperties().putAll(profile.getAuthData().getProperties());
		
		assertEquals(profile.getId().longValue(), profileDTO.getProfileId());
		assertEquals(profile.getName(), profileDTO.getTitle());
		assertEquals(profile.getType(), profileTypeModel);
		assertEquals(profileDTO.getProfileType(), profileTypeDTO);
		
		assertNotNull(profileDTO.getAuthData());
		assertEquals(profile.getAuthData().getId(), profileDTO.getAuthData().getId());
		assertEquals(profile.getAuthData().getId(), profileDTO.getAuthData().getId());
		assertEquals(profile.getAuthData().getName(), profileDTO.getAuthData().getName());
		assertTrue(profileDTO.getAuthData().getProperties().containsKey(authDataKey));
		assertTrue(profileDTO.getAuthData().getProperties().containsValue(authDataValue));
		
		assertEquals(profile.getOptions().size(), profileDTO.getOptions().size());
		assertEquals(profile.getOptions().get(0), profileDTO.getOptions().get(0));
		
		assertEquals(profile.getProperties().size(), profileDTO.getProperties().size());
		assertTrue(profileDTO.getProperties().containsKey(propKey));
		assertTrue(profileDTO.getProperties().containsValue(propValue));
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
		String inputDefaultValue = "";
		
		
		String redirectUrl = "http://redirecturl";
		
		BackMeUpUser user = new BackMeUpUser(username, firstname, lastname, email, password);
		
		Profile profile = new Profile(profileId, user, profileName, description, PluginType.Source);
		profile.setIdentification(identification);
		
		RequiredInputField inputModel = new RequiredInputField(inputName, inputLabel, inputDesc, inputRequired, inputOrder, inputType, inputDefaultValue);
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
	public void testPluginConfigurationInfoMapping() {		
		String authInputName = "username";
		String authInputLabel = "Username";
		String authInputDesc = "Username for the service";
		boolean authInputRequired = true;
		int authInputOrder = 0;
		RequiredInputField.Type authInputType = RequiredInputField.Type.String;
		String authInputDefaultValue = "";
		
		String authRedirectUrl = "http://redirecturl";
		
		String propInputName = "activateOption";
		String propInputLabel = "Activate Option";
		String propInputDesc = "Descripton for Option ...";
		boolean propInputRequired = true;
		int propInputOrder = 0;
		RequiredInputField.Type propInputType = RequiredInputField.Type.String;
		String proptInputDefaultValue = "true";
		
		String option1 = "-Option1";
				
		
		RequiredInputField authInputModel = new RequiredInputField(authInputName, authInputLabel, authInputDesc, authInputRequired, authInputOrder, authInputType, authInputDefaultValue);
		List<RequiredInputField> authInputFields = new ArrayList<>();
		authInputFields.add(authInputModel);
		
		RequiredInputField propInputModel = new RequiredInputField(propInputName, propInputLabel, propInputDesc, propInputRequired, propInputOrder, propInputType, proptInputDefaultValue);
		List<RequiredInputField> propInputFields = new ArrayList<>();
		propInputFields.add(propInputModel);
		
		List<String> availOption = new ArrayList<>();
		availOption.add(option1);
		
		PluginConfigInfo pluginConfigInfo = new PluginConfigInfo();
		pluginConfigInfo.setRedirectURL(authRedirectUrl);
		pluginConfigInfo.setRequiredInputs(authInputFields);
		pluginConfigInfo.setPropertiesDescription(propInputFields);
		pluginConfigInfo.setAvailableOptions(availOption);
		
		Mapper mapper = getMapper();
		
		PluginDTO pluginDTO = mapper.map(pluginConfigInfo, PluginDTO.class);
		PluginConfigurationDTO pluginConfigDTO = mapper.map(pluginConfigInfo, PluginConfigurationDTO.class);
		pluginDTO.setAuthDataDescription(pluginConfigDTO);
		
		assertEquals(pluginConfigInfo.getRedirectURL(), pluginDTO.getAuthDataDescription().getRedirectURL());
		
		PluginInputFieldDTO authInputDTO = pluginDTO.getAuthDataDescription().getRequiredInputs().get(0);
		assertEquals(authInputModel.getLabel(), authInputDTO.getLabel());
		assertEquals(authInputModel.getName(), authInputDTO.getName());
		assertEquals(authInputModel.getDescription(), authInputDTO.getDescription());
		assertEquals(authInputModel.isRequired(), authInputDTO.isRequired());
		assertEquals(authInputModel.getOrder(), authInputDTO.getOrder());
		assertEquals(authInputModel.getType(), authInputDTO.getType());
		
		PluginInputFieldDTO propsInputDTO = pluginDTO.getPropertiesDescription().get(0);
		assertEquals(propInputModel.getLabel(), propsInputDTO.getLabel());
		assertEquals(propInputModel.getName(), propsInputDTO.getName());
		assertEquals(propInputModel.getDescription(), propsInputDTO.getDescription());
		assertEquals(propInputModel.isRequired(), propsInputDTO.isRequired());
		assertEquals(propInputModel.getOrder(), propsInputDTO.getOrder());
		assertEquals(propInputModel.getType(), propsInputDTO.getType());
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
}
