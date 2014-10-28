package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.backmeup.tests.integration.utils.Constants;
import org.junit.Ignore;
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
	public void testGetAllAuthDataForUser() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.dummy";
		
		final int noOfAuths = 5;
		String authDataName = "AuthData";
		Map<String, String> authProps = new HashMap<>();
		authProps.put("password", "s3cr3t");
		
		List<String> authDataIds = new ArrayList<>();
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			for (int i = 0; i < noOfAuths; i++) {
				response = BackMeUpUtils.addAuthData(accessToken, pluginId, authDataName + i, authProps);
				authDataIds.add(response.extract().path("id").toString());
			}
			
			response = 
			given()
				.log().all()
				.contentType("application/json")
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/plugins/" + pluginId + "/authdata")
			.then()
				.log().all()
				.statusCode(200);
		
		} finally {
			for(String authDataId : authDataIds) {
				BackMeUpUtils.deleteAuthData(accessToken, pluginId, authDataId);
			}
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
	
	@Test
	public void testAddProfileSourceDummy() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.dummy";
		String profileName = "DummyProfile";
		PluginType profileType = PluginType.Source;
		String profileId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			PluginProfileDTO pluginProfile = new PluginProfileDTO();
			pluginProfile.setTitle(profileName);
			pluginProfile.setPluginId(pluginId);
			pluginProfile.setProfileType(profileType);
						
			response = 
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
			
			profileId = response.extract().path("profileId").toString();
		
		} finally {
			BackMeUpUtils.deleteProfile(accessToken, pluginId, profileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testGetProfileSourceDummy() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.dummy";
		String profileName = "DummyProfile";
		PluginType profileType = PluginType.Source;
		String profileId = "";
				
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			PluginProfileDTO pluginProfile = new PluginProfileDTO();
			pluginProfile.setTitle(profileName);
			pluginProfile.setPluginId(pluginId);
			pluginProfile.setProfileType(profileType);
			
			response = BackMeUpUtils.addProfile(accessToken, pluginId, pluginProfile);
			profileId = response.extract().path("profileId").toString();
									
			response = 
			given()
				.log().all()
				.contentType("application/json")
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/plugins/" + pluginId + "/" + profileId)
			.then()
				.log().all()
				.statusCode(200)
				.body("profileId", equalTo(Integer.parseInt(profileId)))
				.body("title", equalTo(profileName))
				.body("pluginId", equalTo(pluginId))
				.body("profileType", equalTo(profileType.toString()));
		
		} finally {
			BackMeUpUtils.deleteProfile(accessToken, pluginId, profileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Ignore
	@Test
	public void testGetProfileSourceFilegenerator() {	
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.filegenerator";
		String profileName = "FilegeneratorProfile";
		PluginType profileType = PluginType.Source;
		String profileId = "";
		
		PluginProfileDTO pluginProfile = new PluginProfileDTO();
		pluginProfile.setTitle(profileName);
		pluginProfile.setPluginId(pluginId);
		pluginProfile.setProfileType(profileType);
		
		pluginProfile.addProperty("text", "true");
		pluginProfile.addProperty("image", "true");
		pluginProfile.addProperty("pdf", "true");
		pluginProfile.addProperty("binary", "true");
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			response = BackMeUpUtils.addProfile(accessToken, pluginId, pluginProfile);
			profileId = response.extract().path("profileId").toString();
			
			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/plugins/" + pluginId + "/" + profileId)
			.then()
				.log().all()
				.statusCode(200);
		} finally {
			BackMeUpUtils.deleteProfile(accessToken, pluginId, profileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testUpdateProfileSourceDummy() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.dummy";
		String profileName = "DummyProfile";
		PluginType profileType = PluginType.Source;
		String profileId = "";
		
		String newProfileName = "NewDummyProfile";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			PluginProfileDTO pluginProfile = new PluginProfileDTO();
			pluginProfile.setTitle(profileName);
			pluginProfile.setPluginId(pluginId);
			pluginProfile.setProfileType(profileType);
			
			response = BackMeUpUtils.addProfile(accessToken, pluginId, pluginProfile);
			profileId = response.extract().path("profileId").toString();
			
			pluginProfile = BackMeUpUtils.getProfile(accessToken, pluginId, profileId);			
			pluginProfile.setTitle(newProfileName);
						
			response = 
			given()
				.log().all()
				.contentType("application/json")
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
				.body(pluginProfile, ObjectMapperType.JACKSON_1)
			.when()
				.put("/plugins/" + pluginId + "/" + profileId)
			.then()
				.log().all()
				.statusCode(200)
				.body("profileId", equalTo(Integer.parseInt(profileId)))
				.body("title", equalTo(newProfileName))
				.body("pluginId", equalTo(pluginId))
				.body("profileType", equalTo(profileType.toString()));
		
		} finally {
			BackMeUpUtils.deleteProfile(accessToken, pluginId, profileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testDeleteProfile() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.dummy";
		String profileName = "DummyProfile";
		PluginType profileType = PluginType.Source;
		String profileId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			response = BackMeUpUtils.addProfile(accessToken, pluginId, profileName, profileType, null, null, null);
			profileId = response.extract().path("profileId").toString();
						
			given()
				.log().all()
				.contentType("application/json")
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.delete("/plugins/" + pluginId + "/" + profileId)
			.then()
				.log().all()
				.statusCode(204);
		
		} finally {
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Ignore
	@Test
	public void testAddProfileFileGeneratorSourceInputBased() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.filegenerator";
		String profileName = "FilegeneratorProfile";
		PluginType profileType = PluginType.Source;
		String profileId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			PluginProfileDTO pluginProfile = new PluginProfileDTO();
			pluginProfile.setTitle(profileName);
			pluginProfile.setPluginId(pluginId);
			pluginProfile.setProfileType(profileType);
			
			pluginProfile.addProperty("text", "true");
			pluginProfile.addProperty("image", "true");
			pluginProfile.addProperty("pdf", "true");
			pluginProfile.addProperty("binary", "true");
			
			response = 
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
			
			profileId = response.extract().path("profileId").toString();
		
		} finally {
			BackMeUpUtils.deleteProfile(accessToken, pluginId, profileId);
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Ignore
	@Test
	public void testAddPluginProfileSourceOAuth() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.dropbox";
		String profileName = "DropboxProfile";
		PluginType profileType = PluginType.Source;
		String profileId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			PluginProfileDTO pluginProfile = new PluginProfileDTO();
			pluginProfile.setTitle(profileName);
			pluginProfile.setProfileType(profileType);
			
			pluginProfile.addProperty(Constants.KEY_SOURCE_TOKEN, Constants.VALUE_SOURCE_TOKEN);
			pluginProfile.addProperty(Constants.KEY_SOURCE_SECRET, Constants.VALUE_SOURCE_SECRET);
			
			response = 
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
			
			profileId = response.extract().path("profileId");
		
		} finally {
			BackMeUpUtils.deleteProfile(accessToken, pluginId, profileId);
			BackMeUpUtils.deleteUser(accessToken, username);
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
