package org.backmeup.model;


import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class ProfileTest {
    @Test
    public void testNullPropertiesAndOptions() {
        Profile p = new Profile();
        Assert.assertNull(p.getProperties());
        Assert.assertNull(p.getOptions());
        
        String data = p.getPropertiesAndOptionsAsEncodedString();
        
        Profile pCopy = new Profile();
        pCopy.setPropertiesAndOptionsFromEncodedString(data);
        Assert.assertNull(pCopy.getProperties());
        Assert.assertNull(pCopy.getOptions());
    }
        
    @Test
    public void testSerializeProperties() {        
        Profile p = new Profile();
        p.addProperty("username", "john.doe");
        p.addProperty("password", "abc123!$");
        
        Assert.assertNotNull(p.getProperties());
        Assert.assertEquals(p.getProperties().size(), 2);
        Assert.assertNull(p.getOptions());
        
        String data = p.getPropertiesAndOptionsAsEncodedString();
                
        Profile pCopy = new Profile();
        pCopy.setPropertiesAndOptionsFromEncodedString(data);
        
        Assert.assertNotNull(pCopy.getProperties());
        Assert.assertEquals(pCopy.getProperties().size(), 2);
        Assert.assertEquals(p.getProperties().toString(), pCopy.getProperties().toString());
        Assert.assertNull(pCopy.getOptions());
    }
    
    @Test
    public void testSerializeOptions() {        
        Profile p = new Profile();
        p.addOption("-DskipTests");
        p.addOption("-Debug");
        
        Assert.assertNotNull(p.getOptions());
        Assert.assertEquals(p.getOptions().size(), 2);
        Assert.assertNull(p.getProperties());
        
        String data = p.getPropertiesAndOptionsAsEncodedString();
                
        Profile pCopy = new Profile();
        pCopy.setPropertiesAndOptionsFromEncodedString(data);
        
        Assert.assertNotNull(pCopy.getOptions());
        Assert.assertEquals(pCopy.getOptions().size(), 2);
        Assert.assertEquals(p.getOptions().toString(), pCopy.getOptions().toString());
        Assert.assertNull(pCopy.getProperties());
    }
    
    @Test
    public void testSerializeLargePropertiesOptions() {       
        final int NUM = 1000;
        
        Profile p = new Profile();
        for (int i = 0; i < NUM; i++) {
            p.addProperty(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            p.addOption(UUID.randomUUID().toString());
        }
        Assert.assertNotNull(p.getProperties());
        Assert.assertEquals(p.getProperties().size(), NUM);
        Assert.assertNotNull(p.getOptions());
        Assert.assertEquals(p.getOptions().size(), NUM);
        
        String data = p.getPropertiesAndOptionsAsEncodedString();
        
        Profile pCopy = new Profile();
        pCopy.setPropertiesAndOptionsFromEncodedString(data);
        Assert.assertNotNull(pCopy.getProperties());
        Assert.assertEquals(pCopy.getProperties().size(), NUM);
        Assert.assertEquals(p.getProperties().toString(), pCopy.getProperties().toString());
        
        Assert.assertNotNull(pCopy.getOptions());
        Assert.assertEquals(pCopy.getOptions().size(), NUM);
        Assert.assertEquals(p.getOptions().toString(), pCopy.getOptions().toString());
    }
}
