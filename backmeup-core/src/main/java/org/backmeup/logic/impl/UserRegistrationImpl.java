package org.backmeup.logic.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.internet.MimeUtility;

import org.apache.commons.codec.binary.Base64;
import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.UserDao;
import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.keyserver.model.Token.Kind;
import org.backmeup.keyserver.model.dto.AuthResponseDTO;
import org.backmeup.keyserver.model.dto.TokenDTO;
import org.backmeup.logic.UserRegistration;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Token;
import org.backmeup.model.exceptions.AlreadyRegisteredException;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.EmailVerificationException;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.NotAnEmailAddressException;
import org.backmeup.model.exceptions.PasswordTooShortException;
import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.utilities.mail.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User registration related business logic.
 */
@ApplicationScoped
public class UserRegistrationImpl implements UserRegistration {

    private static final String EXCEPTION_TEXT_REGISTRATION = "Cannot register user";
    private static final String PARAMETER_NULL = "org.backmeup.logic.impl.BusinessLogicImpl.PARAMETER_NULL";
    private static final String VERIFICATION_EMAIL_SUBJECT = "org.backmeup.logic.impl.BusinessLogicImpl.VERIFICATION_EMAIL_SUBJECT";
    private static final String VERIFICATION_EMAIL_CONTENT = "org.backmeup.logic.impl.BusinessLogicImpl.VERIFICATION_EMAIL_CONTENT";
    private static final String VERIFICATION_EMAIL_MIME_TYPE = "org.backmeup.logic.impl.BusinessLogicImpl.VERIFICATION_EMAIL_MIME_TYPE";
    
