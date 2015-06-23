package org.backmeup.tests.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.List;

import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.UserDTO;
import org.backmeup.tests.IntegrationTest;
import org.backmeup.tests.integration.utils.BackMeUpUtils;
import org.backmeup.tests.integration.utils.TestDataManager;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.ValidatableResponse;


@Category(IntegrationTest.class)
public class ProfileIntegrationTest extends IntegrationTestBase {

    @Test
    public void testAddAuthData() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        String pluginId = "org.backmeup.dummy";
        String authDataId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            AuthDataDTO authData = TestDataManager.getAuthDataDummy();

            response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .body(authData, ObjectMapperType.JACKSON_1)
            .when()
                 .post("/plugins/" + pluginId + "/authdata")
            .then()
                 .log().all()
                 .statusCode(200)
                 .body("id", notNullValue())
                 .body("name", equalTo(authData.getName()));

            authDataId = response.extract().path("id").toString();

        } finally {
            BackMeUpUtils.deleteAuthData(accessToken, pluginId, authDataId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testGetAuthData() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        String pluginId = "org.backmeup.dummy";
        String authDataId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            AuthDataDTO authData = TestDataManager.getAuthDataDummy();

            response = BackMeUpUtils.addAuthData(accessToken, pluginId, authData);
            authDataId = response.extract().path("id").toString();

            response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/plugins/" + pluginId + "/authdata/" + authDataId)
            .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(Integer.parseInt(authDataId)))
                .body("name", equalTo(authData.getName()));

        } finally {
            BackMeUpUtils.deleteAuthData(accessToken, pluginId, authDataId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testGetAllAuthDataForUser() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        String pluginId = "org.backmeup.dummy";
        final int noOfAuths = 5;		
        List<String> authDataIds = new ArrayList<>();

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            for (int i = 0; i < noOfAuths; i++) {
                AuthDataDTO authData = TestDataManager.getAuthDataDummy();
                authData.setName(authData.getName() + i);
                response = BackMeUpUtils.addAuthData(accessToken, pluginId, authData);
                authDataIds.add(response.extract().path("id").toString());
            }

            response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/plugins/" + pluginId + "/authdata")
            .then()
                .log().all()
                .statusCode(200);

        } finally {
            for(String authDataId : authDataIds) {
                BackMeUpUtils.deleteAuthData(accessToken, pluginId, authDataId);
            }
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testDeleteAuthData() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        String pluginId = "org.backmeup.dummy";		
        String authDataId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            AuthDataDTO authData = TestDataManager.getAuthDataDummy();
            response = BackMeUpUtils.addAuthData(accessToken, pluginId, authData);
            authDataId = response.extract().path("id").toString();

            response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .delete("/plugins/" + pluginId + "/authdata/" + authDataId)
            .then()
                .log().all()
                .statusCode(204);

        } finally {
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testAddProfileSourceDummy() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO pluginProfile = TestDataManager.getProfileDummySource();
        String profileId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .body(pluginProfile, ObjectMapperType.JACKSON_1)
            .when()
                .post("/plugins/" + pluginProfile.getPluginId())
            .then()
                .log().all()
                .statusCode(200);

            profileId = response.extract().path("profileId").toString();

        } finally {
            BackMeUpUtils.deleteProfile(accessToken, pluginProfile.getPluginId(), profileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testAddProfileSourceFilegenerator() {	
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO pluginProfile = TestDataManager.getProfileFilegenerator();
        String profileId = "";	

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .body(pluginProfile, ObjectMapperType.JACKSON_1)
            .when()
                .post("/plugins/" + pluginProfile.getPluginId())
            .then()
                .log().all()
                .statusCode(200);

            profileId = response.extract().path("profileId").toString();
        } finally {
            BackMeUpUtils.deleteProfile(accessToken, pluginProfile.getPluginId(), profileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }


    @Test
    public void testAddProfileSinkBackmeupStorage() {	
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO pluginProfile = TestDataManager.getProfileBackmeupStorageSink();
        String authDataId = "";
        String profileId = "";	

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addAuthData(accessToken, pluginProfile.getPluginId(), pluginProfile.getAuthData());
            authDataId = response.extract().path("id").toString();
            pluginProfile.getAuthData().setId(Long.parseLong(authDataId));

            response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .body(pluginProfile, ObjectMapperType.JACKSON_1)
            .when()
                .post("/plugins/" + pluginProfile.getPluginId())
            .then()
                .log().all()
                .statusCode(200);

            profileId = response.extract().path("profileId").toString();
        } finally {
            BackMeUpUtils.deleteProfile(accessToken, pluginProfile.getPluginId(), profileId);
            BackMeUpUtils.deleteAuthData(accessToken, pluginProfile.getPluginId(), authDataId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Ignore
    @Test
    public void testAddProfileSourceEmail() {	
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        String profileId = "";
        String authDataId = "";
        PluginProfileDTO pluginProfile = TestDataManager.getProfileEmail();

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addAuthData(accessToken, pluginProfile.getPluginId(), pluginProfile.getAuthData());
            authDataId = response.extract().path("id").toString();
            pluginProfile.getAuthData().setId(Long.parseLong(authDataId));

            response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .body(pluginProfile, ObjectMapperType.JACKSON_1)
            .when()
                .post("/plugins/" + pluginProfile.getPluginId())
            .then()
                .log().all()
                .statusCode(200);

            profileId = response.extract().path("profileId").toString();
        } finally {
            BackMeUpUtils.deleteProfile(accessToken, pluginProfile.getPluginId(), profileId);
            BackMeUpUtils.deleteAuthData(accessToken, pluginProfile.getPluginId(), authDataId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testGetProfileSourceDummy() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO pluginProfile = TestDataManager.getProfileDummySource();
        String profileId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, pluginProfile.getPluginId(), pluginProfile);
            profileId = response.extract().path("profileId").toString();

            response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/plugins/" + pluginProfile.getPluginId() + "/" + profileId)
            .then()
                .log().all()
                .statusCode(200)
                .body("profileId", equalTo(Integer.parseInt(profileId)))
                .body("pluginId", equalTo(pluginProfile.getPluginId()))
                .body("profileType", equalTo(pluginProfile.getProfileType().toString()));

        } finally {
            BackMeUpUtils.deleteProfile(accessToken, pluginProfile.getPluginId(), profileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testGetProfileSourceFilegenerator() {	
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO pluginProfile = TestDataManager.getProfileFilegenerator();
        String profileId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, pluginProfile.getPluginId(), pluginProfile);
            profileId = response.extract().path("profileId").toString();

            given()
                .log().all()
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .get("/plugins/" + pluginProfile.getPluginId() + "/" + profileId)
            .then()
                .log().all()
                .statusCode(200);
        } finally {
            BackMeUpUtils.deleteProfile(accessToken, pluginProfile.getPluginId(), profileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testUpdateProfileSourceDummy() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO pluginProfile = TestDataManager.getProfileDummySource();
        String profileId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, pluginProfile.getPluginId(), pluginProfile);
            profileId = response.extract().path("profileId").toString();

            pluginProfile = BackMeUpUtils.getProfile(accessToken, pluginProfile.getPluginId(), profileId);			

            response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .body(pluginProfile, ObjectMapperType.JACKSON_1)
            .when()
                .put("/plugins/" + pluginProfile.getPluginId() + "/" + profileId)
            .then()
                .log().all()
                .statusCode(200)
                .body("profileId", equalTo(Integer.parseInt(profileId)))
                .body("pluginId", equalTo(pluginProfile.getPluginId()))
                .body("profileType", equalTo(pluginProfile.getProfileType().toString()));

        } finally {
            BackMeUpUtils.deleteProfile(accessToken, pluginProfile.getPluginId(), profileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }

    @Test
    public void testDeleteProfile() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        PluginProfileDTO pluginProfile = TestDataManager.getProfileDummySource();
        String profileId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, pluginProfile.getPluginId(), pluginProfile);
            profileId = response.extract().path("profileId").toString();

            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
            .when()
                .delete("/plugins/" + pluginProfile.getPluginId() + "/" + profileId)
            .then()
                .log().all()
                .statusCode(204);

        } finally {
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }


    @Ignore
    @Test
    public void testAddPluginProfileDropbox() {
        UserDTO user = TestDataManager.getUser();
        String userId = "";
        String accessToken = "";

        //		String pluginId = "org.backmeup.dropbox";
        //		String profileName = "DropboxProfile";
        //		PluginType profileType = PluginType.Source;
        //		pluginProfile.addProperty(Constants.KEY_SOURCE_TOKEN, Constants.VALUE_SOURCE_TOKEN);
        //		pluginProfile.addProperty(Constants.KEY_SOURCE_SECRET, Constants.VALUE_SOURCE_SECRET);
        PluginProfileDTO pluginProfile = TestDataManager.getProfileDummySource();
        String profileId = "";

        try {
            ValidatableResponse response = BackMeUpUtils.addUser(user);
            userId = response.extract().path("userId").toString();
            accessToken = BackMeUpUtils.authenticateUser(user);

            response = BackMeUpUtils.addProfile(accessToken, pluginProfile.getPluginId(), pluginProfile);
            profileId = response.extract().path("profileId").toString();

            response = 
            given()
                .log().all()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("Authorization", accessToken)
                .body(pluginProfile, ObjectMapperType.JACKSON_1)
            .when()
                .post("/plugins/" + pluginProfile.getPluginId())
            .then()
                .log().all()
                .statusCode(200);

            profileId = response.extract().path("profileId");

        } finally {
            BackMeUpUtils.deleteProfile(accessToken, pluginProfile.getPluginId(), profileId);
            BackMeUpUtils.deleteUser(accessToken, userId);
        }
    }
}
