package org.backmeup.logic;

import java.util.List;
import java.util.Map;

import org.backmeup.model.AuthData;
import org.backmeup.model.PluginConfigInfo;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.Validationable;

public interface PluginsLogic {

    boolean isPluginAvailable(String pluginId);


    List<PluginDescribable> getActions();

    List<PluginDescribable> getDatasources();

    List<PluginDescribable> getDatasinks();

    PluginDescribable getPluginDescribableById(String pluginId);

    PluginConfigInfo getPluginConfigInfo(String pluginId);

    PluginConfigInfo getPluginConfigInfo(String pluginId, AuthData authData);


    boolean requiresValidation(String pluginId);

    Validationable getValidator(String description);

    ValidationNotes validatePlugin(String pluginId, Map<String, String> properties, List<String> options);


    boolean requiresAuthorization(String pluginId);

    String authorizePlugin(AuthData authData);

}