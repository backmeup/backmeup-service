package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

import java.util.Properties;

import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.backmeup.tests.integration.utils.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.response.ValidatableResponse;

/*
 * for examples rest-assured see:
 * https://github.com/jayway/rest-assured/tree/master/examples/rest-assured-itest-java/src/test/java/com/jayway/restassured/itest/java
 */

@Category(IntegrationTest.class)
public class DatasourcesIntegrationTest extends IntegrationTestBase {
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetDatasource() {
		when()
			.get("/datasources/")
		.then()
//			.log().all()
			.assertThat().statusCode(200)
			.assertThat().body(containsString("sources"))
			.assertThat().body(containsString("title"))
			.assertThat().body(containsString("datasourceId"))
			.assertThat().body(containsString("imageURL"));	
	}
	
	@Test
	public void testGetEmptyDatasourceProfiles() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
						
			when()
				.get("/datasources/" + username + "/profiles")
			.then()
//				.log().all()
				.statusCode(200)
				.body(containsString("sourceProfiles"));
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testGetDatasourceProfiles() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String profileName = "Dropbox";
		String profileId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileName, datasourceId);
			profileId = response.extract().path("profileId");
						
			when()
				.get("/datasources/" + username + "/profiles")
			.then()
//				.log().all()
				.statusCode(200)
				.body(containsString("sourceProfiles"))
				.body(containsString("title"))
				.body(containsString("createDate"))
				.body(containsString("modifyDate"))
				.body(containsString("pluginName"))
				.body(containsString("datasourceProfileId"));
		} finally {
			BackMeUpUtils.deleteDatasourceProfile(username, profileId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testAuthenticateDatasource() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String profileName = "Dropbox";
		String profileId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = 
			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("profileName", profileName)
				.formParam("keyRing", password)
			.when()
				.post("/datasources/" + username + "/" + datasourceId + "/auth")
			.then()
//				.log().all()
				.statusCode(200)
				.body(containsString("profileId"))
				.body(containsString("type"))
				.body(containsString("sourceProfile"))
				.body(containsString("redirectURL"));
			
			profileId = response.extract().path("profileId");
		
		} finally {
			BackMeUpUtils.deleteDatasourceProfile(username, profileId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testDeleteDatasourceProfile() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String profileName = "Dropbox";
				
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileName, datasourceId);
			String profileId = response.extract().path("profileId");
			
			when()
				.delete("/datasources/" + username + "/profiles/" + profileId)
			.then()
				.statusCode(200);
		
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testGenerateDatasourceOptions() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.dropbox";
		String profileName = "Dropbox";
		String profileId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileName, datasourceId);
			profileId = response.extract().path("profileId");
			
			Properties profileProps = new Properties();
			profileProps.put(Constants.KEY_SOURCE_TOKEN, Constants.VALUE_SOURCE_TOKEN);
			profileProps.put(Constants.KEY_SOURCE_SECRET, Constants.VALUE_SOURCE_SECRET);
			
			BackMeUpUtils.updateProfile(profileId, password, profileProps);
						
			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("keyRing", password)
			.when()
				.post("/datasources/" + username + "/profiles/" + profileId + "/options")
			.then()
//				.log().all()
				.statusCode(200)
				.body(containsString("sourceOptions"));
		} finally {
			BackMeUpUtils.deleteDatasourceProfile(username, profileId);
			BackMeUpUtils.deleteUser(username);
		}
	}
}
