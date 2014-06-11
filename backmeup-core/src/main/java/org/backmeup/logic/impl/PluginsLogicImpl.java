package org.backmeup.logic.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.logic.PluginsLogic;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.dto.ActionProfileEntry;
import org.backmeup.model.dto.JobCreationRequest;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.spi.Authorizable;
import org.backmeup.plugin.spi.InputBased;
import org.backmeup.plugin.spi.OAuthBased;
import org.backmeup.plugin.spi.Authorizable.AuthorizationType;

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
    public List<ActionProfile> getActionProfilesFor(JobCreationRequest request) {
        List<ActionProfile> actions = new ArrayList<>();

        for (ActionProfileEntry action : request.getActions()) {
            ActionDescribable ad = plugins.getActionById(action.getId());

            if (ad == null) {
                throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_ACTION), action.getId()));
            }

            ActionProfile ap = new ActionProfile(ad.getId(), ad.getPriority());
            for (Map.Entry<String, String> entry : action.getOptions().entrySet()) {
                ap.addActionOption(entry.getKey(), entry.getValue());
            }
            actions.add(ap);
        }

        Collections.sort(actions);
        return actions;
    }

    @Override
    public List<String> getActionOptions(String actionId) {
        ActionDescribable action = plugins.getActionById(actionId);
        return action.getAvailableOptions();
    }

    @Override
    public List<ActionDescribable> getActions() {
        return plugins.getActions();
    }

    @Override
    public List<SourceSinkDescribable> getConnectedDatasources() {
        return plugins.getConnectedDatasources();
    }

    @Override
    public List<SourceSinkDescribable> getConnectedDatasinks() {
        return plugins.getConnectedDatasinks();
    }

    @Override
    public SourceSinkDescribable getSourceSinkById(String sourceSinkId) {
        return plugins.getSourceSinkById(sourceSinkId);
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
    public SourceSinkDescribable getExistingSourceSink(String sourceSinkId) {
        SourceSinkDescribable ssd = getSourceSinkById(sourceSinkId);
        if (ssd == null) {
            throw new IllegalArgumentException(String.format(textBundle.getString(UNKNOWN_SOURCE_SINK), sourceSinkId));
        }
        return ssd;
    }

    @Override
    public void validateSourceSinkExists(String sourceSinkId, ValidationNotes notes) {
        SourceSinkDescribable ssd = getSourceSinkById(sourceSinkId);
        if (ssd == null) {
            notes.addValidationEntry(ValidationExceptionType.PluginUnavailable, sourceSinkId);
        }
    }

    @Override
    public AuthRequest configureAuth(Properties props, String uniqueDescIdentifier) {
        props.setProperty("callback", callbackUrl);

        AuthRequest ar = new AuthRequest();
        
        Authorizable auth = plugins.getAuthorizable(uniqueDescIdentifier);
        switch (auth.getAuthType()) {
        case OAuth:
            OAuthBased oauth = plugins.getOAuthBasedAuthorizable(uniqueDescIdentifier);
            String redirectUrl = oauth.createRedirectURL(props, callbackUrl);
            ar.setRedirectURL(redirectUrl);
            // TODO Store all properties within keyserver & don't store them within the local database!
            break;
        case InputBased:
            InputBased ibased = plugins.getInputBasedAuthorizable(uniqueDescIdentifier);
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

        if (auth.getAuthType() == AuthorizationType.InputBased) {
            InputBased inputBasedService = plugins.getInputBasedAuthorizable(sourceSinkId);
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
