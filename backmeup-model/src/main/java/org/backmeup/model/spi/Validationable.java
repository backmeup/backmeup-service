package org.backmeup.model.spi;

import java.util.List;
import java.util.Map;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;

public interface Validationable {

    boolean hasRequiredProperties();

    List<RequiredInputField> getRequiredProperties();

    ValidationNotes validateProperties(Map<String, String> properties);

    boolean hasAvailableOptions();

    List<String> getAvailableOptions(Map<String, String> authData);

    ValidationNotes validateOptions(List<String> options);
}
