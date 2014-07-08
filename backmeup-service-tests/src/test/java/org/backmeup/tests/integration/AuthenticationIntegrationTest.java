package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.response.ValidatableResponse;

@Category(IntegrationTest.class)
public class AuthenticationIntegrationTest extends IntegrationTestBase {

	@Test
	public void testAuthenticateUser() {
		String username = "john.doe";
		String firstname = "John";
		String lastname = "Doe";
		String password = "password1";
		String email = "TestUser@trash-mail.com";
		
		String userId = "";
		String accessToken = "";
		
				
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, firstname, lastname, password, email);
			userId = response.extract().path("userId").toString();
			
			String expectedAccessToken = userId + ";" + password;
			
			response = 
			given()
				.log().all()
				.header("Accept", "application/json")
			.when()
				.get("/authenticate?username=" + username + "&password=" + password)
			.then()
				.log().all()
				.statusCode(200)
				.body("accessToken", equalTo(expectedAccessToken))
				.body(containsString("issueDate"));
			
			accessToken = response.extract().path("accessToken");
		} finally {
			BackMeUpUtils.deleteUser(accessToken, userId);
		}
	}
}
