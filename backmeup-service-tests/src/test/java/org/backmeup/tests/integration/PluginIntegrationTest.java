package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.UserDTO;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.backmeup.tests.integration.utils.TestDataManager;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.response.ValidatableResponse;

@Category(IntegrationTest.class)
public class PluginIntegrationTest extends IntegrationTestBase {

    @Test
    public void testGetPluginFilegenerator() {	
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        String pluginId = "org.backmeup.filegenerator";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

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
    public void testGetPluginBackmeupStorage() {	
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        String pluginId = "org.backmeup.storage";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

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
                .body(containsString("authDataDescription"))
                .statusCode(200);
        } finally {
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testGetPluginDummy() {	
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        String pluginId = "org.backmeup.dummy";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

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
    public void testGetPluginEmailWithOptions() {  
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";
        
        String pluginId = "org.backmeup.mail";
        String authDataId = "";
        AuthDataDTO authDataEmail = TestDataManager.getAuthDataEmail();
        
        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);
            
            response = BackMeUpUtils.addAuthData(accessToken, pluginId, authDataEmail);
            authDataId = response.extract().path("id").toString();
            
            given()
                .log().all()
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/plugins/" + pluginId + "?authData=" + authDataId)
            .then()
                .log().all()
                .statusCode(200);
        } finally {
            BackMeUpUtils.deleteAuthData(accessToken, pluginId, authDataId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testGetAllPlugins() {	
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

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
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginType pluginType = PluginType.Source;

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

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
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginType pluginType = PluginType.Sink;

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

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
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginType pluginType = PluginType.Action;

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

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
