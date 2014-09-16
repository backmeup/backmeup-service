package org.backmeup.model.spi;

import java.util.Properties;

import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.connectors.BaseSourceSinkDescribable;

public class FakePluginDescribable extends BaseSourceSinkDescribable {
    private final String pluginId;

    public FakePluginDescribable(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public String getId() {
        return pluginId;
    }

    @Override
    public String getTitle() {
        return "BackMeUp Dropbox Plug-In";
    }

    @Override
    public String getDescription() {
        return "A plug-in that is capable of downloading and uploading from dropbox";
    }

    @Override
    public String getImageURL() {
        return "http://about:blank";
    }

    @Override
    public PluginType getType() {
        return PluginType.SourceSink;
    }

    @Override
    public Properties getMetadata(@SuppressWarnings("unused") Properties accessData) {
        Properties metadata = new Properties();
        metadata.setProperty(Metadata.BACKUP_FREQUENCY, "daily");
        metadata.setProperty(Metadata.FILE_SIZE_LIMIT, "150");
        metadata.setProperty(Metadata.QUOTA_LIMIT, "2048");
        return metadata;
    }
}