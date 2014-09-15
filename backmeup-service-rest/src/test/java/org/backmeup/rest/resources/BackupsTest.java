package org.backmeup.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

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
import org.backmeup.model.TestUser;
import org.backmeup.rest.auth.AllowAllSecurityInterceptor;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
            return logic;
        }
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

        int responseCode = response.getStatusLine().getStatusCode();
        assertEquals(202, responseCode);

        String locationHeader = response.getFirstHeader("location").getValue();
        assertEquals(HOST + PORT + "/backups/" + USER + "/" + ID + "/query", locationHeader);

        HttpEntity entity = response.getEntity();
        String body = IOUtils.toString(entity.getContent());
        assertEquals("{\"searchId\":2}", body);
    }

    @Test
    @Ignore("not implemented")
    public void testname() throws IOException {
        HttpGet method = new HttpGet(HOST + PORT + "/backups/" + USER + "/search?query=find_me");
        HttpResponse response = client.execute(method);
        assertEquals(200, response.getStatusLine().getStatusCode());
        String str = response.getEntity().toString();
        System.out.println(str);
    }

}
