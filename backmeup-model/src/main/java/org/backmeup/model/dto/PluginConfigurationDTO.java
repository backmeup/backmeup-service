package org.backmeup.model.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PluginConfigurationDTO {
    public enum PluginConfigurationType {
        input, oauth
    }

    private PluginConfigurationType configType;
    private String redirectURL;
    private List<PluginInputFieldDTO> requiredInputs;
    private Map<String, String> properties;

    public PluginConfigurationDTO() {

    }

    public PluginConfigurationDTO( PluginConfigurationType type) {
        this.configType = type;
    }

    public PluginConfigurationType getConfigType() {
        return configType;
    }

    public void setConfigType(PluginConfigurationType configType) {
        this.configType = configType;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, String value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
    }

    public List<PluginInputFieldDTO> getRequiredInputs() {
        return requiredInputs;
    }

    public void setRequiredInputs(List<PluginInputFieldDTO> requiredInputs) {
        this.requiredInputs = requiredInputs;
    }	

    public void addRequiredInput(PluginInputFieldDTO requiredInput) {
        if(requiredInputs == null) {
            requiredInputs = new ArrayList<>();
        }
        this.requiredInputs.add(requiredInput);
    }
}
