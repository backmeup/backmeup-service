package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.response.ValidatableResponse;

@Category(IntegrationTest.class)
public class PluginIntegrationTest extends IntegrationTestBase {

	@Test
	public void testGetPluginFilegenerator() {	
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
				.body(containsString("propertiesDescription"))
				.statusCode(200);
		} finally {
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testGetPluginDummy() {	
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		String pluginId = "org.backmeup.dummy";
		
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
				.body(containsString("metadata"))
				.body(containsString("authDataDescription"))
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
	
	@Test
	public void testGetAllDatasourcePlugins() {	
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		PluginType pluginType = PluginType.Source;
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/plugins/?types=" + pluginType)
			.then()
				.log().all()
				.statusCode(200);
		} finally {
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testGetAllDatasinkPlugins() {	
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		PluginType pluginType = PluginType.Sink;
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/plugins/?types=" + pluginType)
			.then()
				.log().all()
				.statusCode(200);
		} finally {
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
	
	@Test
	public void testGetAllActionPlugins() {	
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "john.doe@example.com";
		
		String userId = "";
		String accessToken = "";
		
		PluginType pluginType = PluginType.Action;
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			accessToken = userId + ";" + password;
			
			given()
				.log().all()
				.header("Accept", "application/json")
				.header("Authorization", accessToken)
			.when()
				.get("/plugins/?types=" + pluginType)
			.then()
				.log().all()
				.statusCode(200);
		} finally {
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
}
