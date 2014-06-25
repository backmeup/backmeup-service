package org.backmeup.logic.impl;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.logic.AuthorizationLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.KeyserverLog;
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

    /*
    @Override
    public void updatePasswords(BackMeUpUser user, String oldPassword, String newPassword, String oldKeyRing, String newKeyRing) {
        if (newPassword != null) {
            throwIfPasswordInvalid(newPassword);
            keyserverClient.changeUserPassword(user.getUserId(), oldPassword, newPassword);
        }

        // TODO Remove keyring from change user options
        if (newKeyRing != null && oldKeyRing != null && !oldKeyRing.equals(newKeyRing)) {
            throwIfPasswordInvalid(newKeyRing);
            keyserverClient.changeUserKeyRing(user.getUserId(), oldKeyRing, newKeyRing);
        }
    }
    */

    @Override
    public Properties getProfileAuthInformation(Profile profile, String keyRing) {
        if (keyRing == null) {
            return new Properties();
        }
        if (!keyserverClient.isAuthInformationAvailable(profile, keyRing)) {
            return new Properties();
        }

        return fetchFirstAuthenticationData(profile, keyRing);
    }

    @Override
    public void initProfileAuthInformation(Profile profile, Properties entries, String keyRing) {
        if (!keyserverClient.isServiceRegistered(profile.getProfileId())) {
            keyserverClient.addService(profile.getProfileId());
        }

        keyserverClient.addAuthInfo(profile, keyRing, entries);
    }

    @Override
    public void overwriteProfileAuthInformation(Profile profile, Properties entries, String keyRing) {
        if (keyserverClient.isAuthInformationAvailable(profile, keyRing)) {
            keyserverClient.deleteAuthInfo(profile.getProfileId());
        }

        keyserverClient.addAuthInfo(profile, keyRing, entries);
    }

    @Override
    public void appendProfileAuthInformation(Profile profile, Properties entries, String keyRing) {
        Properties props = getProfileAuthInformation(profile, keyRing);
        props.putAll(entries);

        overwriteProfileAuthInformation(profile, props, keyRing);
    }

    private Properties fetchFirstAuthenticationData(Profile profile, String password) {
        AuthDataResult authData = getAuthDataFor(profile, password);

        Properties props = new Properties();
        if (authData.getAuthinfos().length > 0) {
            props.putAll(authData.getAuthinfos()[0].getAi_data());
        }

        return props;
    }

    @Override
    public Properties fetchProfileAuthenticationData(Profile profile, String keyRingPassword) {
        AuthDataResult authData = getAuthDataFor(profile, keyRingPassword);
        return authData.getByProfileId(profile.getProfileId());
    }

    private AuthDataResult getAuthDataFor(Profile profile, String password) {
        long now = new Date().getTime();
        boolean reusable = false;
        Token token = keyserverClient.getToken(profile, password, now, reusable, null);

        return keyserverClient.getData(token);
    }

    @Override
    public List<KeyserverLog> getLogs(BackMeUpUser user) {
        return keyserverClient.getLogs(user);
    }

}
