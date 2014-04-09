package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
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
public class DatasinksIntegrationTest extends IntegrationTestBase {
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetDatasinks() {
		when()
			.get("/datasinks/")
		.then()
//			.log().all()
			.assertThat().statusCode(200)
			.assertThat().body(containsString("sinks"))
			.assertThat().body(containsString("title"))
			.assertThat().body(containsString("datasinkId"))
			.assertThat().body(containsString("imageURL"))
			.assertThat().body(containsString("description"));
	}
	
	@Test
	public void testGetEmptyDatasinkProfiles() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
						
			when()
				.get("/datasinks/" + username + "/profiles")
			.then()
//				.log().all()
				.statusCode(200)
				.body(containsString("sinkProfiles"));
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
		
		String datasinkId = "org.backmeup.dropbox";
		String profileName = "Dropbox";
		String profileId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasink(username, password, profileName, datasinkId);
			profileId = response.extract().path("profileId");
						
			when()
				.get("/datasinks/" + username + "/profiles")
			.then()
//				.log().all()
				.statusCode(200)
				.body(containsString("sinkProfiles"))
				.body(containsString("title"))
				.body(containsString("createDate"))
				.body(containsString("modifyDate"))
				.body(containsString("pluginName"))
				.body(containsString("datasinkProfileId"));
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
		
		String datasinkId = "org.backmeup.dropbox";
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
				.post("/datasinks/" + username + "/" + datasinkId + "/auth")
			.then()
//				.log().all()
				.statusCode(200)
				.body(containsString("profileId"))
				.body(containsString("type"))
				.body(containsString("sourceProfile"))
				.body(containsString("redirectURL"));
			
			profileId = response.extract().path("profileId");
		
		} finally {
			BackMeUpUtils.deleteDatasinkProfile(username, profileId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testDeleteDatasinkProfile() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasinkId = "org.backmeup.dropbox";
		String profileName = "Dropbox";
				
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasink(username, password, profileName, datasinkId);
			String profileId = response.extract().path("profileId");
			
			when()
				.delete("/datasinks/" + username + "/profiles/" + profileId)
			.then()
				.statusCode(200);
		
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
	/*
	@Test
	public void testGenerateDatasinkOptions() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasinkId = "org.backmeup.dropbox";
		String profileName = "Dropbox";
		String profileId = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasink(username, password, profileName, datasinkId);
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
				.log().all()
				.statusCode(200)
				.body(containsString("sourceOptions"));
		} finally {
			BackMeUpUtils.deleteDatasourceProfile(username, profileId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	*/
}
