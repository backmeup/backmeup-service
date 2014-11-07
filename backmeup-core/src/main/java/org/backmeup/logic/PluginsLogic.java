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

	boolean isPluginAvailable(String pluginId);


    List<PluginDescribable> getActions();

    List<PluginDescribable> getDatasources();

    List<PluginDescribable> getDatasinks();

    PluginDescribable getPluginDescribableById(String pluginId);
    
    PluginConfigInfo getPluginConfigInfo (String pluginId);

    
    boolean requiresValidation(String pluginId);
    
    Validationable getValidator(String description);
    
    ValidationNotes validatePlugin(String pluginId, Map<String, String> properties, List<String> options);


    boolean requiresAuthorization(String pluginId);
    
    String authorizePlugin(AuthData authData);
    
	
	// deprecated methods -----------------------------------------------------
    
	@Deprecated Datasource getDatasource(String profileDescription);
	@Deprecated void validateSourceSinkExists(String sourceSinkId, ValidationNotes notes);
    @Deprecated List<String> getActionOptions(String actionId);
    @Deprecated List<Profile> getActionProfilesFor(BackupJob request);
    @Deprecated PluginDescribable getExistingSourceSink(String sourceSinkId);
	@Deprecated AuthRequest configureAuth(Properties props, String uniqueDescIdentifier);
	@Deprecated String getAuthorizedUserId(String sourceSinkId, Properties props);
	

}