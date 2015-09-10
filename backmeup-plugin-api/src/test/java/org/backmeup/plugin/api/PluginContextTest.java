package org.backmeup.plugin.api;

import java.util.Enumeration;

import org.junit.Assert;
import org.junit.Test;

public class PluginContextTest {

    @Test
    public void testGetAttribute() {
        final String key = "test";
        final Object value = new Object();
        
        PluginContext context = new PluginContext();
        context.setAttribute(key, value);
        Object expectedVal = context.getAttribute(key);
        
        Assert.assertEquals(value, expectedVal);
    }
    
    @Test
    public void testGetAttributeString() {
        final String key = "test";
        final String value = "value";
        
        PluginContext context = new PluginContext();
        context.setAttribute(key, value);
        String expectedVal = context.getAttribute(key, String.class);
        
        Assert.assertEquals(value, expectedVal);
    }

    @Test
    public void testRemoveAttribute() {
        final String key = "test";
        final String value = "value";
        
        PluginContext context = new PluginContext();
        context.setAttribute(key, value);
        String expectedVal = context.getAttribute(key, String.class);
        Assert.assertEquals(value, expectedVal);
        
        Enumeration<String> names = context.getAttributeNames();
        Assert.assertTrue(names.hasMoreElements());
        
        context.removeAttribute(key);
        expectedVal = context.getAttribute(key, String.class);
        Assert.assertNull(expectedVal);
        
        names = context.getAttributeNames();
        Assert.assertFalse(names.hasMoreElements());
    }

    @Test
    public void testSetAttribute() {
        final String key = "test";
        final String value = "value";
        
        PluginContext context = new PluginContext();
        context.setAttribute(key, value);
        String expectedVal = context.getAttribute(key, String.class);
        Assert.assertEquals(value, expectedVal);
        
        Enumeration<String> names = context.getAttributeNames();
        Assert.assertTrue(names.hasMoreElements());
    }
    
    @Test
    public void testReplaceAttribute() {
        final String key = "test";
        final String value = "value";
        final String valueNew = "valueNew";
        
        PluginContext context = new PluginContext();
        context.setAttribute(key, value);
        String expectedVal = context.getAttribute(key, String.class);
        Assert.assertEquals(value, expectedVal);
        
        Enumeration<String> names = context.getAttributeNames();
        Assert.assertTrue(names.hasMoreElements());
        
        context.setAttribute(key, valueNew);
        expectedVal = context.getAttribute(key, String.class);
        Assert.assertEquals(valueNew, expectedVal);
    }

    @Test
    public void testSetAttributeReadOnly() {
        final String key = "test";
        final String value = "value";
        final String valueNew = "valueNew";
        
        PluginContext context = new PluginContext();
        context.setAttribute(key, value, true);
        String expectedVal = context.getAttribute(key, String.class);
        Assert.assertEquals(value, expectedVal);
        
        context.setAttribute(key, valueNew);
        expectedVal = context.getAttribute(key, String.class);
        Assert.assertEquals(value, expectedVal);
    }

    @Test
    public void testClearAttributes() {
        final String key = "test";
        final String value = "value";
        
        PluginContext context = new PluginContext();
        context.setAttribute(key, value);
        String expectedVal = context.getAttribute(key, String.class);
        Assert.assertEquals(value, expectedVal);
        
        Enumeration<String> names = context.getAttributeNames();
        Assert.assertTrue(names.hasMoreElements());
        
        context.clear();
        
        names = context.getAttributeNames();
        Assert.assertFalse(names.hasMoreElements());
    }
}
