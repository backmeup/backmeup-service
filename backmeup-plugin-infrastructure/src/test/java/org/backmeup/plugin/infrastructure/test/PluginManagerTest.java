package org.backmeup.plugin.infrastructure.test;

import java.util.List;

import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.plugin.infrastructure.PluginManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PluginManagerTest {
    private PluginManager pluginManager;

    @Before
    public void setUp() {
        pluginManager = new PluginManager(
                "../autodeploy",
                "/data/backmeup-service/cache",
                "org.backmeup.plugin.spi org.backmeup.model org.backmeup.model.spi org.backmeup.plugin.api.connectors org.backmeup.plugin.api.storage com.google.gson org.backmeup.plugin.api");

        pluginManager.startup();
        pluginManager.waitForInitialStartup();
        System.out.println("Startup finished!");
    }

    @Test
    public void testPluginLayer() {
        try {
            Thread.sleep(2000);
        } catch (Exception e) {

        }
        List<PluginDescribable> sources = pluginManager.getDatasources();
        for (int i = 0; i < sources.size(); i++) {
            System.out.println(sources.get(i).getId());
        }

        List<PluginDescribable> sinks = pluginManager.getDatasinks();
        for (int i = 0; i < sinks.size(); i++) {
            System.out.println(sinks.get(i).getId());
        }

        if (sources.size() > 0 && sinks.size() > 0) {
            Assert.assertNotNull(pluginManager
                    .getDatasink("org.backmeup.dummy"));
            Assert.assertNotNull(pluginManager
                    .getDatasource("org.backmeup.dummy"));
        }
    }
}
