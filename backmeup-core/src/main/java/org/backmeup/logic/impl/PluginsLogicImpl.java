package org.backmeup.logic.impl;

import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

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
import org.backmeup.plugin.Plugin;
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
    @Named("plugin")
    private Plugin plugins;

    private final ResourceBundle textBundle = ResourceBundle.getBundle("PluginsLogicImpl");

    // lifecycle methods ------------------------------------------------------

    @PreDestroy
    public void shutdown() {
        plugins.shutdown();
    }

    // PluginsLogic methods ---------------------------------------------------

    @Override
    public boolean isPluginAvailable(String pluginId) {
        return plugins.isPluginAvailable(pluginId);
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
    public Validationable getValidator(String description) {
        return plugins.getValidator(description);
    }
    
    @Override 
    public PluginConfigInfo getPluginConfigInfo(String pluginId) {
        return getPluginConfigInfo(pluginId, null);
    };

    @Override
    public PluginConfigInfo getPluginConfigInfo(String pluginId, AuthData authData) {
        PluginConfigInfo pluginConfigInfo = new PluginConfigInfo();

        if(plugins.hasAuthorizable(pluginId)) {
            Authorizable auth = plugins.getAuthorizable(pluginId); 
            // Note that in the call above a java.lang.reflect.Proxy object is returned
            // We have to make a second call to get a proxy with the correct interface
            auth = plugins.getAuthorizable(pluginId, auth.getAuthType()); 

            switch (auth.getAuthType()) {
            case OAuth:
                OAuthBasedAuthorizable oauth = (OAuthBasedAuthorizable) auth;
                Properties props = new Properties();
                String redirectUrl = oauth.createRedirectURL(props, callbackUrl);
                pluginConfigInfo.setRedirectURL(redirectUrl);
                // save oauth properties for later use in authorize method
                pluginConfigInfo.getOAuthProperties().clear();
                for (final String key: props.stringPropertyNames()) {
                    String value = props.getProperty(key);
                    pluginConfigInfo.addOAuthProperty(key, value);
                }
                break;
            case InputBased:
                InputBasedAuthorizable ibased = (InputBasedAuthorizable) auth;
                pluginConfigInfo.setRequiredInputs(ibased.getRequiredInputFields());
                break;
            default:
                throw new IllegalArgumentException("unknown enum value " + auth.getAuthType());
            }
        }

        if(plugins.hasValidator(pluginId)) {
            Validationable validator = plugins.getValidator(pluginId);
            if(validator.hasRequiredProperties()) {
                pluginConfigInfo.setPropertiesDescription(validator.getRequiredProperties());
            }

            if(validator.hasAvailableOptions()) {
                //At some calls we have properties (if we already have authentication data)
                Properties authProps = new Properties();
                if(authData != null) {
                    authProps.putAll(authData.getProperties());
                }
                pluginConfigInfo.setAvailableOptions(validator.getAvailableOptions(authProps));
            }
        }

        return pluginConfigInfo;
    }

    @Override
    public boolean requiresAuthorization(String pluginId) {
        return plugins.hasAuthorizable(pluginId);
    }

    @Override
    public String authorizePlugin(AuthData authData) {
        if(!plugins.hasAuthorizable(authData.getPluginId())) {
            throw new PluginException(authData.getPluginId(), "Plugin doesn't provide an Authorizable");
        }

        Authorizable auth = plugins.getAuthorizable(authData.getPluginId());
        auth = plugins.getAuthorizable(authData.getPluginId(), auth.getAuthType()); 

        // TODO: avoid Properties
        Properties authProps = new Properties();
        authProps.putAll(authData.getProperties());

        if (auth.getAuthType() == AuthorizationType.InputBased) {
            InputBasedAuthorizable inputBasedService = (InputBasedAuthorizable) auth;
            if (!inputBasedService.isValid(authProps)) {
                throw new ValidationException(ValidationExceptionType.AuthException, textBundle.getString(VALIDATION_OF_ACCESS_DATA_FAILED));
            }
        }

        String accountIdentification = auth.authorize(authProps);

        // After we call authorize, authProps may contain additional or changed values.
        // Therefore, we have to update the properties in authData
        authData.getProperties().clear();
        for (final String key: authProps.stringPropertyNames()) {
            String value = authProps.getProperty(key);
            authData.addProperty(key, value);
        }

        return accountIdentification;
    }

    @Override
    public boolean requiresValidation(String pluginId) {
        return plugins.hasValidator(pluginId);
    }

    @Override
    public ValidationNotes validatePlugin(String pluginId, java.util.Map<String,String> properties, java.util.List<String> options) {
        if(!plugins.hasValidator(pluginId)) {
            throw new PluginException(pluginId, "Plugin doesn't provide a Validator");
        }

        Validationable validator = plugins.getValidator(pluginId);
        ValidationNotes notes = new ValidationNotes();

        if(validator.hasRequiredProperties()){
            notes.addAll(validator.validateProperties(properties));
        }

        if(validator.hasAvailableOptions()) {
            notes.addAll(validator.validateOptions(options));
        }

        if(!notes.getValidationEntries().isEmpty()) {
            throw new ValidationException(ValidationExceptionType.ConfigException, "Validation of config data failed");
        }

        return notes;
    }
}
