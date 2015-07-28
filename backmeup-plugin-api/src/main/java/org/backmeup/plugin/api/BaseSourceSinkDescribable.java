package org.backmeup.plugin.api;

import org.backmeup.model.spi.PluginDescribable;

public abstract class BaseSourceSinkDescribable implements PluginDescribable {

    @Override
    public int getPriority() {
        return -1;
    }

    @Override
    public PluginVisibility getVisibility() {
        return PluginVisibility.Job;
    }
}
