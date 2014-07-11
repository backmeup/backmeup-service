package org.backmeup.tests.integration.utils;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map.Entry;
import java.util.Properties;

import org.backmeup.model.dto.BackupJobCreationDTO;
import org.backmeup.model.dto.PluginDTO.PluginType;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.UserDTO;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.ValidatableResponse;


public class BackMeUpUtils {
	// ========================================================================
	//  USER OPERATIONS
	// ------------------------------------------------------------------------
	
	@Deprecated
	public static ValidatableResponse addUser(String username, String password,
			String keyRingPassword, String email) {
		throw new UnsupportedOperationException();
	}
	
	public static ValidatableResponse addUser(String username, String firstname, String lastname, String password, String email){
		UserDTO newUser = new UserDTO(username, firstname, lastname, password, email);
		return addUser(newUser);
	}
	
	public static ValidatableResponse addUser(UserDTO user){
		ValidatableResponse response = 
			given()
//				.log().all()
				.header("Accept", "application/json")
				.body(user, ObjectMapperType.JACKSON_1)
			.when()
				.post("/users/")
			.then()
//				.log().all()
				.statusCode(200)
				.body("username", equalTo(user.getUsername()))
				.body("firstname", equalTo(user.getFirstname()))
				.body("lastname", equalTo(user.getLastname()))
				.body("email", equalTo(user.getEmail()))
				.body("activated", equalTo(true))
				.body(containsString("userId"));
		
		return response;
	}
	
	@Deprecated
	public static void deleteUser(String userId){
		throw new UnsupportedOperationException();
	}
	
	public static void deleteUser(String accessToken, String userId){
		given()
//			.log().all()
			.header("Authorization", accessToken)
		.when()
			.delete("/users/" + userId)
		.then()
//			.log().all()
			.statusCode(204);
	}
	
	@Deprecated
	public static void verifyEmail(String verificationKey){
		throw new UnsupportedOperationException();
	}
	
	// ========================================================================
	//  PLUGIN OPERATIONS
	// ------------------------------------------------------------------------
	public static void deleteProfile(String accessToken, String pluginId, String profileId) {
		given()
//			.log().all()
			.header("Authorization", accessToken)
		.when()
			.delete("/plugins/" +  pluginId + "/" + profileId)
		.then()
//			.log().all()
			.statusCode(204);
	}
	
	public static ValidatableResponse addProfile(String accessToken, String pluginId, String profileName, PluginType profileType, Properties configProps) {		
		PluginProfileDTO pluginProfile = new PluginProfileDTO();
		pluginProfile.setTitle(profileName);
		pluginProfile.setPluginId(pluginId);
		pluginProfile.setProfileType(profileType);
		
		for(Entry<?,?> entry : configProps.entrySet()) {
			String key = (String) entry.getKey();  
			String value = (String) entry.getValue();  
			pluginProfile.addConfigProperties(key, value);
		}
		
		return addProfile(accessToken, pluginId, pluginProfile);
	}
	
	public static ValidatableResponse addProfile(String accessToken, String pluginId, PluginProfileDTO pluginProfile) {		
		ValidatableResponse response = 
		given()
//			.log().all()
			.contentType("application/json")
			.header("Accept", "application/json")
			.header("Authorization", accessToken)
			.body(pluginProfile, ObjectMapperType.JACKSON_1)
		.when()
			.post("/plugins/" + pluginId)
		.then()
//			.log().all()
			.statusCode(200);
		
		return response;
	}
	
	
	// ========================================================================
	//  DATASOURCE OPERATIONS
	// ------------------------------------------------------------------------
	
	@Deprecated
	public static ValidatableResponse authenticateDatasource(String username, String password, String profileName, String datasourceId) {
		throw new UnsupportedOperationException();
	}
	
	@Deprecated
	public static ValidatableResponse postAuthenticateDatasource(String username, String password, String profileId, Properties datasourceParameters) {
		throw new UnsupportedOperationException();
	}
	
	@Deprecated
	public static void deleteDatasourceProfile(String username, String profileId) {			
		throw new UnsupportedOperationException();
	}
	
	// ========================================================================
	//  DATASINK OPERATIONS
	// ------------------------------------------------------------------------
	
	@Deprecated
	public static ValidatableResponse authenticateDatasink(String username, String password, String profileName, String datasinkId) {
		throw new UnsupportedOperationException();
	}
	
	@Deprecated
	public static void deleteDatasinkProfile(String username, String profileId) {			
		throw new UnsupportedOperationException();
	}	
	
	// ========================================================================
	//  PROFILE OPERATIONS
	// ------------------------------------------------------------------------
	
	@Deprecated
	public static void updateProfile(String profileId, String password, Properties profileProps) {
		throw new UnsupportedOperationException();
	}
	
	// ========================================================================
	//  BACKUPJOB OPERATIONS
	// ------------------------------------------------------------------------
	
	public static ValidatableResponse addBackupJob(String accessToken, BackupJobCreationDTO backupJob) {
		ValidatableResponse response = 
		given()
//			.log().all()
			.header("Accept", "application/json")
			.header("Authorization", accessToken)
			.body(backupJob, ObjectMapperType.JACKSON_1)
		.when()
			.post("/backupjobs")
		.then()
//			.log().all()
			.body(containsString("jobId"))
			.statusCode(200);
			
		return response;
	}
	
	public static void deleteBackupJob(String accessToken, String jobId) {
		given()
//			.log().all()
			.header("Authorization", accessToken)
		.when()
			.delete("/backupjobs/" +  jobId)
		.then()
//			.log().all()
			.statusCode(204);
	}
	
	@Deprecated
	public static ValidatableResponse createBackupJob(String username, String password, String profileSourceId, String profileSinkId, String jobTimeExpression, String jobTitle) {
		throw new UnsupportedOperationException();
	}
	
	@Deprecated	
	public static void deleteBackupJob(String username, int jobId) {
		throw new UnsupportedOperationException();
	}
}
