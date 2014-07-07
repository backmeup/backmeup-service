package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.Date;
import java.util.Properties;

import org.backmeup.model.dto.BackupJobCreationDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.BackupJobDTO.JobFrequency;
import org.backmeup.model.dto.PluginDTO.PluginType;
import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.backmeup.tests.integration.utils.Constants;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.ValidatableResponse;

/*
 * for examples rest-assured see:
 * https://github.com/jayway/rest-assured/tree/master/examples/rest-assured-itest-java/src/test/java/com/jayway/restassured/itest/java
 */

@Category(IntegrationTest.class)
public class BackupJobIntegrationTest extends IntegrationTestBase {
	
	@Test
	public void testGetBackupJobNew() {	
		String jobId = "1";
		try {
			given()
				.log().all()
				.header("Accept", "application/json")
			.when()
				.get("/backupjobs/" + jobId)
			.then()
				.log().all()
				.statusCode(200);
		} finally {
		}
	}
	
	@Test
	public void testGetBackupJobNewFull() {	
		String jobId = "1";
		try {
			given()
				.log().all()
				.header("Accept", "application/json")
			.when()
				.get("/backupjobs/" + jobId + "?expandUser=true&expandToken=true&expandProfiles=true&expandProtocol=true")
			.then()
				.log().all()
				.statusCode(200);
		} finally {
		}
	}
	
	@Test
	public void testGetBackupJobList() {	
		try {
			given()
				.log().all()
				.header("Accept", "application/json")
			.when()
				.get("/backupjobs/")
			.then()
				.log().all()
				.statusCode(200);
		} finally {
		}
	}
	
	@Test
	public void testGetBackupJobListFilter() {	
		try {
			given()
				.log().all()
				.header("Accept", "application/json")
			.when()
				.get("/backupjobs?jobStatus=queued")
			.then()
				.log().all()
				.statusCode(200);
		} finally {
		}
	}
	
	@Test
	public void testCreateBackupJobNew() {	
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String sourcePluginId = "org.backmeup.filegenerator";
		String sourceProfileName = "FilegeneratorProfile";
		PluginType sourceProfileType = PluginType.source;
		String sourceProfileId = "";
		
		String sinkPluginId   = "org.backmeup.dummy";
		String profileSinkName   = "DummySinkProfile";
		PluginType sinkProfileType = PluginType.sink;
		String sinkProfileId = "";
		
		String jobTitle = "BackupJob1";
		JobFrequency schedule = JobFrequency.weekly;
		Date start = new Date();

		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			PluginProfileDTO sourcePluginProfile = new PluginProfileDTO();
			sourcePluginProfile.setTitle(sourceProfileName);
			sourcePluginProfile.setPluginId(sourcePluginId);
			sourcePluginProfile.setProfileType(sourceProfileType);
			sourcePluginProfile.addConfigProperties("text", "true");
			sourcePluginProfile.addConfigProperties("image", "true");
			sourcePluginProfile.addConfigProperties("pdf", "true");
			sourcePluginProfile.addConfigProperties("binary", "true");
			
			response = BackMeUpUtils.addProfile(accessToken, sourcePluginId, sourcePluginProfile);
			sourceProfileId = response.extract().path("profileId").toString();
			
			PluginProfileDTO sinkPluginProfile = new PluginProfileDTO();
			sinkPluginProfile.setTitle(profileSinkName);
			sinkPluginProfile.setPluginId(sinkPluginId);
			sinkPluginProfile.setProfileType(sinkProfileType);
			
			response = BackMeUpUtils.addProfile(accessToken, sinkPluginId, sinkPluginProfile);
			sinkProfileId = response.extract().path("profileId").toString();
			
			BackupJobCreationDTO backupJob = new BackupJobCreationDTO();
			backupJob.setJobTitle(jobTitle);
			backupJob.setSchedule(schedule);
			backupJob.setStart(start);
			backupJob.setSource(Long.parseLong(sourceProfileId));
			backupJob.setSink(Long.parseLong(sinkProfileId));
			
			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
				.body(backupJob, ObjectMapperType.JACKSON_1)
			.when()
				.post("/backupjobs")
			.then()
				.log().all()
				.statusCode(200);
		} finally {
			//TODO: 
			// delete backupjob
			// delete profiles
			// delete user
		}
	}
	
	// ========================================================================
	// ========================================================================
	// ========================================================================

	@Test
	public void testGetBackupJobWrongUser() {
		String username = "UnknownUser";
		when()
			.get("/jobs/" + username)
		.then()
//			.log().all()
			.assertThat().statusCode(404)
			.body("errorMessage", equalTo("Unknown user"))
			.body("errorType", equalTo("org.backmeup.model.exceptions.UnknownUserException"));
	}
	
	@Test
	public void testGetBackupJobEmptyList() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
						
			when()
				.get("/jobs/" + username)
			.then()
