package org.backmeup.plugin;

import java.util.List;

import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.api.connectors.Action;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.spi.Authorizable;
import org.backmeup.plugin.spi.Authorizable.AuthorizationType;

/**
 * The Plugin interface 
 * encapsulates all operations
 * that interact with a plugin.
 * 
 * If an error occurs, a PluginException will be thrown.
 * 
 * @author fschoeppl
 *
 */
public interface Plugin {

	List<PluginDescribable> getDatasources();
	
	List<PluginDescribable> getDatasinks();
	
	List<PluginDescribable> getActions();
	
	PluginDescribable getPluginDescribableById(String pluginId);	

	
	
	Datasink getDatasink(String sinkId);
	
	Datasource getDatasource(String sourceId);
	
	Action getAction(String actionId);
	
	
	
	Authorizable getAuthorizable(String sourceSinkId);
	
	Authorizable getAuthorizable(String sourceSinkId, AuthorizationType authType);
	
	Validationable getValidator(String sourceSinkId);
	
	
	void shutdown();

	void startup();
}
