package org.backmeup.model;


import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class AuthDataTest {
    @Test
    public void testNullProperties() {
        AuthData ad = new AuthData();
        Assert.assertNull(ad.getProperties());
        
        String data = ad.getPropertiesAsEncodedString();
        
        AuthData adCopy = new AuthData();
        adCopy.setPropertiesFromEncodedString(data);
        Assert.assertNull(adCopy.getProperties());
    }
        
    @Test
    public void testSerializeSmallProperties() {        
        AuthData ad = new AuthData();
        ad.addProperty("username", "john.doe");
        ad.addProperty("password", "abc123!$");
        
        Assert.assertNotNull(ad.getProperties());
        Assert.assertEquals(ad.getProperties().size(), 2);
        
        String data = ad.getPropertiesAsEncodedString();
                
        AuthData adCopy = new AuthData();
        adCopy.setPropertiesFromEncodedString(data);
        
        Assert.assertNotNull(adCopy.getProperties());
        Assert.assertEquals(adCopy.getProperties().size(), 2);
        Assert.assertEquals(ad.getProperties().toString(), adCopy.getProperties().toString());
    }
    
    @Test
    public void testSerializeLargeProperties() {       
        final int NUM = 100000;
        
        AuthData ad = new AuthData();
        for (int i = 0; i < NUM; i++) {
            ad.addProperty(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }
        Assert.assertNotNull(ad.getProperties());
        Assert.assertEquals(ad.getProperties().size(), NUM);
        
        String data = ad.getPropertiesAsEncodedString();
        
        AuthData adCopy = new AuthData();
        adCopy.setPropertiesFromEncodedString(data);
        Assert.assertNotNull(adCopy.getProperties());
        Assert.assertEquals(adCopy.getProperties().size(), NUM);
        Assert.assertEquals(ad.getProperties().toString(), adCopy.getProperties().toString());
    }
}