//				.log().all()
				.statusCode(200)
				.body(containsString("backupJobs"));
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testCreateBackupJob() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String datasinkId   = "org.backmeup.dropbox";
		String profileSourceName = "DropboxSourceProfile";
		String profileSinkName   = "DropboxSinkProfile";
		String profileSourceId = "";
		String profileSinkId = "";
		
		String jobTitle = "TestUserJob1";
		String jobTimeExpression = "weekly";
		int jobId = 0;
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileSourceName, datasourceId);
			profileSourceId = response.extract().path("profileId");
			Properties profileSourceProps = new Properties();
			profileSourceProps.put(Constants.KEY_SOURCE_TOKEN, Constants.VALUE_SOURCE_TOKEN);
			profileSourceProps.put(Constants.KEY_SOURCE_SECRET, Constants.VALUE_SOURCE_SECRET);
			BackMeUpUtils.updateProfile(profileSourceId, password, profileSourceProps);
			
			response = BackMeUpUtils.authenticateDatasink(username, password, profileSinkName, datasinkId);
			profileSinkId = response.extract().path("profileId");
			Properties profileSinkProps = new Properties();
			profileSinkProps.put(Constants.KEY_SINK_TOKEN, Constants.VALUE_SINK_TOKEN);
			profileSinkProps.put(Constants.KEY_SINK_SECRET, Constants.VALUE_SINK_SECRET);
			BackMeUpUtils.updateProfile(profileSinkId, password, profileSinkProps);
					
			response = 
			given()
//				.log().all()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("keyRing", password)
				.formParam("sourceProfiles", profileSourceId)
				.formParam("sinkProfileId", profileSinkId)
				.formParam("timeExpression", jobTimeExpression)
				.formParam("jobTitle", jobTitle)
			.when()
				.post("/jobs/" + username)
			.then()
