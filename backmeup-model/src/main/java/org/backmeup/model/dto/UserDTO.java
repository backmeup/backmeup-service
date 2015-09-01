package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@SuppressWarnings("unused")
public class UserDTO {
    private Long userId;
    private String username;
    private String firstname;
    private String lastname;
    private String password;
    private String email;
    private boolean activated;
    private boolean anonymous;

    public UserDTO() {

    }

    public UserDTO(String username, String firstname, String lastname, String password, String email) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.email = email;
        this.activated = false;
        this.anonymous = false;
    }

    public Long getUserId() {
        return userId;
    }

    private void setUserId(Long userId) {
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

    public void setLastname(String name) {
        this.lastname = name;
    }

    public String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActivated() {
        return activated;
    }

    private void setActivated(Boolean activated) {
        this.activated = activated;
    }

	public boolean isAnonymous() {
		return anonymous;
	}
    
}
