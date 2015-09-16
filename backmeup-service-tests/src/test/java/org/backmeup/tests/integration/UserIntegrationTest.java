package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.backmeup.model.dto.UserDTO;
import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.backmeup.tests.integration.utils.TestDataManager;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.ValidatableResponse;

@Category(IntegrationTest.class)
public class UserIntegrationTest extends IntegrationTestBase {

    @Test
    public void testAddUser() {
        UserDTO newUser = TestDataManager.getUser();
        String accessToken;

        ValidatableResponse response = null;
        try {
            response = 
            given()
                .log().all()
                .header("Accept", "application/json")
                .body(newUser, ObjectMapperType.JACKSON_1)
            .when()
                .post("/users/")
            .then()
                .log().all()
                .statusCode(200)
                .body("username", equalTo(newUser.getUsername()))
                .body("firstname", equalTo(newUser.getFirstname()))
                .body("lastname", equalTo(newUser.getLastname()))
                .body("password", equalTo(null))
                .body("email", equalTo(newUser.getEmail()))
                .body("activated", equalTo(true))
                .body(containsString("userId"));
        } finally {
            String userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(newUser);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testAddUserAlreadyRegistered() {
        UserDTO user = TestDataManager.getUser();
        String accessToken;
        String userId = "";

        try {			
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();

            UserDTO newUser = TestDataManager.getUser(); 

            given()
                .log().all()
                .header("Accept", "application/json")
                .body(newUser, ObjectMapperType.JACKSON_1)
            .when()
                .post("/users/")
            .then()
                .log().all()
                .statusCode(500);
        } finally {
            accessToken = BackMeUpUtils.authenticateUser(user);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testDeleteUser() {
        UserDTO user = TestDataManager.getUser();

        ValidatableResponse response = BackMeUpUtils.addUser(user);
        String userId = response.extract().path("userId").toString();
        String accessToken = BackMeUpUtils.authenticateUser(user);

        given()
            .log().all()
            .header("Authorization", accessToken)
        .when()
            .delete("/users/" + userId)
        .then()
            .log().all()
            .statusCode(204);
    }

    @Test
    public void testGetUser() {
        UserDTO user = TestDataManager.getUser();

        String userId = "";
        String accessToken = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            given()
                .log().all()
                .header("Authorization", accessToken)
            .when()
                .get("/users/" + userId)
            .then()
                .log().all()
                .statusCode(200)
                .body("username", equalTo(user.getUsername()))
                .body("firstname", equalTo(user.getFirstname()))
                .body("lastname", equalTo(user.getLastname()))
                .body("email", equalTo(user.getEmail()))
                .body("activated", equalTo(true))
                .body(containsString("userId"));
        } finally {
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testGetUserUnauthorized() {
        String userId = "1000";

        given()
            .log().all()
        .when()
            .get("/users/" + userId)
        .then()
            .log().all()
            .statusCode(401);
    }

    @Test
    public void testGetUserForbidden() {
        UserDTO user = TestDataManager.getUser();

        String userId = "";
        String forbiddenUserId = "4711";
        String accessToken = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            given()
                .log().all()
                .header("Authorization", accessToken)
            .when()
                .get("/users/" + forbiddenUserId)
            .then()
                .log().all()
                .statusCode(403);
        } finally {
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testUpdateUser() {
        String userId = "";
        String accessToken = "";

        try {
            UserDTO u = TestDataManager.getUser();

            ValidatableResponse response = BackMeUpUtils.addUser(u);
            UserDTO user = response.extract().as(UserDTO.class);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(u);

            String newFirstname = "Bob";
            String newLastname = "Jones";
            String newEmail = "bob.jones@example.com";

            user.setFirstname(newFirstname);
            user.setLastname(newLastname);
            user.setEmail(newEmail);

            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .body(user, ObjectMapperType.JACKSON_1)
            .when()
                .put("/users/" + userId)
            .then()
                .log().all()
                .statusCode(200)
                .body("username", equalTo(user.getUsername()))
                .body("firstname", equalTo(user.getFirstname()))
                .body("lastname", equalTo(user.getLastname()))
                .body("email", equalTo(user.getEmail()))
                .body("activated", equalTo(true))
                .body(containsString("userId"));
        } finally {
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }
    
    @Test
    public void testAddAnonymousUser() {
         UserDTO user = TestDataManager.getUser();

         String userId = "";
         String accessToken = "";

         try {
             ValidatableResponse response = BackMeUpUtils.addUser(user);
             userId = response.extract().path("userId").toString();
             accessToken = BackMeUpUtils.authenticateUser(user);

             given()
                 .log().all()
                 .header("Authorization", accessToken)
             .when()
                 .post("/users/anonymous")
             .then()
                 .log().all()
                 .statusCode(200)
                 .body("activated", equalTo(true))
                 .body("anonymous", equalTo(true))
                 .body(containsString("userId"))
                 .body(containsString("username"))
                 .body(containsString("email"));
         } finally {
             BackMeUpUtils.deleteUser(accessToken, userId);
         }
    }
    
    @Test
    public void testGetActivationCodeForAnonymousUser() {
    	 UserDTO user = TestDataManager.getUser();

         String userId = "";
         String accessToken = "";

         try {
             ValidatableResponse response = BackMeUpUtils.addUser(user);
             userId = response.extract().path("userId").toString();
             accessToken = BackMeUpUtils.authenticateUser(user);

             response = BackMeUpUtils.addAnonymousUser(accessToken);
             String anonymousUserId = response.extract().path("userId").toString();
             
             given()
                .log().all()
                .header("Authorization", accessToken)
             .when()
                .get("/users/" + anonymousUserId + "/activationCode")
             .then()
                .log().all()
                .statusCode(200)
                .body(containsString("activationCode"));
             
         } finally {
             BackMeUpUtils.deleteUser(accessToken, userId);
         }
    }
    
    @Test
    public void testGetActivationCodeForAnonymousUserAsPdf() {
         UserDTO user = TestDataManager.getUser();

         String userId = "";
         String accessToken = "";

         try {
             ValidatableResponse response = BackMeUpUtils.addUser(user);
             userId = response.extract().path("userId").toString();
             accessToken = BackMeUpUtils.authenticateUser(user);

             response = BackMeUpUtils.addAnonymousUser(accessToken);
             String anonymousUserId = response.extract().path("userId").toString();
             
             given()
                .log().all()
                .header("Authorization", accessToken)
                .header("accept", "application/pdf")
                .header("Content-Type", "application/pdf") 
             .when()
                .get("/users/" + anonymousUserId + "/activationCode")
             .then()
                .log().all()
                .statusCode(200);
             
         } finally {
             BackMeUpUtils.deleteUser(accessToken, userId);
         }
    }
}
