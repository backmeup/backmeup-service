package org.backmeup.plugin.spi;

import java.util.List;
import java.util.Map;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;

public interface InputBasedAuthorizable extends Authorizable {

	public List<RequiredInputField> getRequiredInputFields();
	
	public ValidationNotes validateInputFields(Map<String, String> properties);

	public boolean isValid(Map<String, String> properties);

}
