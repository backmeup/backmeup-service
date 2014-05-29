package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.response.ValidatableResponse;

/*
 * for examples rest-assured see:
 * https://github.com/jayway/rest-assured/tree/master/examples/rest-assured-itest-java/src/test/java/com/jayway/restassured/itest/java
 */

@Category(IntegrationTest.class)
public class UserIntegrationTest extends IntegrationTestBase {

	@Test
	public void testRegisterUser() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		try {
			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("password", password)
				.formParam("keyRing", keyRingPassword)
				.formParam("email", email)
			.when()
				.post("/users/" + username + "/register")
			.then()
				.assertThat().statusCode(200).assertThat()
				.body(containsString("verificationKey"));
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testAlreadyRegisteredUser() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		try {
			BackMeUpUtils.addUser(username, password, keyRingPassword, email);

			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("password", password)
				.formParam("keyRing", keyRingPassword)
				.formParam("email", email)
			.when()
				.post("/users/" + username + "/register")
			.then()
				.statusCode(400);
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
		
	}
	
	@Test
	public void testRegisterAlreadyExistingUsername() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		try {
			BackMeUpUtils.addUser(username, password, keyRingPassword, email);

			String newPassword = "password2";

			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("password", newPassword)
				.formParam("keyRing", keyRingPassword)
				.formParam("email", email)
			.when()
				.post("/users/" + username + "/register")
			.then()
				.statusCode(400);
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testDeleteUser() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		BackMeUpUtils.addUser(username, password, keyRingPassword, email);
		
		when()
			.delete("/users/" + username)
		.then()
			.statusCode(200)
			.body("type", equalTo("success"))
			.body("messages", hasItem("User has been deleted"));
	}
		
	@Test
	public void testAlreadyDeletedUser(){
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		BackMeUpUtils.addUser(username, password, keyRingPassword, email);
		BackMeUpUtils.deleteUser(username);
		
		when()
			.delete("/users/" + username)
		.then()
			.statusCode(404);
	}
	
	@Test
	public void testVerifyEmailKeyNotExists(){
		String verificationKey = "abc123";
		when()
			.get("/users/" + verificationKey + "/verifyEmail")
		.then()
			.statusCode(400);
	}
	
	@Test
	public void testVerifyEmail(){
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");

			when()
				.get("/users/" + verificationKey + "/verifyEmail")
			.then()
				.statusCode(200)
				.body("username", equalTo(username));
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
		
	@Test
	public void testChangeUserUsername(){
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String newUsername = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			newUsername = "TestUser2";
			String newPassword = password;
			String newKeyRingPassword = keyRingPassword;
			String newEmail = email;

			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("username", newUsername)
				.formParam("oldPassword", password)
				.formParam("password", newPassword)
				.formParam("oldKeyring", keyRingPassword)
				.formParam("newKeyring", newKeyRingPassword)
				.formParam("email", newEmail)
			.when()
				.put("/users/" + username)
			.then()
				.statusCode(200)
				.body("type", equalTo("success"))
				.body("messages", hasItem("Account username has been changed"));
		} finally {
			BackMeUpUtils.deleteUser(newUsername);
		}
	}
	
	@Test
	public void testChangeUserPassword(){
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String newUsername = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			newUsername = username;
			String newPassword = "pw!NW12?aB";
			String newKeyRingPassword = keyRingPassword;
			String newEmail = email;

			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("username", newUsername)
				.formParam("oldPassword", password)
				.formParam("password", newPassword)
				.formParam("oldKeyring", keyRingPassword)
				.formParam("newKeyring", newKeyRingPassword)
				.formParam("email", newEmail)
			.when()
				.put("/users/" + username)
			.then()
				.statusCode(200)
				.body("type", equalTo("success"))
				.body("messages", hasItem("Account password has been changed"));
		} finally {
			BackMeUpUtils.deleteUser(newUsername);
		}
	}
	
	@Test
	public void testChangeUserEmail() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String newUsername = "";
		
