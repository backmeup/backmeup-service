package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;

import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.ValidatableResponse;


@Category(IntegrationTest.class)
public class ProfileIntegrationTest extends IntegrationTestBase {
	
	@Test
	public void testAddAuthData() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.dummy";
		
		String authDataName = "AuthData1";
		
		String authDataId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			AuthDataDTO authData = new AuthDataDTO();
			authData.setName(authDataName);
			authData.addProperty("password", "s3cr3t");
			
			response = 
			given()
				.log().all()
				.contentType("application/json")
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
				.body(authData, ObjectMapperType.JACKSON_1)
			.when()
				.post("/plugins/" + pluginId + "/authdata")
			.then()
				.log().all()
				.statusCode(200)
				.body("id", notNullValue())
				.body("name", equalTo(authDataName));
			
			authDataId = response.extract().path("id").toString();
		
		} finally {
			BackMeUpUtils.deleteAuthData(accessToken, pluginId, authDataId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testGetAuthData() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.dummy";
		
		String authDataName = "AuthData1";
		Map<String, String> authProps = new HashMap<>();
		authProps.put("password", "s3cr3t");
		
		String authDataId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			response = BackMeUpUtils.addAuthData(accessToken, pluginId, authDataName, authProps);
			authDataId = response.extract().path("id").toString();
			
			response = 
			given()
				.log().all()
				.contentType("application/json")
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/plugins/" + pluginId + "/authdata/" + authDataId)
			.then()
				.log().all()
				.statusCode(200)
				.body("id", equalTo(Integer.parseInt(authDataId)))
				.body("name", equalTo(authDataName));
		
		} finally {
			BackMeUpUtils.deleteAuthData(accessToken, pluginId, authDataId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testDeleteAuthData() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.dummy";
		
		String authDataName = "AuthData1";
		Map<String, String> authProps = new HashMap<>();
		authProps.put("password", "s3cr3t");
		
		String authDataId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			response = BackMeUpUtils.addAuthData(accessToken, pluginId, authDataName, authProps);
			authDataId = response.extract().path("id").toString();
			
			response = 
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
		
		} finally {
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
/*
	@Test
	public void testUpdateNonExistingProfile() {
		String profileId = "100";
		String password = "password1";
		String keyTest = "keyTest";
		String valueTest = "valueTest";
		
		given()
			.log().all()
			.contentType("application/x-www-form-urlencoded")
			.header("Accept", "application/json")
			.formParam("keyRing", password)
			.formParam(keyTest, valueTest)
		.when()
			.post("/profiles/" + profileId)
		.then()
			.assertThat().statusCode(400)
			.body("errorType", equalTo("java.lang.IllegalArgumentException"));
	}
	
	@Test
	public void testUpdateProfile() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String profileName = "Dropbox";
		
		String profileId = "";
		
		String keyTest = "keyTest";
		String valueTest = "valueTest";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileName, datasourceId);
			profileId = response.extract().path("profileId");
						
			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("keyRing", password)
				.formParam(keyTest, valueTest)
			.when()
				.post("/profiles/" + profileId)
			.then()
				.log().all()
				.assertThat().statusCode(204);
		} finally {
			BackMeUpUtils.deleteDatasourceProfile(username, profileId);
			BackMeUpUtils.deleteUser(username);
		}
	}	
*/
}
