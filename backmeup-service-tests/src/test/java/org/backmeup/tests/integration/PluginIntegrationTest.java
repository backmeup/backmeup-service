package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.backmeup.model.AuthData;
import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.backmeup.tests.integration.utils.Constants;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.internal.matchers.NotNull;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.ValidatableResponse;

@Category(IntegrationTest.class)
public class PluginIntegrationTest extends IntegrationTestBase {

	@Test
	public void testGetPlugin() {	
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.filegenerator";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/plugins/" + pluginId)
			.then()
				.log().all()
				.body("pluginId", equalTo(pluginId))
				.body(containsString("title"))
				.body(containsString("description"))
				.body(containsString("imageURL"))
				.body(containsString("pluginType"))
				.statusCode(200);
		} finally {
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Ignore
	@Test
	public void testGetPluginExpandProfiles() {	
		String pluginId = "backmeupPlugin1";
		try {
			given()
				.log().all()
				.header("Accept", "application/json")
			.when()
				.get("/plugins/" + pluginId + "/?expandProfiles=true")
			.then()
				.log().all()
				.statusCode(200);
		} finally {
		}
	}
	
	@Test
	public void testGetAllPlugins() {	
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
				.get("/plugins/")
			.then()
				.log().all()
				.statusCode(200);
		} finally {
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Ignore
	@Test
	public void testGetAllDatasourcePlugins() {	
		String pluginTypes = "source";
		try {
			given()
				.log().all()
				.header("Accept", "application/json")
			.when()
				.get("/plugins/?types=" + pluginTypes)
			.then()
				.log().all()
				.statusCode(200);
		} finally {
		}
	}
	
	@Ignore
	@Test
	public void testGetAllDatasinkPlugins() {	
		String pluginTypes = "sink";
		try {
			given()
				.log().all()
				.header("Accept", "application/json")
			.when()
				.get("/plugins/?types=" + pluginTypes)
			.then()
				.log().all()
				.statusCode(200);
		} finally {
		}
	}
	
	@Ignore
	@Test
	public void testGetAllActionPlugins() {	
		String pluginTypes = "action";
		try {
			given()
				.log().all()
				.header("Accept", "application/json")
			.when()
				.get("/plugins/?types=" + pluginTypes)
			.then()
				.log().all()
				.statusCode(200);
		} finally {
		}
	}
	
	@Test
	public void testGetPluginProfile() {	
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
	
	@Ignore
	@Test
	public void testGetPluginProfileExpandConfig() {	
		String pluginId = "backmeupPlugin1";
		String profileId = "1";
		try {
			given()
				.log().all()
				.header("Accept", "application/json")
			.when()
				.get("/plugins/" + pluginId + "/" + profileId + "/?expandConfig=true")
			.then()
				.log().all()
				.statusCode(200);
		} finally {
		}
	}
}
