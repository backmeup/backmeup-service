package org.backmeup.logic.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.logic.AuthorizationLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.PasswordTooShortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
@ApplicationScoped
public class AuthorizationImpl implements AuthorizationLogic {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @Configuration(key = "backmeup.minimalPasswordLength")
    private Integer minimalPasswordLength;

    @Inject
    private KeyserverClient keyserverClient;

    @Override
    public void register(BackMeUpUser user) {
        throwIfPasswordInvalid(user.getPassword());
        try {
            keyserverClient.registerUser(user.getUserId().toString(), user.getPassword());
        } catch (KeyserverException e) {
            throw new BackMeUpException("Cannot register user", e);
        }
    }

    private void throwIfPasswordInvalid(String password) {
        if (password == null || password.length() < minimalPasswordLength) {
            throw new PasswordTooShortException(minimalPasswordLength, password == null ? 0 : password.length());
        }
    }

    @Override
    public void unregister(BackMeUpUser user) {
        try {
            keyserverClient.removeUser(null);
        } catch (Exception ex) {
            logger.warn(MessageFormat.format("Couldn't delete user \"{0}\" from keyserver", user.getUsername()), ex);
        }
    }

    @Override
    public void authorize(BackMeUpUser user, String password) {
        try {
            keyserverClient.authenticateUserWithPassword(user.getUsername(), password);
        } catch (KeyserverException ex) {
            throw new InvalidCredentialsException();
        }
    }

    @Override
    public Map<String, String> getProfileAuthInformation(Profile profile, String keyRing) {
        if (keyRing == null) {
            return new HashMap<>();
        }

        return fetchProfileAuthenticationData(profile, keyRing);
    }

    @Override
    public void overwriteProfileAuthInformation(Profile profile, Map<String, String> entries, String keyRing) {
        Properties props = new Properties();
        props.putAll(entries);
//        keyserverClient.addAuthInfo(profile, keyRing, props);
    }

    @Override
    public Map<String, String> fetchProfileAuthenticationData(Profile profile, String keyRingPassword) {
        String authData = getAuthDataFor(profile, keyRingPassword);
        Map<String, String> authProps = convertToMap(authData);
        return authProps;
    }

    private String getAuthDataFor(Profile profile, String password) {        
        try {
            return keyserverClient.getPluginData(null, profile.getPluginId());
        } catch (KeyserverException e) {
            throw new BackMeUpException("Cannot get plugin data", e);
        }
    }
    
    public static Map<String, String> convertToMap(String data) {
        String[] tokens = data.split(";|=");
        Map<String, String> map = new HashMap<>();
        for (int i=0; i<tokens.length-1; ) map.put(tokens[i++], tokens[i++]);
        return map;
    }
}
