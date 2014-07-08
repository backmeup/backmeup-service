package org.backmeup.tests.integration;

import org.backmeup.tests.IntegrationTest;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class DatasourcesIntegrationTest extends IntegrationTestBase {
/*
	@Test
	public void testGetDatasource() {
		when()
			.get("/datasources/")
		.then()
			.log().all()
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
				.log().all()
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
				.log().all()
				.statusCode(200)
				.body(containsString("sourceProfiles"))
				.body(containsString("title"))
				.body(containsString("createDate"))
				.body(containsString("modifyDate"))
				.body(containsString("pluginName"))
				.body(containsString("profileId"));
		} finally {
			BackMeUpUtils.deleteDatasourceProfile(username, profileId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testAuthenticateDatasourceOAuthBased() {
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
				.log().all()
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
	public void testAuthenticateDatasourceInputBased() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.filegenerator";
		String profileName = "FileGeneratorSourceProfile";
		String profileSourceId = "";

		
		ValidatableResponse response = null;
		try {
			response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
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
				.log().all()
				.statusCode(200)
				.body(containsString("profileId"))
				.body(containsString("type"))
				.body(containsString("sourceProfile"))
				.body(containsString("requiredInputs"));
			
		} finally {
			profileSourceId = response.extract().path("profileId");
			BackMeUpUtils.deleteDatasourceProfile(username, profileSourceId);
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testPostAuthenticateDatasourceInputBased() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String datasourceId = "org.backmeup.filegenerator";
		String profileSourceName = "FileGeneratorSourceProfile";
		String profileSourceId = "";

		
		ValidatableResponse response;
		try {
			response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);
			
			response = BackMeUpUtils.authenticateDatasource(username, password, profileSourceName, datasourceId);
			profileSourceId = response.extract().path("profileId");
			
			Properties dsProperties = new Properties();
			dsProperties.put("text", "true");
			dsProperties.put("image", "true");
			dsProperties.put("pdf", "true");
			dsProperties.put("binary", "true");
			
			RequestSpecBuilder reqSpecBuilder = new RequestSpecBuilder();
			reqSpecBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded");
			reqSpecBuilder.addHeader("Accept", "application/json");
			reqSpecBuilder.addParameter("keyRing", password);
			
			for(Entry<?,?> entry : dsProperties.entrySet()) {
				String key = (String) entry.getKey();  
				String value = (String) entry.getValue();  
				reqSpecBuilder.addParameter(key, value);
			}
			
			RequestSpecification requestSpec = reqSpecBuilder.build();
			response = 
			given()
				.spec(requestSpec)
			.when()
				.post("/datasources/" + username + "/" + profileSourceId + "/auth/post")
			.then()
				.statusCode(200)
				.body("type", equalTo("success"))
				.body("messages", hasItem("Source profile has been authorized"));
				
		} finally {
			BackMeUpUtils.deleteDatasourceProfile(username, profileSourceId);
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