		try {	
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			newUsername = username;
			String newPassword = password;
			String newKeyRingPassword = keyRingPassword;
			String newEmail = "ChangedTestUser@trash-mail.com";

			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("username", newUsername)
				.formParam("oldPassword", password)
				.formParam("password", newPassword)
				.formParam("oldKeyring", keyRingPassword)
				.formParam("newKeyring", newKeyRingPassword)
				.formParam("email", newEmail)
			.when()
				.put("/users/" + username)
			.then()
				.statusCode(200)
				.body("type", equalTo("success"))
				.body("messages", hasItem("Account email has been changed"));

		} finally {
			BackMeUpUtils.deleteUser(newUsername);
		}
	}
	
	@Test
	public void testChangeUserInvalidEmail() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String newUsername = "";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			newUsername = username;
			String newPassword = password;
			String newKeyRingPassword = keyRingPassword;
			String newEmail = "ChangedTestUsertrash-mail.com";

			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("username", newUsername)
				.formParam("oldPassword", password)
				.formParam("password", newPassword)
				.formParam("oldKeyring", keyRingPassword)
				.formParam("newKeyring", newKeyRingPassword)
				.formParam("email", newEmail)
			.when()
				.put("/users/" + username)
			.then()
				.statusCode(400);
		} finally {
			BackMeUpUtils.deleteUser(newUsername);
		}
	}
	
	@Test
	public void testChangeUserToExistingUsername(){
		String u1Username = "TestUser1";
		String u1Password = "password1";
		String u1KeyRingPassword = "keyringpassword1";
		String u1Email = "TestUser@trash-mail.com";
		
		String u2Username = "TestUser2";
		String u2Password = "password2";
		String u2KeyRingPassword = "keyringpassword2";
		String u2Email = "TestUser2@trash-mail.com";
		
		try {
			ValidatableResponse u1Response = BackMeUpUtils.addUser(u1Username, u1Password, u1KeyRingPassword, u1Email);
			String verificationKey = u1Response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			ValidatableResponse u2Response = BackMeUpUtils.addUser(u2Username, u2Password, u2KeyRingPassword, u2Email);
			verificationKey = u2Response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			String newUsername = "TestUser1";
			String newPassword = u2Password;
			String newKeyRingPassword = u2KeyRingPassword;
			String newEmail = u2Email;

			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("username", newUsername)
				.formParam("oldPassword", u2Password)
				.formParam("password", newPassword)
				.formParam("oldKeyring", u2KeyRingPassword)
				.formParam("newKeyring", newKeyRingPassword)
				.formParam("email", newEmail)
			.when()
				.put("/users/" + u2Username)
			.then()
				.statusCode(400)
				.body("errorType", equalTo("org.backmeup.model.exceptions.AlreadyRegisteredException"));
		} finally {
			BackMeUpUtils.deleteUser(u1Username);
			BackMeUpUtils.deleteUser(u2Username);
		}
	}
	
	@Test
	public void testChangeUserKeyring() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		String newUsername = "";
		
		try {
		ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
		String verificationKey = response.extract().path("verificationKey");
		BackMeUpUtils.verifyEmail(verificationKey);
		
		newUsername = username;
		String newPassword = password;
		String newKeyRingPassword = "nwPW##!123";
		String newEmail = email;
		
		given()
			.contentType("application/x-www-form-urlencoded")
			.header("Accept", "application/json")
			.formParam("username", newUsername)
			.formParam("oldPassword", password)
			.formParam("password", newPassword)
			.formParam("oldKeyring", keyRingPassword)
			.formParam("newKeyring", newKeyRingPassword)
			.formParam("email", newEmail)
		.when()
			.put("/users/" + username)
		.then()
			.statusCode(200);
		} finally {
			BackMeUpUtils.deleteUser(newUsername);
		}
	}
	
	@Test
	public void testGetUser() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			when()
				.get("/users/" + username)
			.then()
				.statusCode(200)
				.body("username", equalTo(username))
				.body("email", equalTo(email));
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testGetUnknownUser() {
		String username = "TestUser1";
		
		when()
			.get("/users/" + username)
		.then()
			.statusCode(404)
			.body("errorType", equalTo("org.backmeup.model.exceptions.UnknownUserException"));
	}
	
	@Test
	public void testGetChangedUser() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			when()
				.get("/users/" + username)
			.then()
				.statusCode(200)
				.body("username", equalTo(username))
				.body("email", equalTo(email));

			BackMeUpUtils.changeUser(username, username, password, "password2", 
					keyRingPassword, keyRingPassword, email, email);

			when()
				.get("/users/" + username)
			.then()
				.statusCode(200)
				.body("username", equalTo(username))
				.body("email", equalTo(email));
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testLoginUserSuccessful() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("password", password)
			.when()
				.post("/users/" + username + "/login")
			.then()
				.statusCode(200);
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
		
	@Test
	public void testLoginUserDenied() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			String wrongPassword = "pw2";

			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.formParam("password", wrongPassword)
			.when()
				.post("/users/" + username + "/login")
			.then()
				.statusCode(401);
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testSetUserProperty() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		String propertyKey = "TestPropKey";
		String propertyValue = "TestPropValue";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			given()
				.contentType("application/x-www-form-urlencoded")
				.header("Accept", "application/json")
			.when()
				.post("/users/" + username + "/properties/" + propertyKey + "/" + propertyValue)
			.then()
				.statusCode(200)
				.body("type", equalTo("success"))
				.body("messages", hasItem("User property has been set"));
		} finally {
			BackMeUpUtils.deleteUser(username);
		}	
	}
	
	@Test
	public void testGetUserPropertyNotFound() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		String propertyKey = "unknown";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			when()
				.get("/users/" + username + "/properties/" + propertyKey)
			.then()
				.statusCode(404)
				.body("errorType", equalTo("org.backmeup.model.exceptions.UnknownUserPropertyException"));
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testGetUserProperty() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		String propertyKey = "TestPropKey";
		String propertyValue = "TestPropValue";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			BackMeUpUtils.setUserProperty(username, propertyKey, propertyValue);

			when()
				.get("/users/" + username + "/properties/" + propertyKey)
			.then()
				.statusCode(200)
				.body("name", equalTo(propertyKey))
				.body("value", equalTo(propertyValue));
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
	
	@Test
	public void testDeleteUserProperty() {
		String username = "TestUser1";
		String password = "password1";
		String keyRingPassword = "keyringpassword1";
		String email = "TestUser@trash-mail.com";
		String propertyKey = "TestPropKey";
		String propertyValue = "TestPropValue";
		
		try {
			ValidatableResponse response = BackMeUpUtils.addUser(username, password, keyRingPassword, email);
			String verificationKey = response.extract().path("verificationKey");
			BackMeUpUtils.verifyEmail(verificationKey);

			BackMeUpUtils.setUserProperty(username, propertyKey, propertyValue);

			when()
				.delete("/users/" + username + "/properties/" + propertyKey)
			.then()
				.statusCode(200)
				.body("type", equalTo("success"))
				.body("messages", hasItem("User property has been removed"));
		} finally {
			BackMeUpUtils.deleteUser(username);
		}
	}
}
