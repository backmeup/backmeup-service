package org.backmeup.tests.integration.utils;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import java.util.Properties;
import java.util.Map.Entry;

import org.backmeup.model.dto.UserDTO;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;


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
	
	public static void verifyEmail(String verificationKey){
		throw new UnsupportedOperationException();
	}
	
//	public static ValidatableResponse getUser(String username) {		
//	ValidatableResponse response = 
//	when()
//		.get("/users/" + username)
//	.then()
//		.statusCode(200)
//		.body(containsString("userId"))
//		.body(containsString("username"))
//		.body(containsString("email"));
//	return response;
//}
	
	
	// ========================================================================
	//  DATASOURCE OPERATIONS
	// ------------------------------------------------------------------------
	
	public static ValidatableResponse authenticateDatasource(String username, String password, String profileName, String datasourceId) {
		ValidatableResponse response = 
			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("profileName", profileName)
				.formParam("keyRing", password)
			.when()
				.post("/datasources/" + username + "/" + datasourceId + "/auth")
			.then()
				.statusCode(200)
				.body(containsString("profileId"))
				.body(containsString("type"))
				.body(containsString("sourceProfile"));
		return response;
	}
	
	public static ValidatableResponse postAuthenticateDatasource(String username, String password, String profileId, Properties datasourceParameters) {
		RequestSpecBuilder reqSpecBuilder = new RequestSpecBuilder();
		reqSpecBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded");
		reqSpecBuilder.addHeader("Accept", "application/json");
		reqSpecBuilder.addParameter("keyRing", password);
		
		for(Entry<?,?> entry : datasourceParameters.entrySet()) {
			String key = (String) entry.getKey();  
			String value = (String) entry.getValue();  
			reqSpecBuilder.addParameter(key, value);
		}
		
		RequestSpecification requestSpec = reqSpecBuilder.build();
		ValidatableResponse response = 
		given()
			.spec(requestSpec)
		.when()
			.post("/datasources/" + username + "/" + profileId + "/auth/post")
		.then()
			.statusCode(200)
			.body("type", equalTo("success"))
			.body("messages", hasItem("Source profile has been authorized"));
		return response;
	}
	
	public static void deleteDatasourceProfile(String username, String profileId) {			
		when()
			.delete("/datasources/" + username + "/profiles/" + profileId)
		.then()
			.statusCode(200);
	}
	
	// ========================================================================
	//  DATASINK OPERATIONS
	// ------------------------------------------------------------------------
		
	public static ValidatableResponse authenticateDatasink(String username, String password, String profileName, String datasinkId) {
		ValidatableResponse response = 
		given()
			.contentType("application/x-www-form-urlencoded")
			.header("Accept", "application/json")
			.formParam("keyRing", password)
		.when()
			.post("/datasinks/" + username + "/" + datasinkId + "/auth")
		.then()
			.statusCode(200)
			.body(containsString("profileId"))
			.body(containsString("type"))
			.body(containsString("sourceProfile"))
			.body(containsString("redirectURL"));
		return response;
	}
		
	public static void deleteDatasinkProfile(String username, String profileId) {			
		when()
			.delete("/datasinks/" + username + "/profiles/" + profileId)
		.then()
			.statusCode(200);
	}	
	
	// ========================================================================
	//  PROFILE OPERATIONS
	// ------------------------------------------------------------------------
	
	public static void updateProfile(String profileId, String password, Properties profileProps) {
		
		RequestSpecBuilder reqSpecBuilder = new RequestSpecBuilder();
		reqSpecBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded");
		reqSpecBuilder.addHeader("Accept", "application/json");
		reqSpecBuilder.addParameter("keyRing", password);
		
		for(Entry<?,?> entry : profileProps.entrySet()) {
			String key = (String) entry.getKey();  
			String value = (String) entry.getValue();  
			reqSpecBuilder.addParameter(key, value);
		}
		
		RequestSpecification requestSpec = reqSpecBuilder.build();
			
		given()
			.spec(requestSpec)
		.when()
			.post("/profiles/" + profileId)
		.then()
			.assertThat().statusCode(204);
	}
	
	// ========================================================================
	//  BACKUPJOB OPERATIONS
	// ------------------------------------------------------------------------
		
	public static void deleteBackupJob(String username, int jobId) {
		when()
			.delete("/jobs/" + username + "/" + jobId)
		.then()
			.statusCode(200);
	}
	
	public static ValidatableResponse createBackupJob(String username, String password, String profileSourceId, String profileSinkId, String jobTimeExpression, String jobTitle) {
		ValidatableResponse response = 
		given()
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
			.statusCode(200)
			.body(containsString("jobId"));
		return response;
	}
}
