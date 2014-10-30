package org.backmeup.plugin.spi;

import java.util.List;
import java.util.Properties;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;

public interface InputBasedAuthorizable extends Authorizable {

	public List<RequiredInputField> getRequiredInputFields();
	
	public ValidationNotes validateInputFields(Properties properties);

	public boolean isValid(Properties inputs);

}
