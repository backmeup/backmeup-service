package org.backmeup.model.spi;

import java.util.Map;

/**
 * A plugin is eather a source, an action or a sink plugin.
 * 
 * The id of the plugin must be unique within the system and
 * it must be used within the plugins spring configuration as
 * the filter for each service, e.g.
 * 
 * getId() = "org.backmeup.dropbox"
 * 
 * spring configuration must be:
 * 
 * <service id="dropboxDescriptorService" ref="dropboxDescriptor" auto-export="interfaces">
 *  <service-properties>
 *    <!-- value must be getId() -->
 *    <entry key="name" value="org.backmeup.dropbox"/>
 *  </service-properties>
 * </service>
 * 
 * The title can be displayed within a client.
 * 
 * The description should state what the plugin does.
 * 
 * 
 * Datasources and Datasinks must implement this interface.
 * The getType method returns if this plugin 
 * contains a source implementation, a sink implementation or both.
 * 
 * The imageURL returns a link to a thumbnail picture of this plugin.
 * 
 * @author fschoeppl
 *
 */
public interface PluginDescribable {
    public enum PluginType {
        Source,
        Sink,
        SourceSink,
        Action
    }

    public enum PluginVisibility {
        Global,
        Job,
        Hidden
    }

    String getId();
    String getTitle();
    String getDescription();

    PluginType getType();
    int getPriority();
    PluginVisibility getVisibility();

    String getImageURL();
    Map<String, String> getMetadata(Map<String, String> authData);
}
