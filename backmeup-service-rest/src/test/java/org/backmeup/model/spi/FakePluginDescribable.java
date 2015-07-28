package org.backmeup.model.spi;

import java.util.HashMap;
import java.util.Map;

import org.backmeup.plugin.api.BaseSourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;

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
    public Map<String, String> getMetadata(@SuppressWarnings("unused") Map<String, String> authData ) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(Metadata.BACKUP_FREQUENCY, "daily");
        metadata.put(Metadata.FILE_SIZE_LIMIT, "150");
        metadata.put(Metadata.QUOTA_LIMIT, "2048");
        return metadata;
    }
}