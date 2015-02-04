package org.backmeup.model.spi;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;

public interface Validationable {
	
	public boolean hasRequiredProperties();
	
	public List<RequiredInputField> getRequiredProperties();

	public ValidationNotes validateProperties(Map<String, String> properties);
	
	public boolean hasAvailableOptions();
	
	public List<String> getAvailableOptions(Properties accessData);

	public ValidationNotes validateOptions(List<String> options);
}
