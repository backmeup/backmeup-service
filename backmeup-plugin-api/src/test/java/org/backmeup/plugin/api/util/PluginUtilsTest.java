package org.backmeup.plugin.api.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.backmeup.plugin.api.util.PluginUtils.QueryParameters;
import org.junit.Assert;
import org.junit.Test;

public class PluginUtilsTest {
    @Test
    public void testSplitQuery() throws UnsupportedEncodingException {
        String queryParams = "param1=value1&param2=value2";
        QueryParameters params = PluginUtils.splitQuery(queryParams);
        
        String val1 = params.getParameter("param1");
        Assert.assertNotNull(val1);
        Assert.assertFalse(val1.isEmpty());
        Assert.assertEquals("value1", val1);
        
        String val2 = params.getParameter("param2");
        Assert.assertNotNull(val2);
        Assert.assertFalse(val2.isEmpty());
        Assert.assertEquals("value2", val2);
    }
    
    @Test
    public void testSplitQueryUrl() throws UnsupportedEncodingException, MalformedURLException {
        URL url = new URL("http://example.com/test?param=value");
        QueryParameters params = PluginUtils.splitQuery(url);
        
        String val = params.getParameter("param");
        Assert.assertNotNull(val);
        Assert.assertFalse(val.isEmpty());
        Assert.assertEquals("value", val);
    }
    
    @Test
    public void testGetParameterNull() throws UnsupportedEncodingException {
        String queryParams = "http://example.com/test?param1=value1&param2=value2";
        QueryParameters params = PluginUtils.splitQuery(queryParams);
        
        String val = params.getParameter("null");
        Assert.assertNull(val);
    }
}
