package org.backmeup.plugin.api;

import java.util.List;
import java.util.Map;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;

public interface InputBasedAuthorizable extends Authorizable {

    List<RequiredInputField> getRequiredInputFields();

    ValidationNotes validateInputFields(Map<String, String> properties);

    boolean isValid(Map<String, String> properties);

}
