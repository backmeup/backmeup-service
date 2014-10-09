package org.backmeup.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.backmeup.index.model.SearchResponse;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.model.FakeSearchResponse;
import org.backmeup.model.FakeUser;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.junit.Rule;
import org.junit.Test;

public class BackupsTest {

    @Rule
    public final EmbeddedRestServer server = new EmbeddedRestServer(BackupsWithMockedLogic.class);
    private final String HOST = server.host;
    private final int PORT = server.port;

    private HttpClient client = new DefaultHttpClient();
    private static final String DOZER_SEARCH_MAPPING = "dozer-search-mapping.xml";

    private static final Long USER = FakeUser.ACTIVE_USER_ID;
    private static final Long ID = FakeSearchResponse.SEARCH_ID;

    public static class BackupsWithMockedLogic extends Backups {
        @Override
        protected BusinessLogic getLogic() {
            BusinessLogic logic = mock(BusinessLogic.class);
            when(logic.searchBackup(USER, "find_me")).thenReturn(ID);
            SearchResponse sr = FakeSearchResponse.oneFile();
            when(logic.queryBackup(USER, ID, null)).thenReturn(sr);
            return logic;
        }
        @Override
        protected Mapper getMapper() {
            return new DozerBeanMapper(Arrays.asList(DOZER_SEARCH_MAPPING));
        }
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
        assertEquals("{\"searchId\":" + ID + "}", body);
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

    private void assertStatusCode(int expectedStatus, HttpResponse response) {
        int responseCode = response.getStatusLine().getStatusCode();
        assertEquals(expectedStatus, responseCode);
    }
}
