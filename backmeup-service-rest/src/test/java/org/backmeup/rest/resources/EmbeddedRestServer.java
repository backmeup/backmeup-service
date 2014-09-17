package org.backmeup.rest.resources;

import org.backmeup.rest.auth.AllowAllSecurityInterceptor;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.junit.rules.ExternalResource;

/**
 * Start and stop an embedded RestEasy for tests.
 * 
 * @author Peter Kofler
 */
public class EmbeddedRestServer extends ExternalResource {

    private static final String HOST = "http://localhost:";
    private static final int PORT = 8089;

    public final String host = HOST;
    public final int port = PORT;
    private final Class<?> resource;
    private TJWSEmbeddedJaxrsServer server;

    public EmbeddedRestServer(Class<?> resource) {
        this.resource = resource;
    }

    @Override
    protected void before() {
        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(PORT);
        server.getDeployment().getActualProviderClasses().add(AllowAllSecurityInterceptor.class);
        server.getDeployment().getActualResourceClasses().add(resource);
        server.start();
    }

    @Override
    protected void after() {
        server.stop();
    }
}
