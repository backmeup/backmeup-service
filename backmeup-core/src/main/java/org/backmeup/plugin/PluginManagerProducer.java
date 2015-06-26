package org.backmeup.plugin;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.plugin.infrastructure.PluginManager;
import org.backmeup.utilities.Assert;

@ApplicationScoped
public class PluginManagerProducer {
    @Inject
    @Configuration(key = "backmeup.osgi.deploymentDirectory")
    private String deploymentDirPath;

    @Inject
    @Configuration(key = "backmeup.osgi.temporaryDirectory")
    private String tempDirPath;

    @Inject
    @Configuration(key = "backmeup.osgi.exportedPackages")
    private String exportedPackages;

    private PluginManager pluginManager;

    @Produces
    @ApplicationScoped
    public PluginManager getPluginInfrastructure() {
        if (pluginManager == null) {
            Assert.notNull(deploymentDirPath, "Plugin deployment directory must not be null");
            Assert.notNull(tempDirPath, "Plugin temp data directory must not be null");
            Assert.notNull(exportedPackages, "Plugin exported packages must not be null");

            pluginManager = new PluginManager(deploymentDirPath, tempDirPath, exportedPackages);
        }
        return pluginManager;
    }
}
