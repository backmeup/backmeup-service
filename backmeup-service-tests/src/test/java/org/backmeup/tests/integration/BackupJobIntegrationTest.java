package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;

import org.backmeup.model.constants.JobExecutionStatus;
import org.backmeup.model.dto.BackupJobCreationDTO;
import org.backmeup.model.dto.BackupJobExecutionDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.UserDTO;
import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.backmeup.tests.integration.utils.TestDataManager;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.ValidatableResponse;

@Category(IntegrationTest.class)
public class BackupJobIntegrationTest extends IntegrationTestBase {
    @Test
    public void testGetBackupJobDummy() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileFilegenerator();
        String sourceProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileDummySink();
        String sinkProfileId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId);

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
                .body("jobTitle", equalTo(backupJob.getJobTitle()))
                .body("schedule", equalTo(backupJob.getSchedule().toString()))
                .body(containsString("status"))
                .body(containsString("active"))
                .body(containsString("created"))
                .body(containsString("modified"))
                .body(containsString("next"))
                .body(containsString("delay"));
        } finally {
            BackMeUpUtils.deleteBackupJob(accessToken, jobId);
            BackMeUpUtils.deleteProfile(accessToken, sourcePluginProfile.getPluginId(), sourceProfileId);
            BackMeUpUtils.deleteProfile(accessToken, sinkPluginProfile.getPluginId(), sinkProfileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testGetBackupJobDummyAllExpanded() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileFilegenerator();
        String sourceProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileDummySink();
        String sinkProfileId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId);
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
                .body("jobTitle", equalTo(backupJob.getJobTitle()))
                .body("schedule", equalTo(backupJob.getSchedule().toString()))
                .body("user.userId", equalTo(Integer.parseInt(userId)))
                .body("source.profileId", equalTo(Integer.parseInt(sourceProfileId)))
                .body("sink.profileId", equalTo(Integer.parseInt(sinkProfileId)))
                .body(containsString("status"))
                .body(containsString("active"))
                .body(containsString("created"))
                .body(containsString("modified"))
                .body(containsString("next"))
                .body(containsString("delay"))
                .body(containsString("user"))
                .body(containsString("token"))
                .body(containsString("source"))
                .body(containsString("sink"));


        } finally {
            BackMeUpUtils.deleteBackupJob(accessToken, jobId);
            BackMeUpUtils.deleteProfile(accessToken, sourcePluginProfile.getPluginId(), sourceProfileId);
            BackMeUpUtils.deleteProfile(accessToken, sinkPluginProfile.getPluginId(), sinkProfileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testGetBackupJobList() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileDummySource();
        String sourceProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileDummySink();
        String sinkProfileId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId);
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
            BackMeUpUtils.deleteProfile(accessToken, sourcePluginProfile.getPluginId(), sourceProfileId);
            BackMeUpUtils.deleteProfile(accessToken, sinkPluginProfile.getPluginId(), sinkProfileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testGetBackupJobListFilter() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileDummySource();
        String sourceProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileDummySink();
        String sinkProfileId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId);
            response = BackMeUpUtils.addBackupJob(accessToken, backupJob);
            jobId = response.extract().path("jobId").toString();

            given()
                .log().all()
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/backupjobs?jobStatus=CREATED")
            .then()
                .log().all()
                .statusCode(200);
        } finally {
            BackMeUpUtils.deleteBackupJob(accessToken, jobId);
            BackMeUpUtils.deleteProfile(accessToken, sourcePluginProfile.getPluginId(), sourceProfileId);
            BackMeUpUtils.deleteProfile(accessToken, sinkPluginProfile.getPluginId(), sinkProfileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testGetBackupJobListEmpty() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            given()
                .log().all()
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/backupjobs?jobStatus=ACTIVE")
            .then()
                .log().all()
                .statusCode(200);
        } finally {
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }
    
    @Test
    public void testGetBackupJobExecutionList() throws InterruptedException {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileFilegenerator();
        String sourceProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileDummySink();
        String sinkProfileId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId);

            response = BackMeUpUtils.addBackupJob(accessToken, backupJob);
            jobId = response.extract().path("jobId").toString();
            
            // Wait for 5 seconds to make sure the job execution is created. 
            Thread.sleep(5000);

            given()
                .log().all()
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/backupjobs/" + jobId + "/executions")
            .then()
                .log().all()
                .statusCode(200);
        } finally {
            BackMeUpUtils.deleteBackupJob(accessToken, jobId);
            BackMeUpUtils.deleteProfile(accessToken, sourcePluginProfile.getPluginId(), sourceProfileId);
            BackMeUpUtils.deleteProfile(accessToken, sinkPluginProfile.getPluginId(), sinkProfileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }
    
    @Test
    public void testGetBackupJobExecutionDummy() throws InterruptedException {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileDummySource();
        String sourceProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileDummySink();
        String sinkProfileId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId);

            response = BackMeUpUtils.addBackupJob(accessToken, backupJob);
            jobId = response.extract().path("jobId").toString();
            
            // Wait for 5 seconds to make sure the job execution is created. 
            Thread.sleep(5000);

            List<BackupJobExecutionDTO> jobExecutions = BackMeUpUtils.getBackupJobExecutions(accessToken, jobId);
            String jobExecId = jobExecutions.get(0).getId().toString();
            
            given()
                .log().all()
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/backupjobs/" + jobId + "/executions/" + jobExecId)
            .then()
                .log().all()
                .statusCode(200)
                .body("jobId", equalTo(Integer.parseInt(jobId)))
                .body("status", equalTo(JobExecutionStatus.QUEUED.toString()))
                .body(containsString("id"))
                .body(containsString("name"))
                .body(containsString("created"));
            
        } finally {
            BackMeUpUtils.deleteBackupJob(accessToken, jobId);
            BackMeUpUtils.deleteProfile(accessToken, sourcePluginProfile.getPluginId(), sourceProfileId);
            BackMeUpUtils.deleteProfile(accessToken, sinkPluginProfile.getPluginId(), sinkProfileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }
    
    @Test
    public void testGetBackupJobExecutionDummyAllExpanded() throws InterruptedException {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileDummySource();
        String sourceProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileDummySink();
        String sinkProfileId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId);

            response = BackMeUpUtils.addBackupJob(accessToken, backupJob);
            jobId = response.extract().path("jobId").toString();
            
            // Wait for 5 seconds to make sure the job execution is created. 
            Thread.sleep(5000);

            List<BackupJobExecutionDTO> jobExecutions = BackMeUpUtils.getBackupJobExecutions(accessToken, jobId);
            String jobExecId = jobExecutions.get(0).getId().toString();
            
            given()
                .log().all()
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/backupjobs/" + jobId + "/executions/" + jobExecId  + "?expandUser=true&expandToken=true&expandProfiles=true")
            .then()
                .log().all()
                .statusCode(200)
                .body("jobId", equalTo(Integer.parseInt(jobId)))
                .body("status", equalTo(JobExecutionStatus.QUEUED.toString()))
                .body("user.userId", equalTo(Integer.parseInt(userId)))
                .body("source.profileId", equalTo(Integer.parseInt(sourceProfileId)))
                .body("sink.profileId", equalTo(Integer.parseInt(sinkProfileId)))
                .body(containsString("id"))
                .body(containsString("name"))
                .body(containsString("created"))
                .body(containsString("modified"))
                .body(containsString("user"))
                .body(containsString("token"))
                .body(containsString("source"))
                .body(containsString("actions"))
                .body(containsString("sink"));
            
        } finally {
            BackMeUpUtils.deleteBackupJob(accessToken, jobId);
            BackMeUpUtils.deleteProfile(accessToken, sourcePluginProfile.getPluginId(), sourceProfileId);
            BackMeUpUtils.deleteProfile(accessToken, sinkPluginProfile.getPluginId(), sinkProfileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testCreateBackupJobDummyToDummy() throws InterruptedException {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileDummySource();
        String sourceProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileDummySink();
        String sinkProfileId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId);

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
            
            // Wait for 2 seconds to make sure we are not deleting the 
            // job while creating the job execution
            Thread.sleep(5000);
        } finally {
            BackMeUpUtils.deleteBackupJob(accessToken, jobId);
            BackMeUpUtils.deleteProfile(accessToken, sourcePluginProfile.getPluginId(), sourceProfileId);
            BackMeUpUtils.deleteProfile(accessToken, sinkPluginProfile.getPluginId(), sinkProfileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testCreateBackupJobFilegeneratorToDummy() throws InterruptedException {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileFilegenerator();
        String sourceProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileDummySink();
        String sinkProfileId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId);

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
            
            // Wait for 2 seconds to make sure we are not deleting the 
            // job while creating the job execution
            Thread.sleep(5000);
        } finally {
            BackMeUpUtils.deleteBackupJob(accessToken, jobId);
            BackMeUpUtils.deleteProfile(accessToken, sourcePluginProfile.getPluginId(), sourceProfileId);
            BackMeUpUtils.deleteProfile(accessToken, sinkPluginProfile.getPluginId(), sinkProfileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Ignore
    @Test
    public void testCreateBackupJobFilegeneratorToBackmeupStorage() throws InterruptedException {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileFilegenerator();
        String sourceProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileBackmeupStorageSink();
        String sinkProfileId = "";
        String sinkAuthDataId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addAuthData(accessToken, sinkPluginProfile.getPluginId(),
                    sinkPluginProfile.getAuthData());
            sinkAuthDataId = response.extract().path("id").toString();
            sinkPluginProfile.getAuthData().setId(Long.parseLong(sinkAuthDataId));

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId);

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
            
            // Wait for 2 seconds to make sure we are not deleting the 
            // job while creating the job execution
            Thread.sleep(5000);
        } finally {
            BackMeUpUtils.deleteBackupJob(accessToken, jobId);
            BackMeUpUtils.deleteProfile(accessToken, sourcePluginProfile.getPluginId(), sourceProfileId);
            BackMeUpUtils.deleteProfile(accessToken, sinkPluginProfile.getPluginId(), sinkProfileId);
            BackMeUpUtils.deleteAuthData(accessToken, sinkPluginProfile.getPluginId(), sinkAuthDataId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testDeleteBackupJob() throws InterruptedException {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileDummySource();
        String sourceProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileDummySink();
        String sinkProfileId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId);
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
            
            // Wait for 2 seconds to make sure we are not deleting the 
            // job while creating the job execution
            Thread.sleep(5000);

            given()
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/backupjobs/" + jobId)
            .then()
                .statusCode(500); // Internal Server Error is thrown
            
        } finally {
            BackMeUpUtils.deleteProfile(accessToken, sourcePluginProfile.getPluginId(), sourceProfileId);
            BackMeUpUtils.deleteProfile(accessToken, sinkPluginProfile.getPluginId(), sinkProfileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Ignore("truncate db and proper bundle setup required before running this test. No cleanup performed afterwards")
    @Test
    public void testCreateBackupJobWithActionAndExecuteViaWorker() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";
        List<String> actionProfiles = new ArrayList<>();

        PluginProfileDTO sourcePluginProfile = TestDataManager.getProfileFilegenerator();
        String sourceProfileId = "";

        PluginProfileDTO thumbnailActionPluginProfile = TestDataManager.getProfileThumbnailAction();
        String thumbnailActionProfileId = "";

        PluginProfileDTO sinkPluginProfile = TestDataManager.getProfileBackmeupStorageSink();
        String sinkProfileId = "";
        String sinkAuthDataId = "";

        String jobId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, sourcePluginProfile.getPluginId(), sourcePluginProfile);
            sourceProfileId = response.extract().path("profileId").toString();

            response = BackMeUpUtils.addProfile(accessToken, thumbnailActionPluginProfile.getPluginId(),
                    thumbnailActionPluginProfile);
            thumbnailActionProfileId = response.extract().path("profileId").toString();
            actionProfiles.add(thumbnailActionProfileId);

            response = BackMeUpUtils.addAuthData(accessToken, sinkPluginProfile.getPluginId(),
                    sinkPluginProfile.getAuthData());
            sinkAuthDataId = response.extract().path("id").toString();
            System.out.println("Sink AuthData Id: " + sinkAuthDataId);
            sinkPluginProfile.getAuthData().setId(Long.parseLong(sinkAuthDataId));

            response = BackMeUpUtils.addProfile(accessToken, sinkPluginProfile.getPluginId(), sinkPluginProfile);
            sinkProfileId = response.extract().path("profileId").toString();

            BackupJobCreationDTO backupJob = TestDataManager.getBackupJob(sourceProfileId, sinkProfileId,
                    actionProfiles);

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
        }
    }
}
