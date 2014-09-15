package org.backmeup.rest.resources;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class BackupsTest {

    private TJWSEmbeddedJaxrsServer server;

    @Before
    public void start() {
        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(8081);
        server.getDeployment().getActualResourceClasses().add(Backups.class);
        server.start();
    }

    @After
    public void stop() {
        server.stop();
    }

    @Test @Ignore("not implemented")
    public void testAbderaFeed() throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet("http://localhost:8081/atom/feed");
        HttpResponse response = client.execute(method);
        assertEquals(200, response.getStatusLine().getStatusCode());
        String str = response.getEntity().toString();
        System.out.println(str);
        HttpPut put = new HttpPut("http://localhost:8081/atom/feed");
        put.setEntity(new StringEntity(str, MediaType.APPLICATION_ATOM_XML, null));
        response = client.execute(put);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

}