//				.log().all()
				.statusCode(200)
				.body(containsString("jobId"));
			
			jobId = response.extract().path("job.jobId");
			
		} finally {
			BackMeUpUtils.deleteBackupJob(username, jobId);
			BackMeUpUtils.deleteDatasourceProfile(username, profileSourceId);
			BackMeUpUtils.deleteDatasinkProfile(username, profileSinkId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	
//	@Test
//	public void cleanUser(){
//		String username = "TestUser1";
//		BackMeUpUtils.deleteUser(username);
//	}
	
	@Test
	public void testCreateBackupJobWrongSourceProfile() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String datasinkId   = "org.backmeup.dropbox";
		String profileSourceName = "DropboxSourceProfile";
		String profileSinkName   = "DropboxSinkProfile";
		String profileSourceId = "";
		String wrongProfileSourceId = "-1";
		String profileSinkId = "";
		
		String jobTitle = "TestUserJob1";
		String jobTimeExpression = "weekly";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileSourceName, datasourceId);
			profileSourceId = response.extract().path("profileId");
			Properties profileSourceProps = new Properties();
			profileSourceProps.put(Constants.KEY_SOURCE_TOKEN, Constants.VALUE_SOURCE_TOKEN);
			profileSourceProps.put(Constants.KEY_SOURCE_SECRET, Constants.VALUE_SOURCE_SECRET);
			BackMeUpUtils.updateProfile(profileSourceId, password, profileSourceProps);
			
			response = BackMeUpUtils.authenticateDatasink(username, password, profileSinkName, datasinkId);
			profileSinkId = response.extract().path("profileId");
			Properties profileSinkProps = new Properties();
			profileSinkProps.put(Constants.KEY_SINK_TOKEN, Constants.VALUE_SINK_TOKEN);
			profileSinkProps.put(Constants.KEY_SINK_SECRET, Constants.VALUE_SINK_SECRET);
			BackMeUpUtils.updateProfile(profileSinkId, password, profileSinkProps);
						
			response = 
			given()
//				.log().all()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("keyRing", password)
				.formParam("sourceProfiles", wrongProfileSourceId)
				.formParam("sinkProfileId", profileSinkId)
				.formParam("timeExpression", jobTimeExpression)
				.formParam("jobTitle", jobTitle)
			.when()
				.post("/jobs/" + username)
			.then()
//				.log().all()
				.statusCode(400)
				.body("errorMessage", equalTo("Unknown profile " + wrongProfileSourceId))
				.body("errorType", equalTo("java.lang.IllegalArgumentException"));
			
		} finally {
			BackMeUpUtils.deleteDatasourceProfile(username, profileSourceId);
			BackMeUpUtils.deleteDatasinkProfile(username, profileSinkId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testCreateBackupJobWrongPassword() {
		String username = "TestUser1";
		String password = "password1";
		String wrongPassword = "WRONGPW";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String datasinkId   = "org.backmeup.dropbox";
		String profileSourceName = "DropboxSourceProfile";
		String profileSinkName   = "DropboxSinkProfile";
		String profileSourceId = "";
		String profileSinkId = "";
		
		String jobTitle = "TestUserJob1";
		String jobTimeExpression = "weekly";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileSourceName, datasourceId);
			profileSourceId = response.extract().path("profileId");
			Properties profileSourceProps = new Properties();
			profileSourceProps.put(Constants.KEY_SOURCE_TOKEN, Constants.VALUE_SOURCE_TOKEN);
			profileSourceProps.put(Constants.KEY_SOURCE_SECRET, Constants.VALUE_SOURCE_SECRET);
			BackMeUpUtils.updateProfile(profileSourceId, password, profileSourceProps);
			
			response = BackMeUpUtils.authenticateDatasink(username, password, profileSinkName, datasinkId);
			profileSinkId = response.extract().path("profileId");
			Properties profileSinkProps = new Properties();
			profileSinkProps.put(Constants.KEY_SINK_TOKEN, Constants.VALUE_SINK_TOKEN);
			profileSinkProps.put(Constants.KEY_SINK_SECRET, Constants.VALUE_SINK_SECRET);
			BackMeUpUtils.updateProfile(profileSinkId, password, profileSinkProps);
						
			response = 
			given()
//				.log().all()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("keyRing", wrongPassword)
				.formParam("sourceProfiles", profileSourceId)
				.formParam("sinkProfileId", profileSinkId)
				.formParam("timeExpression", jobTimeExpression)
				.formParam("jobTitle", jobTitle)
			.when()
				.post("/jobs/" + username)
			.then()
//				.log().all()
				.statusCode(401)
				.body("errorMessage", equalTo("Invalid credentials"))
				.body("errorType", equalTo("org.backmeup.model.exceptions.InvalidCredentialsException"));
			
		} finally {
			BackMeUpUtils.deleteDatasourceProfile(username, profileSourceId);
			BackMeUpUtils.deleteDatasinkProfile(username, profileSinkId);
			BackMeUpUtils.deleteUser(username);
		}
	}
		
	@Test
	public void testDeleteBackupJob() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String datasinkId   = "org.backmeup.dropbox";
		String profileSourceName = "DropboxSourceProfile";
		String profileSinkName   = "DropboxSinkProfile";
		String profileSourceId = "";
		String profileSinkId = "";
		
		String jobTitle = "TestUserJob1";
		String jobTimeExpression = "weekly";
		int jobId = 0;
				
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileSourceName, datasourceId);
			profileSourceId = response.extract().path("profileId");
			Properties profileSourceProps = new Properties();
			profileSourceProps.put(Constants.KEY_SOURCE_TOKEN, Constants.VALUE_SOURCE_TOKEN);
			profileSourceProps.put(Constants.KEY_SOURCE_SECRET, Constants.VALUE_SOURCE_SECRET);
			BackMeUpUtils.updateProfile(profileSourceId, password, profileSourceProps);
			
			response = BackMeUpUtils.authenticateDatasink(username, password, profileSinkName, datasinkId);
			profileSinkId = response.extract().path("profileId");
			Properties profileSinkProps = new Properties();
			profileSinkProps.put(Constants.KEY_SINK_TOKEN, Constants.VALUE_SINK_TOKEN);
			profileSinkProps.put(Constants.KEY_SINK_SECRET, Constants.VALUE_SINK_SECRET);
			BackMeUpUtils.updateProfile(profileSinkId, password, profileSinkProps);
						
			response = BackMeUpUtils.createBackupJob(username, password, profileSourceId, profileSinkId, jobTimeExpression, jobTitle);
			jobId = response.extract().path("job.jobId");
			
			when()
				.delete("/jobs/" + username + "/" + jobId)
			.then()
				.statusCode(200);
			
		} finally {
			BackMeUpUtils.deleteDatasourceProfile(username, profileSourceId);
			BackMeUpUtils.deleteDatasinkProfile(username, profileSinkId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	/*
	@Test
	public void testValidateBackupJobNoErrors() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String datasinkId   = "org.backmeup.dropbox";
		String profileSourceName = "DropboxSourceProfile";
		String profileSinkName   = "DropboxSinkProfile";
		String profileSourceId = "";
		String profileSinkId = "";
		
		String jobTitle = "TestUserJob1";
		String jobTimeExpression = "weekly";
		int jobId = 0;
		
		ValidatableResponse response;
		try {
			response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileSourceName, datasourceId);
			profileSourceId = response.extract().path("profileId");
			Properties profileSourceProps = new Properties();
			profileSourceProps.put(Constants.KEY_SOURCE_TOKEN, Constants.VALUE_SOURCE_TOKEN);
			profileSourceProps.put(Constants.KEY_SOURCE_SECRET, Constants.VALUE_SOURCE_SECRET);
			BackMeUpUtils.updateProfile(profileSourceId, password, profileSourceProps);
			
			response = BackMeUpUtils.authenticateDatasink(username, password, profileSinkName, datasinkId);
			profileSinkId = response.extract().path("profileId");
			Properties profileSinkProps = new Properties();
			profileSinkProps.put(Constants.KEY_SINK_TOKEN, Constants.VALUE_SINK_TOKEN);
			profileSinkProps.put(Constants.KEY_SINK_SECRET, Constants.VALUE_SINK_SECRET);
			BackMeUpUtils.updateProfile(profileSinkId, password, profileSinkProps);
						
			response = BackMeUpUtils.createBackupJob(username, password, profileSourceId, profileSinkId, jobTimeExpression, jobTitle);
			jobId = response.extract().path("job.jobId");
			System.out.println("JobId = " + jobId);
			
			given()
				.log().all()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("keyRing", password)
			.when()
				.post("/jobs/" + username + "/validate/" + jobId)
			.then()
				.log().all();
//				.statusCode(200);
			
			Boolean hasErrors = response.extract().path("hasErrors");
			Assert.assertFalse(hasErrors);
			
		} finally {
			BackMeUpUtils.deleteBackupJob(username, jobId);
			BackMeUpUtils.deleteDatasourceProfile(username, profileSourceId);
			BackMeUpUtils.deleteDatasinkProfile(username, profileSinkId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	*/
	
	@Test
	public void testGetBackupJobStatus() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String datasinkId   = "org.backmeup.dropbox";
		String profileSourceName = "DropboxSourceProfile";
		String profileSinkName   = "DropboxSinkProfile";
		String profileSourceId = "";
		String profileSinkId = "";
		
		String jobTitle = "TestUserJob1";
		String jobTimeExpression = "weekly";
		int jobId = 0;
		
		ValidatableResponse response;
		try {
			response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileSourceName, datasourceId);
			profileSourceId = response.extract().path("profileId");
			Properties profileSourceProps = new Properties();
			profileSourceProps.put(Constants.KEY_SOURCE_TOKEN, Constants.VALUE_SOURCE_TOKEN);
			profileSourceProps.put(Constants.KEY_SOURCE_SECRET, Constants.VALUE_SOURCE_SECRET);
			BackMeUpUtils.updateProfile(profileSourceId, password, profileSourceProps);
			
			response = BackMeUpUtils.authenticateDatasink(username, password, profileSinkName, datasinkId);
			profileSinkId = response.extract().path("profileId");
			Properties profileSinkProps = new Properties();
			profileSinkProps.put(Constants.KEY_SINK_TOKEN, Constants.VALUE_SINK_TOKEN);
			profileSinkProps.put(Constants.KEY_SINK_SECRET, Constants.VALUE_SINK_SECRET);
			BackMeUpUtils.updateProfile(profileSinkId, password, profileSinkProps);
						
			response = BackMeUpUtils.createBackupJob(username, password, profileSourceId, profileSinkId, jobTimeExpression, jobTitle);
			jobId = response.extract().path("job.jobId");
//			System.out.println("JobId = " + jobId);
			
			given()
//				.log().all()
			.when()
				.get("/jobs/" + username + "/" + jobId + "/status")
			.then()
//				.log().all()
				.statusCode(200);
			
		} finally {
			BackMeUpUtils.deleteBackupJob(username, jobId);
			BackMeUpUtils.deleteDatasourceProfile(username, profileSourceId);
			BackMeUpUtils.deleteDatasinkProfile(username, profileSinkId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testGetBackupJob() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String datasinkId   = "org.backmeup.dropbox";
		String profileSourceName = "DropboxSourceProfile";
		String profileSinkName   = "DropboxSinkProfile";
		String profileSourceId = "";
		String profileSinkId = "";
		
		String jobTitle = "TestUserJob1";
		String jobTimeExpression = "weekly";
		int jobId = 0;
		
		ValidatableResponse response;
		try {
			response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileSourceName, datasourceId);
			profileSourceId = response.extract().path("profileId");
			Properties profileSourceProps = new Properties();
			profileSourceProps.put(Constants.KEY_SOURCE_TOKEN, Constants.VALUE_SOURCE_TOKEN);
			profileSourceProps.put(Constants.KEY_SOURCE_SECRET, Constants.VALUE_SOURCE_SECRET);
			BackMeUpUtils.updateProfile(profileSourceId, password, profileSourceProps);
			
			response = BackMeUpUtils.authenticateDatasink(username, password, profileSinkName, datasinkId);
			profileSinkId = response.extract().path("profileId");
			Properties profileSinkProps = new Properties();
			profileSinkProps.put(Constants.KEY_SINK_TOKEN, Constants.VALUE_SINK_TOKEN);
			profileSinkProps.put(Constants.KEY_SINK_SECRET, Constants.VALUE_SINK_SECRET);
			BackMeUpUtils.updateProfile(profileSinkId, password, profileSinkProps);
						
			response = BackMeUpUtils.createBackupJob(username, password, profileSourceId, profileSinkId, jobTimeExpression, jobTitle);
			jobId = response.extract().path("job.jobId");
			
			given()
//				.log().all()
			.when()
				.get("/jobs/" + username + "/" + jobId + "/full")
			.then()
				.log().all()
				.statusCode(200)
				.body("jobId", equalTo(jobId))
				.body("timeExpression", equalTo(jobTimeExpression))
				.body("onHold", equalTo(false))
				.body("jobTitle", equalTo(jobTitle))
				.body("status", equalTo("queued"))
				.body(containsString("createDate"))
				.body(containsString("modifyDate"))
				.body(containsString("nextBackup"))
				.body(containsString("datasources"))
				.body(containsString("datasink"))
				.body(containsString("actions"))
				.body(containsString("tokenId"))
				.body(containsString("token"));
				
		} finally {
			BackMeUpUtils.deleteBackupJob(username, jobId);
			BackMeUpUtils.deleteDatasourceProfile(username, profileSourceId);
			BackMeUpUtils.deleteDatasinkProfile(username, profileSinkId);
			BackMeUpUtils.deleteUser(username);
		}
	}
		
	@Test
	public void testGetBackupJobFileGen() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.filegenerator";
		String datasinkId   = "org.backmeup.dummy";
		String profileSourceName = "FileGeneratorSourceProfile";
		String profileSinkName   = "DummySinkProfile";
		String profileSourceId = "";
		String profileSinkId = "";
		
		String jobTitle = "TestUserJob1";
		String jobTimeExpression = "weekly";
		int jobId = 0;
		
		ValidatableResponse response;
		try {
			response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileSourceName, datasourceId);
			profileSourceId = response.extract().path("profileId");
			
			Properties dsProperties = new Properties();
			dsProperties.put("text", "true");
			dsProperties.put("image", "false");
			dsProperties.put("pdf", "false");
			dsProperties.put("binary", "false");
			BackMeUpUtils.postAuthenticateDatasource(username, password, profileSourceId, dsProperties);
			
			response = BackMeUpUtils.authenticateDatasink(username, password, profileSinkName, datasinkId);
			profileSinkId = response.extract().path("profileId");
						
			response = BackMeUpUtils.createBackupJob(username, password, profileSourceId, profileSinkId, jobTimeExpression, jobTitle);
			jobId = response.extract().path("job.jobId");
			
			given()
//				.log().all()
			.when()
				.get("/jobs/" + username + "/" + jobId + "/full")
			.then()
				.log().all()
				.statusCode(200)
				.body("jobId", equalTo(jobId))
				.body("timeExpression", equalTo(jobTimeExpression))
				.body("onHold", equalTo(false))
				.body("jobTitle", equalTo(jobTitle))
				.body("status", equalTo("queued"))
				.body(containsString("createDate"))
				.body(containsString("modifyDate"))
				.body(containsString("nextBackup"))
				.body(containsString("datasources"))
				.body(containsString("datasink"))
				.body(containsString("actions"))
				.body(containsString("tokenId"))
				.body(containsString("token"));
				
		} finally {
			BackMeUpUtils.deleteBackupJob(username, jobId);
			BackMeUpUtils.deleteDatasourceProfile(username, profileSourceId);
			BackMeUpUtils.deleteDatasinkProfile(username, profileSinkId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	/*
	@Test
	public void Cleanup() {
		String username = "TestUser1";
		int jobId = 24;
		BackMeUpUtils.deleteBackupJob(username, jobId);
//		BackMeUpUtils.deleteDatasinkProfile(username, profileSinkId);
		BackMeUpUtils.deleteUser(username);
	}
	*/
}
