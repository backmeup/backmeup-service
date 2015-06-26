package org.backmeup.logic.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.logic.PluginsLogic;
import org.backmeup.model.AuthData;
import org.backmeup.model.PluginConfigInfo;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.infrastructure.PluginManager;
import org.backmeup.plugin.spi.Authorizable;
import org.backmeup.plugin.spi.Authorizable.AuthorizationType;
import org.backmeup.plugin.spi.InputBasedAuthorizable;
import org.backmeup.plugin.spi.OAuthBasedAuthorizable;

@ApplicationScoped
public class PluginsLogicImpl implements PluginsLogic {
    private static final String VALIDATION_OF_ACCESS_DATA_FAILED = "org.backmeup.logic.impl.BusinessLogicImpl.VALIDATION_OF_ACCESS_DATA_FAILED";

    @Inject
    @Configuration(key="backmeup.callbackUrl")
    private String callbackUrl;

    @Inject
    private PluginManager pluginManager;

    private final ResourceBundle textBundle = ResourceBundle.getBundle("PluginsLogicImpl");

    // lifecycle methods ------------------------------------------------------

    @PostConstruct
    public void startup() {
        pluginManager.startup();
    }
    @PreDestroy
    public void shutdown() {
        pluginManager.shutdown();
    }

    // PluginsLogic methods ---------------------------------------------------

    @Override
    public boolean isPluginAvailable(String pluginId) {
        return pluginManager.isPluginAvailable(pluginId);
    }

    @Override
    public List<PluginDescribable> getActions() {
        return pluginManager.getActions();
    }

    @Override
    public List<PluginDescribable> getDatasources() {
        return pluginManager.getDatasources();
    }

    @Override
    public List<PluginDescribable> getDatasinks() {
        return pluginManager.getDatasinks();
    }

    @Override
    public PluginDescribable getPluginDescribableById(String sourceSinkId) {
        return pluginManager.getPluginDescribableById(sourceSinkId);
    }

    @Override
    public Validationable getValidator(String description) {
        return pluginManager.getValidator(description);
    }
    
    @Override 
    public PluginConfigInfo getPluginConfigInfo(String pluginId) {
        return getPluginConfigInfo(pluginId, null);
    }

    @Override
    public PluginConfigInfo getPluginConfigInfo(String pluginId, AuthData authData) {
        PluginConfigInfo pluginConfigInfo = new PluginConfigInfo();

        if(pluginManager.hasAuthorizable(pluginId)) {
            Authorizable auth = pluginManager.getAuthorizable(pluginId); 
            // Note that in the call above a java.lang.reflect.Proxy object is returned
            // We have to make a second call to get a proxy with the correct interface
            auth = pluginManager.getAuthorizable(pluginId, auth.getAuthType()); 

            switch (auth.getAuthType()) {
            case OAuth:
                OAuthBasedAuthorizable oauth = (OAuthBasedAuthorizable) auth;
                Map<String, String> oauthProps = new HashMap<>();
                String redirectUrl = oauth.createRedirectURL(oauthProps, callbackUrl);
                pluginConfigInfo.setRedirectURL(redirectUrl);
                // save oauth properties for later use in authorize method
                pluginConfigInfo.setOAuthProperties(oauthProps);
                break;
            case InputBased:
                InputBasedAuthorizable ibased = (InputBasedAuthorizable) auth;
                pluginConfigInfo.setRequiredInputs(ibased.getRequiredInputFields());
                break;
            default:
                throw new IllegalArgumentException("unknown enum value " + auth.getAuthType());
            }
        }

        if(pluginManager.hasValidator(pluginId)) {
            Validationable validator = pluginManager.getValidator(pluginId);
            if(validator.hasRequiredProperties()) {
                pluginConfigInfo.setPropertiesDescription(validator.getRequiredProperties());
            }

            if(validator.hasAvailableOptions()) {
                //At some calls we have properties (if we already have authentication data)
                Map<String, String> authProps = new HashMap<>();
                if (authData != null) {
                    authProps.putAll(authData.getProperties());
                }
                pluginConfigInfo.setAvailableOptions(validator.getAvailableOptions(authProps));
            }
        }

        return pluginConfigInfo;
    }

    @Override
    public boolean requiresAuthorization(String pluginId) {
        return pluginManager.hasAuthorizable(pluginId);
    }

    @Override
    public String authorizePlugin(AuthData authData) {
        if(!pluginManager.hasAuthorizable(authData.getPluginId())) {
            throw new PluginException(authData.getPluginId(), "Plugin doesn't provide an Authorizable");
        }
        
        Authorizable auth = pluginManager.getAuthorizable(authData.getPluginId());
        auth = pluginManager.getAuthorizable(authData.getPluginId(), auth.getAuthType()); 
        
        if (auth.getAuthType() == AuthorizationType.InputBased) {
            InputBasedAuthorizable inputBasedService = (InputBasedAuthorizable) auth;
            if (!inputBasedService.isValid(authData.getProperties())) {
                throw new ValidationException(ValidationExceptionType.AuthException, textBundle.getString(VALIDATION_OF_ACCESS_DATA_FAILED));
            }
        }
        
        // Note: After we call authorize, properties in authData may contain additional
        // or changed values.
        String accountIdentification = auth.authorize(authData.getProperties());
        
        return accountIdentification;
    }

    @Override
    public boolean requiresValidation(String pluginId) {
        return pluginManager.hasValidator(pluginId);
    }

    @Override
    public ValidationNotes validatePlugin(String pluginId, Map<String,String> properties, List<String> options) {
        if (!pluginManager.hasValidator(pluginId)) {
            throw new PluginException(pluginId, "Plugin doesn't provide a Validator");
        }

        Validationable validator = pluginManager.getValidator(pluginId);
        ValidationNotes notes = new ValidationNotes();

        if (validator.hasRequiredProperties()) {
            notes.addAll(validator.validateProperties(properties));
        }

        if (validator.hasAvailableOptions() && (options != null) && !options.isEmpty()) {
            notes.addAll(validator.validateOptions(options));
        }

        return notes;
    }
}
