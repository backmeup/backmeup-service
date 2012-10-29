package org.backmeup.zip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.model.spi.RequiredInputField;
import org.backmeup.model.spi.RequiredInputField.Type;
import org.backmeup.plugin.spi.InputBased;

public class ZipAuthenticator implements InputBased {

  @Override
  public AuthorizationType getAuthType() {
    return AuthorizationType.InputBased;
  }

  @Override
  public void postAuthorize(Properties inputProperties) {
    // Nothing to do here
  }

  @Override
  public List<RequiredInputField> getRequiredInputFields() {
    // Empty list will do just fine
    return new ArrayList<RequiredInputField>();
  }

  @Override
  public Map<String, Type> getTypeMapping() {
    // Empty map will do just fine
    return new HashMap<String, Type>();
  }

  @Override
  public boolean isValid(Properties inputs) {
    // We don't need authentication data here, its always ok
    return true;
  }

}
