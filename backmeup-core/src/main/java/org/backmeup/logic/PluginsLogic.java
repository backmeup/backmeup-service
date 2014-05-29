package org.backmeup.logic;

import java.util.List;
import java.util.Properties;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.dto.JobCreationRequest;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.api.connectors.Datasource;

public interface PluginsLogic {

    List<ActionProfile> getActionProfilesFor(JobCreationRequest request);

    List<String> getActionOptions(String actionId);

    List<ActionDescribable> getActions();

    List<SourceSinkDescribable> getConnectedDatasources();

    List<SourceSinkDescribable> getConnectedDatasinks();

    SourceSinkDescribable getSourceSinkById(String sourceSinkId);

    Datasource getDatasource(String profileDescription);

    Validationable getValidator(String description);

    SourceSinkDescribable getExistingSourceSink(String sourceSinkId);

    void validateSourceSinkExists(String sourceSinkId, ValidationNotes notes);

    AuthRequest configureAuth(Properties props, String uniqueDescIdentifier);

    String getAuthorizedUserId(String sourceSinkId, Properties props);

}