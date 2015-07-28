package org.backmeup.plugin.api;

import org.backmeup.model.spi.PluginDescribable.PluginVisibility;
import org.junit.Assert;
import org.junit.Test;


public class BaceActionDescribableTest {
    private static final String ID = "org.backmeup.test";
    private static final String TITLE = "TestAction";
    private static final String DESCRIPTION = "Test plugin";
    private static final int PRIORITY = 4711;
    private static final PluginVisibility VISIBILITY = PluginVisibility.Global;
    
    @Test
    public void testPropertiesFromDefaultFile() {
        MyBaseActionDescribable desc = new MyBaseActionDescribable();
        Assert.assertEquals(ID, desc.getId());
        Assert.assertEquals(TITLE, desc.getTitle());
        Assert.assertEquals(DESCRIPTION, desc.getDescription());
        Assert.assertEquals(PRIORITY, desc.getPriority());
        Assert.assertEquals(VISIBILITY, desc.getVisibility());
    }
    
    @Test
    public void testPropertiesFromCustomFile() {
        MyBaseActionDescribable desc = new MyBaseActionDescribable("actionTest.properties");
        Assert.assertEquals(ID, desc.getId());
        Assert.assertEquals(TITLE, desc.getTitle());
        Assert.assertEquals(DESCRIPTION, desc.getDescription());
        Assert.assertEquals(PRIORITY, desc.getPriority());
        Assert.assertEquals(VISIBILITY, desc.getVisibility());
    }
    
    private class MyBaseActionDescribable extends BaseActionDescribable {
        public MyBaseActionDescribable() {
            super();
        }
        
        public MyBaseActionDescribable(String propertyFilename) {
            super(propertyFilename);
        }
    }
}
