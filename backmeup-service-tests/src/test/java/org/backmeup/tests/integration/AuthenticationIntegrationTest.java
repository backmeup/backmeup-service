package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.backmeup.model.dto.UserDTO;
import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.backmeup.tests.integration.utils.TestDataManager;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.response.ValidatableResponse;

@Category(IntegrationTest.class)
public class AuthenticationIntegrationTest extends IntegrationTestBase {

    @Test
    public void testAuthenticateUser() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();

            response = 
            given()
                .log().all()
                .header("Accept", "application/json")
            .when()
                .get("/authenticate?username=" + user.getUsername() + "&password=" + user.getPassword())
            .then()
                .log().all()
                .statusCode(200)
                .body(containsString("accessToken"))
                .body(containsString("expiresAt"));

            accessToken = response.extract().path("accessToken");
        } finally {
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }
}
