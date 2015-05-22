package org.backmeup.keyserver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.utilities.Assert;

@ApplicationScoped
public class KeyserverClientProducer {
    @Inject
    @Configuration(key = "backmeup.keyserver.baseUrl")
    private String baseUrl;
    
    @Inject
    @Configuration(key = "backmeup.service.appId")
    private String appId;
    
    @Inject
    @Configuration(key = "backmeup.service.appSecret")
    private String appSecret;   
    
    private KeyserverClient keyserverClient;
    
    @Produces
    @ApplicationScoped
    public KeyserverClient getKeyserverClient() {
        if(keyserverClient == null) {
            Assert.notNull(baseUrl, "Keyserver base url must not be null");
            Assert.notNull(appId, "Keyserver app id must not be null");
            Assert.notNull(appSecret, "Keyserver app secret must not be null");
            
            keyserverClient = new KeyserverClient(baseUrl, appId, appSecret);
        }
        return keyserverClient;
    }
}