    private static final String CHARSET_NAME_UTF8 = "UTF-8";

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationImpl.class);
    
    private final ResourceBundle textBundle = ResourceBundle.getBundle("UserRegistrationImpl");

    @Inject
    @Configuration(key = "backmeup.autoVerifyUser")
    private Boolean autoVerifyUser;

    @Inject
    @Configuration(key = "backmeup.emailRegex")
    private String emailRegex;

    @Inject
    @Configuration(key = "backmeup.emailVerificationUrl")
    private String verificationUrl;
    
    @Inject
    @Configuration(key = "backmeup.minimalPasswordLength")
    private Integer minimalPasswordLength;

    @Inject
    private KeyserverClient keyserverClient;

    @Inject
    private DataAccessLayer dal;

    private UserDao getUserDao() {
        return dal.createUserDao();
    }

    private BackMeUpUser save(BackMeUpUser user) {
        return getUserDao().save(user);
    }

    @Override
    public BackMeUpUser getUserByUserId(Long userId) {
        return getUserByUserId(userId, false);
    }

    @Override
    public BackMeUpUser getUserByUserId(Long userId, boolean ensureActivated) {
        BackMeUpUser user = getUserDao().findById(userId);
        if (user == null) {
            throw new UnknownUserException(userId);
        }

        if(ensureActivated) {
            user.ensureActivated();
        }
        return user;
    }
    
    @Override
    public BackMeUpUser getUserByKeyserverUserId(String keyserverUserId) {
        BackMeUpUser user = getUserDao().findByKeyserverId(keyserverUserId);
        if (user == null) {
            throw new UnknownUserException(keyserverUserId);
        }

        return user;
    }

    @Override
    public BackMeUpUser getUserByUsername(String username, boolean ensureActivated) {
        BackMeUpUser user = getUserDao().findByName(username);
        if (user == null) {
            throw new UnknownUserException(username);
        }

        if(ensureActivated) {
            user.ensureActivated();
        }
        return user;
    }

    @Override
    public BackMeUpUser register(BackMeUpUser user) {
        if (user.getUsername() == null || user.getEmail() == null || user.getPassword() == null) {
            throw new IllegalArgumentException(textBundle.getString(PARAMETER_NULL));
        }
        throwIfEmailInvalid(user.getEmail());
        throwIfPasswordInvalid(user.getPassword());

        ensureUsernameAvailable(user.getUsername());
        ensureEmailAvailable(user.getEmail());

        setNewVerificationKeyTo(user);

        try {
            BackMeUpUser newUser = save(user);
            newUser.setPassword(user.getPassword());
            
            String serviceUserId = keyserverClient.registerUser(newUser.getUserId().toString(), newUser.getPassword());
            newUser.setKeyserverId(serviceUserId);
            
            save(newUser);
            return newUser;
        } catch (KeyserverException e) {
            throw new BackMeUpException(EXCEPTION_TEXT_REGISTRATION, e);
        }
    }
    
    @Override
    public BackMeUpUser registerAnonymous(BackMeUpUser currentUser) {
        try {
            TokenDTO token = new TokenDTO(Kind.INTERNAL, currentUser.getPassword());
            AuthResponseDTO response = keyserverClient.registerAnonymousUser(token);
            String anonServiceUserId = response.getServiceUserId();
            
            String uuid = UUID.randomUUID().toString();
            BackMeUpUser anonUser = new BackMeUpUser(uuid, null, null, uuid + "@backmeup", null);
            anonUser.setAnonymous(true);
            anonUser.setActivated(true);
            anonUser.setKeyserverId(anonServiceUserId);
            BackMeUpUser newUser = save(anonUser);
            
            return newUser;
        } catch (KeyserverException e) {
            throw new BackMeUpException(EXCEPTION_TEXT_REGISTRATION, e);
        }
    }

    private void throwIfEmailInvalid(String email) {
        Pattern emailPattern = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher emailMatcher = emailPattern.matcher(email);
        if (!emailMatcher.matches()) {
            throw new NotAnEmailAddressException(emailRegex, email);
        }
    }
    
    private void throwIfPasswordInvalid(String password) {
        if (password == null || password.length() < minimalPasswordLength) {
            throw new PasswordTooShortException(minimalPasswordLength, password == null ? 0 : password.length());
        }
    }

    private void ensureUsernameAvailable(String username) {
        BackMeUpUser existingUser = getUserDao().findByName(username);
        if (existingUser != null) {
            throw new AlreadyRegisteredException(existingUser.getUsername());
        }
    }

    private void ensureEmailAvailable(String email) {
        BackMeUpUser existingUser = getUserDao().findByEmail(email);
        if (existingUser != null) {
            throw new AlreadyRegisteredException(existingUser.getEmail());
        }
    }

    @Override
    public void setNewVerificationKeyTo(BackMeUpUser user) {
        String timeStamp = Long.toString(new Date().getTime());
        generateNewVerificationKey(user, timeStamp);
    }

    private void generateNewVerificationKey(BackMeUpUser user, String additionalPart) {
        String payLoad = user.getUsername() + "." + additionalPart;
        String verificationKey = createKey(payLoad);
        user.setVerificationKey(verificationKey);
    }

    private String createKey(String tostore) {
        try {
            // http://stackoverflow.com/questions/4871094/generate-activation-urls-in-java-ee-6
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(tostore.getBytes(CHARSET_NAME_UTF8));
            String base64 = Base64.encodeBase64String(digest.digest());
            return base64.replaceAll("/", "_").replaceAll("\\+", "D").replaceAll("=", "A").trim();

        } catch (NoSuchAlgorithmException e) {
            throw new BackMeUpException(e);
        } catch (UnsupportedEncodingException e) {
            throw new BackMeUpException(e);
        }
    }

    @Override
    public void sendVerificationEmailFor(BackMeUpUser user) {
        String verifierUrl = String.format(verificationUrl, user.getVerificationKey());
        String subject = "";
        try {
            subject = new String(textBundle.getString(VERIFICATION_EMAIL_SUBJECT).getBytes("ISO-8859-1"), CHARSET_NAME_UTF8);
            subject = MimeUtility.encodeText(subject, CHARSET_NAME_UTF8, "Q");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Something went wrong in sendVerificationEmailFor", e);
        }
        String text = MessageFormat.format(textBundle.getString(VERIFICATION_EMAIL_CONTENT), verifierUrl, user.getVerificationKey());
        String mimeType = textBundle.getString(VERIFICATION_EMAIL_MIME_TYPE);
        Mailer.send(user.getEmail(), subject, text, mimeType);
    }

    @Override
    public BackMeUpUser requestNewVerificationEmail(String username) {
        BackMeUpUser user = getUserByUsername(username, false);
        user.ensureNotActivated();

        setNewVerificationKeyTo(user);
        save(user);

        sendVerificationEmailFor(user);

        return user;
    }

    @Override
    public BackMeUpUser activateUserFor(String verificationKey) {
        BackMeUpUser user = getUserDao().findByVerificationKey(verificationKey);
        if (user == null) {
            throw new EmailVerificationException(verificationKey);
        }
        user.ensureNotActivated();

        // Note: Don't delete the verification key (e.g. set to null). 
        // If the user tries an second verification it should work.
        user.setActivated(true);
        save(user);

        return user;
    }

    @Override
    public void ensureNewValuesAvailable(BackMeUpUser user, String newUsername, String newEmail) {
        if (newUsername != null && !user.getUsername().equals(newUsername)) {
            ensureUsernameAvailable(newUsername);
        }

        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            throwIfEmailInvalid(newEmail);
        }
    }

    @Override
    public BackMeUpUser update(BackMeUpUser user) {
        BackMeUpUser persistentUser = getUserByUsername(user.getUsername(), true);

        if (user.getFirstname() != null && !user.getFirstname().equals(persistentUser.getFirstname())){
            persistentUser.setFirstname(user.getFirstname());
        }

        if (user.getLastname() != null && !user.getLastname().equals(persistentUser.getLastname())){
            persistentUser.setLastname(user.getLastname());
        }

        if (user.getEmail() != null && !user.getEmail().equals(persistentUser.getEmail())) {
            persistentUser.setEmail(user.getEmail());
            if (!autoVerifyUser) {
                persistentUser.setActivated(false);
                setNewVerificationKeyTo(user);
                sendVerificationEmailFor(user);
            }
        }

        return save(persistentUser);
    }

    @Override
    public void delete(BackMeUpUser user) {
        try {
            TokenDTO token = new TokenDTO(Kind.INTERNAL, user.getPassword());
            keyserverClient.removeUser(token);
            getUserDao().delete(user);
        } catch (Exception ex) {
            LOGGER.warn(MessageFormat.format("Couldn't delete user \"{0}\"", user.getUsername()), ex);
        }
    }
    
    @Override
    public Token authorize(BackMeUpUser user, String password) {
        try {
            AuthResponseDTO response = keyserverClient.authenticateUserWithPassword(user.getUserId().toString(), password);
            String token = response.getToken().getB64Token();
            Date ttl = response.getToken().getTtl().getTime();
            return new Token(token, ttl.getTime());
        } catch (KeyserverException ex) {
            LOGGER.warn("Cannot authenticate on keyserver", ex);
            throw new InvalidCredentialsException();
        }
    }
    
    @Override
    public Token authorize(String activationCode) {
        try {
        	AuthResponseDTO response = keyserverClient.authenticateWithInternalToken(new TokenDTO(Kind.INTERNAL, activationCode));
            String token = response.getToken().getB64Token();
            Date ttl = response.getToken().getTtl().getTime();
            return new Token(token, ttl.getTime());
        } catch (KeyserverException ex) {
            LOGGER.warn("Cannot authenticate on keyserver", ex);
            throw new InvalidCredentialsException();
        }
    }

    @Override
    public String getActivationCode(BackMeUpUser currentUser, BackMeUpUser anonUser) {
        try {
            TokenDTO token = new TokenDTO(Kind.INTERNAL, currentUser.getPassword());
            TokenDTO t = keyserverClient.getInheritanceToken(token, anonUser.getKeyserverId());
            String activationCode = t.getB64Token();
            return activationCode;
        } catch (KeyserverException e) {
            throw new BackMeUpException(EXCEPTION_TEXT_REGISTRATION, e);
        }
    }
}
