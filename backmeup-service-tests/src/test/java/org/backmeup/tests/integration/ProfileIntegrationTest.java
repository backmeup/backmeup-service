package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

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
public class ProfileIntegrationTest extends IntegrationTestBase {
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

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
			System.out.println(profileId);
						
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
}
