package org.backmeup.model.dto;

import org.backmeup.model.BackMeUpUser;

public class UserDTO {
    private Long userId;
    private String username;
    private String email;

    public UserDTO() {
    }

    public UserDTO(BackMeUpUser user) {
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
