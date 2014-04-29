package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.BackMeUpUser;

@XmlRootElement
public class User {
    private Long userId;
    private String username;
    private String email;

    public User() {
    }
    
    public User(Long userId, String username, String email) {
    	this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public User(BackMeUpUser user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public String toString() {
    	return String.format("User: %s, %s (%s)", userId, username, email);
    }
}
