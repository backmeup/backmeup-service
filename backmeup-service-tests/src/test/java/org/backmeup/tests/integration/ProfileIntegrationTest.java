package org.backmeup.tests.integration;

import org.backmeup.tests.IntegrationTest;
import org.junit.experimental.categories.Category;


@Category(IntegrationTest.class)
public class ProfileIntegrationTest extends IntegrationTestBase {
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
