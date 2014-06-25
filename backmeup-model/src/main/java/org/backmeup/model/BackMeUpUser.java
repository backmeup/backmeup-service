package org.backmeup.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "user")
    private final List<JobProtocol> protocols = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private final Set<UserProperty> properties = new HashSet<>();
    
    public BackMeUpUser() {
    	
    }

    public BackMeUpUser(String username, String firstname, String lastname, String email, String password) {
        this(null, username, firstname, lastname, email, password);
    }

    public BackMeUpUser(Long userId, String username, String firstname, String lastname, String email, String password) {
        this.userId = userId;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.activated = false;
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
 
    public Set<UserProperty> getUserProperties() {
        return properties;
    }
    
    public String getUserProperty(String key) {
        UserProperty up = findProperty(key);
        if (up == null) {
            return null;
        }
        return up.getValue();
    }

    public void setUserProperty(String key, String value) {
        UserProperty up = findProperty(key);
        if (up == null) {
            up = new UserProperty(key, value);
            this.getUserProperties().add(up);
        } else {
            up.setValue(value);
        }
    }

    public void deleteUserProperty(String key) {
        UserProperty up = new UserProperty(key, null);
        this.getUserProperties().remove(up);
    }
    
    private UserProperty findProperty(String key) {
        for (UserProperty up : getUserProperties()) {
            if (up.getKey().equals(key)) {
                return up;
            }
        }
        return null;
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

    @Override
    public String toString() {
        return username;
    }
}
