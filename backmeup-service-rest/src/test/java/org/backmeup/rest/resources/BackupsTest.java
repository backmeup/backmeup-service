package org.backmeup.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.SearchResponse.CountedEntry;
import org.backmeup.model.SearchResponse.SearchEntry;
import org.backmeup.model.TestUser;
import org.backmeup.rest.auth.AllowAllSecurityInterceptor;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BackupsTest {

    private static final String HOST = "http://localhost:";
    private static final int PORT = 8081;

    private TJWSEmbeddedJaxrsServer server;
    private HttpClient client = new DefaultHttpClient();

    @Before
    public void startEmbeddedRestEasy() {
        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(PORT);
        server.getDeployment().getActualProviderClasses().add(AllowAllSecurityInterceptor.class);
        server.getDeployment().getActualResourceClasses().add(StubbedBackups.class);
        server.start();
    }

    private static final Long USER = TestUser.ACTIVE_USER_ID;
    private static final long ID = 2L;

    public static class StubbedBackups extends Backups {

        @Override
        protected BusinessLogic getLogic() {
            BusinessLogic logic = mock(BusinessLogic.class);
            when(logic.searchBackup(USER, "find_me")).thenReturn(ID);
            SearchResponse sr = createSearchResponse();
            when(logic.queryBackup(USER, ID, null)).thenReturn(sr);
            return logic;
        }
    }

    private static SearchResponse createSearchResponse() {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setId(ID);
        searchResponse.setQuery("find_me");
        searchResponse.setByJob(Arrays.asList(new CountedEntry("first Job", 1), new CountedEntry("next Job", 1)));
        searchResponse.setBySource(Arrays.asList(new CountedEntry("Dropbox", 2), new CountedEntry("Facebook", 2)));
        searchResponse.setByType(Arrays.asList(new CountedEntry("Type", 3)));
        searchResponse.setFiles(Arrays.asList(new SearchEntry("fileId", new Date(), "type", "A wonderfil file (title)", "thmbnailUrl",
                "Dropbpx", "first Job")));
        return searchResponse;
    }

    @After
    public void stopEmbeddedRestEasy() {
        server.stop();
    }

    @Test
    public void shouldCreateSearchBusinessLogicWithUserAndQuery() throws IOException {
        HttpPut put = new HttpPut(HOST + PORT + "/backups/" + USER + "/search");
        put.setEntity(new StringEntity("query=find_me", MediaType.APPLICATION_FORM_URLENCODED, null));
        HttpResponse response = client.execute(put);

        assertStatusCode(202, response);

        String locationHeader = response.getFirstHeader("location").getValue();
        assertEquals(HOST + PORT + "/backups/" + USER + "/" + ID + "/query", locationHeader);

        HttpEntity entity = response.getEntity();
        String body = IOUtils.toString(entity.getContent());
        assertEquals("{\"searchId\":2}", body);
    }

    private void assertStatusCode(int expectedStatus, HttpResponse response) {
        int responseCode = response.getStatusLine().getStatusCode();
        assertEquals(expectedStatus, responseCode);
    }

    @Test
    public void shouldGetSearchResultForCreatedQuery() throws IOException {
        HttpGet method = new HttpGet(HOST + PORT + "/backups/" + USER + "/" + ID + "/query");
        HttpResponse response = client.execute(method);

        assertStatusCode(200, response);

        HttpEntity entity = response.getEntity();
        String body = IOUtils.toString(entity.getContent());
        System.out.println(body);
        assertTrue(body.indexOf("\"searchQuery\":\"find_me\"") >= 0);
        assertTrue(body.indexOf("\"byJob\":[{\"title\":\"first Job\",\"count\":1},{\"title\":\"next Job\",\"count\":1}]") >= 0);
        assertTrue(body.indexOf("\"byType\":[{\"title\":\"Type\",\"count\":3}]") >= 0);
        assertTrue(body.indexOf("\"bySource\":[{\"title\":\"Dropbox\",\"count\":2},{\"title\":\"Facebook\",\"count\":2}]") >= 0);
        assertTrue(body.indexOf("\"files\":[{\"fileId\":\"fileId\",") >= 0);
    }

}
