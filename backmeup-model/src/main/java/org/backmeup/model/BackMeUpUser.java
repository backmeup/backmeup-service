package org.backmeup.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.backmeup.model.exceptions.UserAlreadyActivatedException;
import org.backmeup.model.exceptions.UserNotActivatedException;

/**
 * The User class represents a user of backmeup.
 * 
 * @author fschoeppl
 */
@Entity
public class BackMeUpUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;

    private String firstname;

    private String lastname;

    @Column(unique = true, nullable = false)
    private String email;

    @Transient
    private String password;

    private boolean activated;

    private String verificationKey;
    
    private boolean anonymous;
    private String keyserverId;

    public BackMeUpUser() {

    }

    public BackMeUpUser(String username, String firstname, String lastname,
            String email, String password) {
        this(null, username, firstname, lastname, email, password);
    }

    public BackMeUpUser(Long userId, String username, String firstname,
            String lastname, String email, String password) {
        this.userId = userId;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.activated = false;
        this.anonymous = false;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getVerificationKey() {
        return verificationKey;
    }

    public void setVerificationKey(String verificationKey) {
        this.verificationKey = verificationKey;
    }

    public void ensureActivated() {
        if (!isActivated()) {
            throw new UserNotActivatedException(getUsername());
        }
    }

    public void ensureNotActivated() {
        if (isActivated()) {
            throw new UserAlreadyActivatedException(getUsername());
        }
    }

    public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	public String getKeyserverId() {
		return keyserverId;
	}

	public void setKeyserverId(String keyserverId) {
		this.keyserverId = keyserverId;
	}

	@Override
    public String toString() {
        return username;
    }
}
