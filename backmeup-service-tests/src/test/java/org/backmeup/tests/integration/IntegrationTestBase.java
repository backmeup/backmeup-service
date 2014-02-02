package org.backmeup.tests.integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;

public abstract class IntegrationTestBase {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 8080;
		RestAssured.basePath = "/rest/backmeup";
		RestAssured.defaultParser = Parser.JSON;
		RestAssured.requestContentType("application/json");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		RestAssured.reset();
	}
}
