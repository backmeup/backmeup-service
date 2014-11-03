package org.backmeup.logic;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.model.AuthData;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackupJob;
import org.backmeup.model.PluginConfigInfo;
import org.backmeup.model.Profile;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.api.connectors.Datasource;

public interface PluginsLogic {

    @Deprecated List<Profile> getActionProfilesFor(BackupJob request);

    List<String> getActionOptions(String actionId);

    List<PluginDescribable> getActions();

    List<PluginDescribable> getDatasources();

    List<PluginDescribable> getDatasinks();

    PluginDescribable getPluginDescribableById(String pluginId);

    Datasource getDatasource(String profileDescription);

    Validationable getValidator(String description);

    @Deprecated PluginDescribable getExistingSourceSink(String sourceSinkId);

    void validateSourceSinkExists(String sourceSinkId, ValidationNotes notes);

    @Deprecated AuthRequest configureAuth(Properties props, String uniqueDescIdentifier);
    
    PluginConfigInfo getPluginConfigInfo (String pluginId);
    
    boolean requiresAuthorization(String pluginId);
    
    String authorizePlugin(AuthData authData);
    
    boolean requiresValidation(String pluginId);
    
    ValidationNotes validatePlugin(String pluginId, Map<String, String> properties, List<String> options);
    
    @Deprecated String getAuthorizedUserId(String sourceSinkId, Properties props);

	boolean isPluginAvailable(String pluginId);

}