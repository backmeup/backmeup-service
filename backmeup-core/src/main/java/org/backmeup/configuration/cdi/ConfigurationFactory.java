package org.backmeup.configuration.cdi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationFactory.class);
    private static final String MANDATORY_KEY_MISSING ="No definition found for mandatory configuration property '{0}'";
    private static final String PROPERTIES_FILE_NAME = "backmeup.properties";
    private static final String PROPERTIES_FILE_PATH = "config/"+ PROPERTIES_FILE_NAME;

    private static volatile Properties properties;

    public synchronized static Properties getProperties() {
        if (properties == null) {
            LOGGER.debug("Locate configuration properties file");

            properties = new Properties();
            try (InputStream is = propertyFileStream()) {
                properties.load(is);
            } catch (IOException e) {
                LOGGER.error("Failed to load properties file. Add {} or add it to the classpath!", PROPERTIES_FILE_PATH);
                throw new RuntimeException("Failed to load properties file", e);
            } 
        }
        return properties;
    }

    private static InputStream propertyFileStream() {
        InputStream is = ConfigurationFactory.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME);

        if (is == null) {
            LOGGER.debug("No properties file ({}) in classpath found",PROPERTIES_FILE_NAME);

            try {
                is = new FileInputStream(new File(PROPERTIES_FILE_PATH));
            } catch (FileNotFoundException e) {
                LOGGER.debug("No properties file found at path: {}",PROPERTIES_FILE_PATH);
                LOGGER.error("", e);
            } 
        }

        if (is == null) {
            LOGGER.error("No properties file found. Add {} or add it to the classpath!", PROPERTIES_FILE_PATH);
            throw new RuntimeException("No properties file found");
        }
        return is;
    }

    @Produces
    @Configuration
    public String getConfiguration(InjectionPoint ip){
        Configuration param = ip.getAnnotated().getAnnotation(Configuration.class);
        if(param.key() == null || param.key().length() == 0){
            LOGGER.debug("Configuration parameter null or empty, returning default value");
            return param.defaultValue();
        }

        Properties config = getProperties();
        String value = config.getProperty(param.key());

        if(value == null){
            LOGGER.debug("No definition found for config parameter '{}'", param.key());
            if(param.mandatory()){
                throw new IllegalStateException(MessageFormat.format(MANDATORY_KEY_MISSING, new Object[]{param.key()}));
            }
            LOGGER.debug("Returning default value for mandatory config parameter '{}'", param.key());
            return param.defaultValue();
        }
        LOGGER.info("Configuration: key='{}' value='{}'", param.key(), value);
        return value;
    }

    @Produces
    @Configuration
    public Integer getConfigurationInt(InjectionPoint ip) {
        String value = getConfiguration(ip);
        return Integer.parseInt(value);
    }

    @Produces
    @Configuration
    public Double getConfigurationDouble(InjectionPoint ip) {
        String value = getConfiguration(ip);
        return Double.parseDouble(value);
    }

    @Produces
    @Configuration
    public Boolean getConfigurationBoolean(InjectionPoint ip) {
        String value = getConfiguration(ip);
        return Boolean.parseBoolean(value);
    }
}
