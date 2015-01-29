package org.backmeup.plugin.api.connectors;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.spi.PluginDescribable;

public abstract class BaseActionDescribable implements PluginDescribable {

	private Properties descriptionEntries;

	private final String propertyFilename;

	public BaseActionDescribable(String propertyFilename) {
		this.propertyFilename = propertyFilename;
	}

	public BaseActionDescribable() {
		this.propertyFilename = "action.properties";
	}

	private Properties getDescriptionEntries() throws PluginException {
		if (descriptionEntries == null) {
			try (InputStream  is = getClass().getClassLoader().getResourceAsStream(propertyFilename)) {
				if (is == null) {
					throw new PluginException("UNKWN", "Please provide "
							+ propertyFilename + " for your plugins!");
				}
				descriptionEntries = new Properties();
				descriptionEntries.load(is);
			} catch (IOException e) {
				throw new PluginException("UNKWN", "Unable to load from "
						+ propertyFilename + " stream!", e);
			}
		}
		return descriptionEntries;
	}

	@Override
	public String getId() {
		return getDescriptionEntries().getProperty("actionId");
	}

	@Override
	public String getTitle() {
		return getDescriptionEntries().getProperty("actionTitle");
	}

	@Override
	public String getDescription() {
		return getDescriptionEntries().getProperty("actionDescription");
	}

	@Override
	public int getPriority() {
		return Integer.parseInt(getDescriptionEntries().getProperty("actionPriority"));
	}

	@Override
	public Properties getMetadata(Properties accessData) {
		return new Properties();
	}

	@Override
	public PluginVisibility getVisibility() {
		String visibility = getDescriptionEntries().getProperty("actionVisibility");
		return PluginVisibility.valueOf(visibility);
	}

	@Override
	public PluginType getType() {
		return PluginType.Action;
	}

	@Override
	public String getImageURL() {
		return null;
	}
}
