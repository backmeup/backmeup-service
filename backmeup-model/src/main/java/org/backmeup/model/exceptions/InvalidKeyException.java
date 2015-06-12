package org.backmeup.model.exceptions;

/**
 * If a plugins authorization/authentication fails
 * because one of its keys are invalid,
 * this Exception will be thrown.
 * 
 * @author fschoeppl
 *
 */
public class InvalidKeyException extends PluginException {
    private static final long serialVersionUID = 1L;
    private final String plugin;
    private final String keyType;
    private final String value;
    private final String configFile;

    public InvalidKeyException(String plugin, String keyType, String value, String configFile) {    
        super(plugin, "Invalid app key / secret!");
        this.plugin = plugin;
        this.keyType = keyType;
        this.value = value;
        this.configFile = configFile;
    }

    public String getPlugin() {
        return plugin;
    }

    public String getKeyType() {
        return keyType;
    }

    public String getValue() {
        return value;
    }

    public String getConfigFile() {
        return configFile;
    }
}
