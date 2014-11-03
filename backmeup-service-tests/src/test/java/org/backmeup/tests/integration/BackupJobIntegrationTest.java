package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.Date;

import org.backmeup.model.dto.BackupJobCreationDTO;
import org.backmeup.model.dto.BackupJobDTO.JobFrequency;
import org.backmeup.model.dto.BackupJobDTO.JobStatus;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.ValidatableResponse;


@Category(IntegrationTest.class)
public class BackupJobIntegrationTest extends IntegrationTestBase {
	@Test
	public void testGetBackupJobDummy() {			
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String sourcePluginId = "org.backmeup.filegenerator";
		String sourceProfileName = "FilegeneratorProfile";
		PluginType sourceProfileType = PluginType.Source;
		String sourceProfileId = "";
		
		String sinkPluginId   = "org.backmeup.dummy";
		String sinkProfileName   = "DummySinkProfile";
		PluginType sinkProfileType = PluginType.Sink;
		String sinkProfileId = "";
		
		String jobTitle = "BackupJob1";
		JobFrequency schedule = JobFrequency.weekly;
		Date start = new Date();
		String jobId = "";

		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			PluginProfileDTO sourcePluginProfile = new PluginProfileDTO();
			sourcePluginProfile.setTitle(sourceProfileName);
			sourcePluginProfile.setPluginId(sourcePluginId);
			sourcePluginProfile.setProfileType(sourceProfileType);
			sourcePluginProfile.addProperty("text", "true");
			sourcePluginProfile.addProperty("image", "true");
			sourcePluginProfile.addProperty("pdf", "true");
			sourcePluginProfile.addProperty("binary", "true");
			
			response = BackMeUpUtils.addProfile(accessToken, sourcePluginId, sourcePluginProfile);
			sourceProfileId = response.extract().path("profileId").toString();
						
			response = BackMeUpUtils.addProfile(accessToken, sinkPluginId, sinkProfileName, sinkProfileType, null, null, null);
			sinkProfileId = response.extract().path("profileId").toString();
			
			BackupJobCreationDTO backupJob = new BackupJobCreationDTO();
			backupJob.setJobTitle(jobTitle);
			backupJob.setSchedule(schedule);
			backupJob.setStart(start);
			backupJob.setSource(Long.parseLong(sourceProfileId));
			backupJob.setSink(Long.parseLong(sinkProfileId));
			
			response = BackMeUpUtils.addBackupJob(accessToken, backupJob);
			jobId = response.extract().path("jobId").toString();
			
			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/backupjobs/" + jobId)
			.then()
				.log().all()
				.statusCode(200)
				.body("jobId", equalTo(Integer.parseInt(jobId)))
				.body("jobTitle", equalTo(jobTitle))
				.body("jobStatus", equalTo(JobStatus.queued.toString()))
				.body("onHold", equalTo(false))
				.body("schedule", equalTo(schedule.toString()))
				.body(containsString("created"))
				.body(containsString("modified"))
				.body(containsString("start"))
				.body(containsString("delay"));
		} finally {
			BackMeUpUtils.deleteBackupJob(accessToken, jobId);
			BackMeUpUtils.deleteProfile(accessToken, sourcePluginId, sourceProfileId);
			BackMeUpUtils.deleteProfile(accessToken, sinkPluginId, sinkProfileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testGetBackupJobDummyAllExpanded() {			
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String sourcePluginId = "org.backmeup.filegenerator";
		String sourceProfileName = "FilegeneratorProfile";
		PluginType sourceProfileType = PluginType.Source;
		String sourceProfileId = "";
		
		String sinkPluginId   = "org.backmeup.dummy";
		String sinkProfileName   = "DummySinkProfile";
		PluginType sinkProfileType = PluginType.Sink;
		String sinkProfileId = "";
		
		String jobTitle = "BackupJob1";
		JobFrequency schedule = JobFrequency.weekly;
		Date start = new Date();
		String jobId = "";

		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;

			PluginProfileDTO sourcePluginProfile = new PluginProfileDTO();
			sourcePluginProfile.setTitle(sourceProfileName);
			sourcePluginProfile.setPluginId(sourcePluginId);
			sourcePluginProfile.setProfileType(sourceProfileType);
			sourcePluginProfile.addProperty("text", "true");
			sourcePluginProfile.addProperty("image", "true");
			sourcePluginProfile.addProperty("pdf", "true");
			sourcePluginProfile.addProperty("binary", "true");
			
			response = BackMeUpUtils.addProfile(accessToken, sourcePluginId, sourcePluginProfile);
			sourceProfileId = response.extract().path("profileId").toString();
						
			response = BackMeUpUtils.addProfile(accessToken, sinkPluginId, sinkProfileName, sinkProfileType, null, null, null);
			sinkProfileId = response.extract().path("profileId").toString();

			BackupJobCreationDTO backupJob = new BackupJobCreationDTO();
			backupJob.setJobTitle(jobTitle);
			backupJob.setSchedule(schedule);
			backupJob.setStart(start);
			backupJob.setSource(Long.parseLong(sourceProfileId));
			backupJob.setSink(Long.parseLong(sinkProfileId));
			
			response = BackMeUpUtils.addBackupJob(accessToken, backupJob);
			jobId = response.extract().path("jobId").toString();

			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/backupjobs/" + jobId + "?expandUser=true&expandToken=true&expandProfiles=true&expandProtocol=true")
			.then()
				.log().all()
				.statusCode(200)
				.body("jobId", equalTo(Integer.parseInt(jobId)))
				.body("jobTitle", equalTo(jobTitle))
				.body("jobStatus", equalTo(JobStatus.queued.toString()))
				.body("onHold", equalTo(false))
				.body("schedule", equalTo(schedule.toString()))
				.body(containsString("created"))
				.body(containsString("modified"))
				.body(containsString("start"))
				.body(containsString("delay"))
				.body(containsString("user"))
				.body(containsString("token"))
				.body(containsString("source"))
				.body(containsString("sink"));

		} finally {
			BackMeUpUtils.deleteBackupJob(accessToken, jobId);
			BackMeUpUtils.deleteProfile(accessToken, sourcePluginId, sourceProfileId);
			BackMeUpUtils.deleteProfile(accessToken, sinkPluginId, sinkProfileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testGetBackupJobList() {	
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String sourcePluginId = "org.backmeup.dummy";
		String sourceProfileName = "DummySourceProfile";
		PluginType sourceProfileType = PluginType.Source;
		String sourceProfileId = "";
		
		String sinkPluginId   = "org.backmeup.dummy";
		String sinkProfileName   = "DummySinkProfile";
		PluginType sinkProfileType = PluginType.Sink;
		String sinkProfileId = "";
		
		String jobTitle = "BackupJob1";
		JobFrequency schedule = JobFrequency.weekly;
		Date start = new Date();
		String jobId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			response = BackMeUpUtils.addProfile(accessToken, sourcePluginId, sourceProfileName, sourceProfileType, null, null, null);
			sourceProfileId = response.extract().path("profileId").toString();
						
			response = BackMeUpUtils.addProfile(accessToken, sinkPluginId, sinkProfileName, sinkProfileType, null, null, null);
			sinkProfileId = response.extract().path("profileId").toString();

			BackupJobCreationDTO backupJob = new BackupJobCreationDTO();
			backupJob.setJobTitle(jobTitle);
			backupJob.setSchedule(schedule);
			backupJob.setStart(start);
			backupJob.setSource(Long.parseLong(sourceProfileId));
			backupJob.setSink(Long.parseLong(sinkProfileId));
			
			response = BackMeUpUtils.addBackupJob(accessToken, backupJob);
			jobId = response.extract().path("jobId").toString();
			
			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/backupjobs/")
			.then()
				.log().all()
				.statusCode(200);
		} finally {
			BackMeUpUtils.deleteBackupJob(accessToken, jobId);
			BackMeUpUtils.deleteProfile(accessToken, sourcePluginId, sourceProfileId);
			BackMeUpUtils.deleteProfile(accessToken, sinkPluginId, sinkProfileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testGetBackupJobListFilter() {	
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String sourcePluginId = "org.backmeup.dummy";
		String sourceProfileName = "DummySourceProfile";
		PluginType sourceProfileType = PluginType.Source;
		String sourceProfileId = "";
		
		String sinkPluginId   = "org.backmeup.dummy";
		String sinkProfileName   = "DummySinkProfile";
		PluginType sinkProfileType = PluginType.Sink;
		String sinkProfileId = "";
		
		String jobTitle = "BackupJob1";
		JobFrequency schedule = JobFrequency.weekly;
		Date start = new Date();
		String jobId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			response = BackMeUpUtils.addProfile(accessToken, sourcePluginId, sourceProfileName, sourceProfileType, null, null, null);
			sourceProfileId = response.extract().path("profileId").toString();
						
			response = BackMeUpUtils.addProfile(accessToken, sinkPluginId, sinkProfileName, sinkProfileType, null, null, null);
			sinkProfileId = response.extract().path("profileId").toString();

			BackupJobCreationDTO backupJob = new BackupJobCreationDTO();
			backupJob.setJobTitle(jobTitle);
			backupJob.setSchedule(schedule);
			backupJob.setStart(start);
			backupJob.setSource(Long.parseLong(sourceProfileId));
			backupJob.setSink(Long.parseLong(sinkProfileId));
			
			response = BackMeUpUtils.addBackupJob(accessToken, backupJob);
			jobId = response.extract().path("jobId").toString();

			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/backupjobs?jobStatus=queued")
			.then()
				.log().all()
				.statusCode(200);
		} finally {
			BackMeUpUtils.deleteBackupJob(accessToken, jobId);
			BackMeUpUtils.deleteProfile(accessToken, sourcePluginId, sourceProfileId);
			BackMeUpUtils.deleteProfile(accessToken, sinkPluginId, sinkProfileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testGetBackupJobListEmpty() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
						
			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/backupjobs?jobStatus=queued")
			.then()
				.log().all()
				.statusCode(200);
		} finally {
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testCreateBackupJobDummy() {	
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String sourcePluginId = "org.backmeup.dummy";
		String sourceProfileName = "DummySourceProfile";
		PluginType sourceProfileType = PluginType.Source;
		String sourceProfileId = "";
		
		String sinkPluginId   = "org.backmeup.dummy";
		String sinkProfileName   = "DummySinkProfile";
		PluginType sinkProfileType = PluginType.Sink;
		String sinkProfileId = "";
		
		String jobTitle = "BackupJob1";
		JobFrequency schedule = JobFrequency.weekly;
		Date start = new Date();
		String jobId = "";

		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
							
			response = BackMeUpUtils.addProfile(accessToken, sourcePluginId, sourceProfileName, sourceProfileType, null, null, null);
			sourceProfileId = response.extract().path("profileId").toString();
						
			response = BackMeUpUtils.addProfile(accessToken, sinkPluginId, sinkProfileName, sinkProfileType, null, null, null);
			sinkProfileId = response.extract().path("profileId").toString();
			
			BackupJobCreationDTO backupJob = new BackupJobCreationDTO();
			backupJob.setJobTitle(jobTitle);
			backupJob.setSchedule(schedule);
			backupJob.setStart(start);
			backupJob.setSource(Long.parseLong(sourceProfileId));
			backupJob.setSink(Long.parseLong(sinkProfileId));
			
			response = 
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
			
			jobId = response.extract().path("jobId").toString();
		} finally {
			BackMeUpUtils.deleteBackupJob(accessToken, jobId);
			BackMeUpUtils.deleteProfile(accessToken, sourcePluginId, sourceProfileId);
			BackMeUpUtils.deleteProfile(accessToken, sinkPluginId, sinkProfileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testCreateBackupJobFilegenerator() {	
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String sourcePluginId = "org.backmeup.filegenerator";
		String sourceProfileName = "FilegeneratorProfile";
		PluginType sourceProfileType = PluginType.Source;
		String sourceProfileId = "";
		
		String sinkPluginId   = "org.backmeup.dummy";
		String profileSinkName   = "DummySinkProfile";
		PluginType sinkProfileType = PluginType.Sink;
		String sinkProfileId = "";
		
		String jobTitle = "BackupJob1";
		JobFrequency schedule = JobFrequency.weekly;
		Date start = new Date();
		String jobId = "";

		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			PluginProfileDTO sourcePluginProfile = new PluginProfileDTO();
			sourcePluginProfile.setTitle(sourceProfileName);
			sourcePluginProfile.setPluginId(sourcePluginId);
			sourcePluginProfile.setProfileType(sourceProfileType);
			sourcePluginProfile.addProperty("text", "true");
			sourcePluginProfile.addProperty("image", "true");
			sourcePluginProfile.addProperty("pdf", "true");
			sourcePluginProfile.addProperty("binary", "true");
			
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
			
			response = 
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
			
			jobId = response.extract().path("jobId").toString();
		} finally {
			BackMeUpUtils.deleteBackupJob(accessToken, jobId);
			BackMeUpUtils.deleteProfile(accessToken, sourcePluginId, sourceProfileId);
			BackMeUpUtils.deleteProfile(accessToken, sinkPluginId, sinkProfileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testDeleteBackupJob() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String sourcePluginId = "org.backmeup.dummy";
		String sourceProfileName = "DummySourceProfile";
		PluginType sourceProfileType = PluginType.Source;
		String sourceProfileId = "";
		
		String sinkPluginId   = "org.backmeup.dummy";
		String sinkProfileName   = "DummySinkProfile";
		PluginType sinkProfileType = PluginType.Sink;
		String sinkProfileId = "";
		
		String jobTitle = "BackupJob1";
		JobFrequency schedule = JobFrequency.weekly;
		Date start = new Date();
		String jobId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			response = BackMeUpUtils.addProfile(accessToken, sourcePluginId, sourceProfileName, sourceProfileType, null, null, null);
			sourceProfileId = response.extract().path("profileId").toString();
						
			response = BackMeUpUtils.addProfile(accessToken, sinkPluginId, sinkProfileName, sinkProfileType, null, null, null);
			sinkProfileId = response.extract().path("profileId").toString();

			BackupJobCreationDTO backupJob = new BackupJobCreationDTO();
			backupJob.setJobTitle(jobTitle);
			backupJob.setSchedule(schedule);
			backupJob.setStart(start);
			backupJob.setSource(Long.parseLong(sourceProfileId));
			backupJob.setSink(Long.parseLong(sinkProfileId));
			
			response = BackMeUpUtils.addBackupJob(accessToken, backupJob);
			jobId = response.extract().path("jobId").toString();
			
			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.delete("/backupjobs/" + jobId)
			.then()
				.log().all()
				.statusCode(204);
			
			given()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/backupjobs/" + jobId)
			.then()
				.statusCode(500); // Internal Server Error is thrown
		} finally {
			BackMeUpUtils.deleteProfile(accessToken, sourcePluginId, sourceProfileId);
			BackMeUpUtils.deleteProfile(accessToken, sinkPluginId, sinkProfileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
}
