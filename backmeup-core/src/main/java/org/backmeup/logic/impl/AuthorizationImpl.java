package org.backmeup.logic.impl;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.logic.AuthorizationLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;
import org.backmeup.model.Token;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.PasswordTooShortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AuthorizationImpl implements AuthorizationLogic {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @Configuration(key = "backmeup.minimalPasswordLength")
    private Integer minimalPasswordLength;

    @Inject
    private Keyserver keyserverClient;

    @Override
    public void register(BackMeUpUser user) {
        throwIfPasswordInvalid(user.getPassword());
        keyserverClient.registerUser(user.getUserId(), user.getPassword());
    }

    private void throwIfPasswordInvalid(String password) {
        if (password == null || password.length() < minimalPasswordLength) {
            throw new PasswordTooShortException(minimalPasswordLength, password == null ? 0 : password.length());
        }
    }

    @Override
    public void unregister(BackMeUpUser user) {
        try {
            keyserverClient.deleteUser(user.getUserId());
        } catch (Exception ex) {
            logger.warn(MessageFormat.format("Couldn't delete user \"{0}\" from keyserver", user.getUsername()), ex);
        }
    }

    @Override
    public void authorize(BackMeUpUser user, String password) {
        if (!keyserverClient.validateUser(user.getUserId(), password)) {
            throw new InvalidCredentialsException();
        }
    }

    @Override
    public Map<String, String> getProfileAuthInformation(Profile profile, String keyRing) {
        if (keyRing == null) {
            return new HashMap<>();
        }
        if (!keyserverClient.isAuthInformationAvailable(profile, keyRing)) {
            return new HashMap<>();
        }

        return fetchFirstAuthenticationData(profile, keyRing);
    }

    @Override
    public void overwriteProfileAuthInformation(Profile profile, Map<String, String> entries, String keyRing) {
        if (!keyserverClient.isServiceRegistered(profile.getId())) {
            keyserverClient.addService(profile.getId());
        }
        
        if (keyserverClient.isAuthInformationAvailable(profile, keyRing)) {
            keyserverClient.deleteAuthInfo(profile.getId());
        }

        Properties props = new Properties();
        props.putAll(entries);
        keyserverClient.addAuthInfo(profile, keyRing, props);
    }

    private Map<String, String> fetchFirstAuthenticationData(Profile profile, String password) {
        AuthDataResult authData = getAuthDataFor(profile, password);

        Map<String, String> props = new HashMap<>();
        if (authData.getAuthinfos().length > 0) {
            props.putAll(authData.getAuthinfos()[0].getAi_data());
        }

        return props;
    }

    @Override
    public Map<String, String> fetchProfileAuthenticationData(Profile profile, String keyRingPassword) {
        AuthDataResult authData = getAuthDataFor(profile, keyRingPassword);
        Properties entries = authData.getByProfileId(profile.getId());
        Map<String, String> authProps = new HashMap<>();
        for (final String key: entries.stringPropertyNames()) {
            authProps.put(key, entries.getProperty(key));
        }
        return authProps;
    }

    private AuthDataResult getAuthDataFor(Profile profile, String password) {
        long now = new Date().getTime();
        boolean reusable = false;
        Token token = keyserverClient.getToken(profile, password, now, reusable, null);

        return keyserverClient.getData(token);
    }

}
