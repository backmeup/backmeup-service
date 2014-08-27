package org.backmeup.plugin.api.connectors;

import java.util.LinkedList;
import java.util.List;

import org.backmeup.model.spi.PluginDescribable;

public abstract class BaseSourceSinkDescribable implements PluginDescribable {

	@Override
	public int getPriority() {
		return -1;
	}

	@Override
	public List<String> getAvailableOptions() {
		return new LinkedList<>();
	}

	@Override
	public PluginVisibility getVisibility() {
		return PluginVisibility.Job;
	}
}
