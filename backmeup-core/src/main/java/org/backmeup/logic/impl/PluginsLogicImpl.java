package org.backmeup.logic.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.logic.PluginsLogic;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.spi.Authorizable;
import org.backmeup.plugin.spi.Authorizable.AuthorizationType;
import org.backmeup.plugin.spi.InputBased;
import org.backmeup.plugin.spi.OAuthBased;

@ApplicationScoped
public class PluginsLogicImpl implements PluginsLogic {

    private static final String UNKNOWN_SOURCE_SINK = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_SOURCE_SINK";
    private static final String VALIDATION_OF_ACCESS_DATA_FAILED = "org.backmeup.logic.impl.BusinessLogicImpl.VALIDATION_OF_ACCESS_DATA_FAILED";
    private static final String UNKNOWN_ACTION = "org.backmeup.logic.impl.BusinessLogicImpl.UNKNOWN_ACTION";

    @Inject
    @Configuration(key="backmeup.callbackUrl")
    private String callbackUrl;

    @Inject
    @Named("plugin")
    private Plugin plugins;

    private final ResourceBundle textBundle = ResourceBundle.getBundle("PluginsLogicImpl");

    @Override
    public List<Profile> getActionProfilesFor(BackupJob request) {
        List<Profile> actions = new ArrayList<>();

        for (Profile action : request.getActionProfiles()) {
//            PluginDescribable ad = plugins.getPluginDescribableById(action.getActionId());
//
//            if (ad == null) {
//                throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_ACTION), action.getId()));
//            }

//            Profile ap = new Profile(ad.getId(), ad.getPriority());
//            for (Entry<String, String> entry : action.getProperties().entrySet()) {
//                ap.addProperty(entry.getKey(), entry.getValue());
//            }
//            actions.add(ap);
        	
        	//TODO: Verify why code above is necessary 
        	actions.add(action);
        }

//        Collections.sort(actions);
        return actions;
    }

    @Override
    public List<String> getActionOptions(String actionId) {
        PluginDescribable action = plugins.getPluginDescribableById(actionId);
        return action.getAvailableOptions();
    }

    @Override
    public List<PluginDescribable> getActions() {
        return plugins.getActions();
    }

    @Override
    public List<PluginDescribable> getDatasources() {
        return plugins.getDatasources();
    }

    @Override
    public List<PluginDescribable> getDatasinks() {
        return plugins.getDatasinks();
    }

    @Override
    public PluginDescribable getPluginDescribableById(String sourceSinkId) {
        return plugins.getPluginDescribableById(sourceSinkId);
    }

    @Override
    public Datasource getDatasource(String profileDescription) {
        return plugins.getDatasource(profileDescription);
    }

    @Override
    public Validationable getValidator(String description) {
        return plugins.getValidator(description);
    }

    @Override
    public PluginDescribable getExistingSourceSink(String sourceSinkId) {
        PluginDescribable ssd = getPluginDescribableById(sourceSinkId);
        if (ssd == null) {
            throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_SOURCE_SINK), sourceSinkId));
        }
        return ssd;
    }

    @Override
    public void validateSourceSinkExists(String sourceSinkId, ValidationNotes notes) {
        PluginDescribable ssd = getPluginDescribableById(sourceSinkId);
        if (ssd == null) {
            notes.addValidationEntry(ValidationExceptionType.PluginUnavailable, sourceSinkId);
        }
    }

    @Override
    public AuthRequest configureAuth(Properties props, String uniqueDescIdentifier) {
    	props.setProperty("callback", callbackUrl);

    	AuthRequest ar = new AuthRequest();

    	Authorizable auth = plugins.getAuthorizable(uniqueDescIdentifier); 
    	// Note that in the call above a java.lang.reflect.Proxy object is returned
    	// We have to make a second call to get a proxy with the correct interface
    	auth = plugins.getAuthorizable(uniqueDescIdentifier, auth.getAuthType()); 

        switch (auth.getAuthType()) {
        case OAuth:
        	OAuthBased oauth = (OAuthBased) auth;
            String redirectUrl = oauth.createRedirectURL(props, callbackUrl);
            ar.setRedirectURL(redirectUrl);
            // TODO Store all properties within keyserver & don't store them within the local database!
            break;
        case InputBased:
        	InputBased ibased = (InputBased) auth;
            ar.setRequiredInputs(ibased.getRequiredInputFields());
            break;
        default:
            throw new IllegalArgumentException("unknown enum value " + auth.getAuthType());
        }
        
        return ar;
    }

    @Override
    public String getAuthorizedUserId(String sourceSinkId, Properties props) {
        Authorizable auth = plugins.getAuthorizable(sourceSinkId);
        auth = plugins.getAuthorizable(sourceSinkId, auth.getAuthType()); 

        if (auth.getAuthType() == AuthorizationType.InputBased) {
            InputBased inputBasedService = (InputBased) auth;
            if (!inputBasedService.isValid(props)) {
                throw new ValidationException(ValidationExceptionType.AuthException, textBundle.getString(VALIDATION_OF_ACCESS_DATA_FAILED));
            }
        }

        return auth.postAuthorize(props);
    }

    @PreDestroy
    public void shutdown() {
        plugins.shutdown();
    }
}
