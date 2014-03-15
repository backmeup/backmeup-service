package org.backmeup.logic.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.UserDao;
import org.backmeup.logic.UserRegistration;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.exceptions.AlreadyRegisteredException;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.EmailVerificationException;
import org.backmeup.model.exceptions.NotAnEmailAddressException;
import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.utilities.mail.Mailer;

/**
 * User registration related business logic.
 */
@ApplicationScoped
public class UserRegistrationImpl implements UserRegistration {

    private static final String PARAMETER_NULL = "org.backmeup.logic.impl.BusinessLogicImpl.PARAMETER_NULL";
    private static final String VERIFICATION_EMAIL_SUBJECT = "org.backmeup.logic.impl.BusinessLogicImpl.VERIFICATION_EMAIL_SUBJECT";
    private static final String VERIFICATION_EMAIL_CONTENT = "org.backmeup.logic.impl.BusinessLogicImpl.VERIFICATION_EMAIL_CONTENT";
    private static final String VERIFICATION_EMAIL_MIME_TYPE = "org.backmeup.logic.impl.BusinessLogicImpl.VERIFICATION_EMAIL_MIME_TYPE";

    private final ResourceBundle textBundle = ResourceBundle.getBundle(getClass().getSimpleName());

    @Inject
    @Configuration(key = "backmeup.emailRegex")
    private String emailRegex;

    @Inject
    @Configuration(key = "backmeup.emailVerificationUrl")
    private String verificationUrl;

    @Inject
    private DataAccessLayer dal;

    private UserDao getUserDao() {
        return dal.createUserDao();
    }

    private BackMeUpUser save(BackMeUpUser user) {
        return getUserDao().save(user);
    }

    @Override
    public BackMeUpUser queryExistingUser(String username) {
        BackMeUpUser user = getUserDao().findByName(username);
        if (user == null) {
            throw new UnknownUserException(username);
        }
        return user;
    }

    @Override
    public BackMeUpUser queryActivatedUser(String username) {
        BackMeUpUser user = queryExistingUser(username);
        user.ensureActivated();
        return user;
    }

    private BackMeUpUser queryNonActivatedUser(String username) {
        BackMeUpUser user = queryExistingUser(username);
        user.ensureNotActivated();
        return user;
    }

    @Override
    public BackMeUpUser register(String username, String email) {
        if (username == null || email == null) {
            throw new IllegalArgumentException(textBundle.getString(PARAMETER_NULL));
        }
        throwIfEmailInvalid(email);

        ensureUsernameAvailable(username);
        ensureEmailAvailable(email);

        BackMeUpUser newUser = new BackMeUpUser(username, email);
        newUser.setActivated(false);
        setNewVerificationKeyTo(newUser);

        newUser = save(newUser);
        return newUser;
    }

    private void throwIfEmailInvalid(String email) {
        Pattern emailPattern = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher emailMatcher = emailPattern.matcher(email);
        if (!emailMatcher.matches()) {
            throw new NotAnEmailAddressException(emailRegex, email);
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
            digest.update(tostore.getBytes("UTF-8"));
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
        String subject = textBundle.getString(VERIFICATION_EMAIL_SUBJECT);
        String text = MessageFormat.format(textBundle.getString(VERIFICATION_EMAIL_CONTENT), verifierUrl, user.getVerificationKey());
        String mimeType = textBundle.getString(VERIFICATION_EMAIL_MIME_TYPE);
        Mailer.send(user.getEmail(), subject, text, mimeType);
    }

    @Override
    public BackMeUpUser requestNewVerificationEmail(String username) {
        BackMeUpUser user = queryNonActivatedUser(username);

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

        // Don't delete the key. If the user tries an second verification it should work.
        // user.setVerificationKey(null);

        user.setActivated(true);
        save(user);

        return user;
    }

    @Override
    public void ensureNewValuesAvailable(BackMeUpUser user, String newUsername, String newEmail) {
        if (newUsername != null && !user.getUserId().equals(newUsername)) {
            ensureUsernameAvailable(newUsername);
        }

        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            throwIfEmailInvalid(newEmail);
        }
    }

    @Override
    public void updateValues(BackMeUpUser user, String newUsername, String newEmail) {
        if (newUsername != null && !user.getUsername().equals(newUsername)) {
            user.setUsername(newUsername);
        }

        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            user.setEmail(newEmail);

            // Decativate user. Send new activation Mail
            user.setActivated(false);
            setNewVerificationKeyTo(user);
            sendVerificationEmailFor(user);
        }

        save(user);
    }

}
