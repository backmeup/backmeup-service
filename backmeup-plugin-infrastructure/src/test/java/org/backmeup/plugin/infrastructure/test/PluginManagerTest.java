package org.backmeup.plugin.osgi.impl.test;

import java.util.List;

import org.junit.Assert;

import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.osgi.PluginImpl;
import org.junit.Before;
import org.junit.Test;

public class PluginImplTest {
  private Plugin pluginLayer;
  
  @Before
  public void setUp() {
    pluginLayer = new PluginImpl("../autodeploy", 
        "/data/backmeup-service/cache", 
        "org.backmeup.plugin.spi org.backmeup.model org.backmeup.model.spi org.backmeup.plugin.api.connectors org.backmeup.plugin.api.storage com.google.gson org.backmeup.plugin.api"
        );
    
    pluginLayer.startup();
    ((PluginImpl)pluginLayer).waitForInitialStartup();
    System.out.println("Startup finished!");
  }
  
  @Test
  public void testPluginLayer() {
    try {
      Thread.sleep(2000);
    } catch (Exception e) {
      
    }
    List<PluginDescribable>  sources = pluginLayer.getDatasources();
    for (int i=0; i < sources.size(); i++) {
      System.out.println(sources.get(i).getId());
    }
    
    List<PluginDescribable>  sinks = pluginLayer.getDatasinks();
    for (int i=0; i < sinks.size(); i++) {
      System.out.println(sinks.get(i).getId());
    }
    
    if (sources.size() > 0 && sinks.size() > 0) {
      Assert.assertNotNull(pluginLayer.getDatasink("org.backmeup.dummy"));
      Assert.assertNotNull(pluginLayer.getDatasource("org.backmeup.dummy"));
    }
  }
}
