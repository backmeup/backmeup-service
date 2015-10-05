package org.backmeup.tests.integration.utils;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.List;

import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.BackupJobCreationDTO;
import org.backmeup.model.dto.BackupJobExecutionDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.UserDTO;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;


public class BackMeUpUtils {
    // ========================================================================
    //  USER OPERATIONS
    // ------------------------------------------------------------------------

    public static ValidatableResponse addUser(UserDTO user){
        ValidatableResponse response = 
            given()
//                .log().all()
                .header("Accept", "application/json")
                .body(user, ObjectMapperType.JACKSON_1)
            .when()
                .post("/users/")
            .then()
//                .log().all()
                .statusCode(200)
                .body("username", equalTo(user.getUsername()))
                .body("firstname", equalTo(user.getFirstname()))
                .body("lastname", equalTo(user.getLastname()))
                .body("email", equalTo(user.getEmail()))
                .body("activated", equalTo(true))
                .body(containsString("userId"));

        return response;
    }
    
    public static ValidatableResponse addAnonymousUser(String accessToken){
        ValidatableResponse response = 
            given()
//                .log().all()
                .header("Authorization", accessToken)
            .when()
                .post("/users/anonymous")
            .then()
//                .log().all()
                .statusCode(200)
                .body("activated", equalTo(true))
                .body("anonymous", equalTo(true))
                .body(containsString("userId"))
                .body(containsString("username"))
                .body(containsString("email"));

        return response;
    }
    
    public static String getActivationCode(String accessToken, String userId){
        ValidatableResponse response = 
            given()
                .log().all()
                .header("Authorization", accessToken)
            .when()
                .get("/users/" + userId + "/activationCode")
            .then()
                .log().all()
                .statusCode(200)
                .body(containsString("activationCode"));

        return response.extract().path("activationCode").toString();
    }

    public static void deleteUser(String accessToken, String userId){
        given()
//            .log().all()
            .header("Authorization", accessToken)
        .when()
            .delete("/users/" + userId)
        .then()
//            .log().all()
            .statusCode(204);
    }

    public static String authenticateUser(UserDTO user) {
        ValidatableResponse response = 
            given()
//                .log().all()
                .header("Accept", "application/json")
            .when()
                .get("/authenticate?username=" + user.getUsername() + "&password=" + user.getPassword())
            .then()
//                .log().all()
                .statusCode(200)
                .body(containsString("accessToken"))
                .body(containsString("expiresAt"));

        return response.extract().path("accessToken");
    }
    
    public static String authenticateWorker(String workerId, String workerSecret) {
        ValidatableResponse response = 
                given()
//                    .log().all()
                    .header("Accept", "application/json")
                .when()
                    .get("/authenticate/worker?workerId=" + workerId + "&workerSecret=" + workerSecret)
                .then()
//                    .log().all()
                    .statusCode(200)
                    .body(containsString("accessToken"))
                    .body(containsString("expiresAt"));
        return response.extract().path("accessToken");
    }

    // ========================================================================
    //  PLUGIN OPERATIONS
    // ------------------------------------------------------------------------
    public static void deleteProfile(String accessToken, String pluginId, String profileId) {
        given()
//            .log().all()
            .header("Authorization", accessToken)
        .when()
            .delete("/plugins/" +  pluginId + "/" + profileId)
        .then()
//            .log().all()
            .statusCode(204);
    }

    public static ValidatableResponse addProfile(String accessToken, String pluginId, PluginProfileDTO pluginProfile) {		
        ValidatableResponse response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .body(pluginProfile, ObjectMapperType.JACKSON_1)
            .when()
                .post("/plugins/" + pluginId)
            .then()
                .log().all()
                .statusCode(200);

        return response;
    }

    public static PluginProfileDTO getProfile(String accessToken, String pluginId, String profileId) {
        return getProfile(accessToken, pluginId, Long.parseLong(profileId));
    }

    public static PluginProfileDTO getProfile(String accessToken, String pluginId, Long profileId) {
        Response response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/plugins/" + pluginId + "/" + profileId);
        return parseResponse(PluginProfileDTO.class, response);
    }
    
    public static List<BackupJobExecutionDTO> getBackupJobExecutions(String accessToken, String jobId) {
        Response response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/backupjobs/" + jobId + "/executions");
        
        return Arrays.asList(parseResponse(BackupJobExecutionDTO[].class, response));
    }

    private static <T> T parseResponse(Class<T> type, Response response) {
        return response.getBody().as(type);
    }


    // ========================================================================
    //  PROFILE OPERATIONS
    // ------------------------------------------------------------------------

    public static ValidatableResponse addAuthData(String accessToken, String pluginId, AuthDataDTO authData) {		
        ValidatableResponse response = 
            given()
//                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .body(authData, ObjectMapperType.JACKSON_1)
            .when()
                .post("/plugins/" + pluginId + "/authdata")
            .then()
//                .log().all()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo(authData.getName()));

        return response;
    }

    public static void deleteAuthData(String accessToken, String pluginId, String authDataId) {
        given()
            .log().all()
            .contentType("application/json")
            .header("Accept", "application/json")
            .header("Authorization", accessToken)
        .when()
            .delete("/plugins/" + pluginId + "/authdata/" + authDataId)
        .then()
            .log().all()
            .statusCode(204);
    }

    // ========================================================================
    //  BACKUPJOB OPERATIONS
    // ------------------------------------------------------------------------

    public static ValidatableResponse addBackupJob(String accessToken, BackupJobCreationDTO backupJob) {
        ValidatableResponse response = 
            given()
//                .log().all()
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .body(backupJob, ObjectMapperType.JACKSON_1)
            .when()
                .post("/backupjobs")
            .then()
//                .log().all()
                .body(containsString("jobId"))
                .statusCode(200);

        return response;
    }

    public static void deleteBackupJob(String accessToken, String jobId) {
        given()
//            .log().all()
            .header("Authorization", accessToken)
        .when()
            .delete("/backupjobs/" +  jobId)
        .then()
//            .log().all()
            .statusCode(204);
    }
}
