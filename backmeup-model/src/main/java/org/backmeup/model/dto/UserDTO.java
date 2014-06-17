package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@SuppressWarnings("unused")
public class UserDTO {
    private Long userId;
    private String firstname;
    private String name;
    private String password;
    private String email;
    private boolean activated;
    
    public UserDTO() {
    	
    }

    public UserDTO(String firstname, String name, String password, String email) {
    	this.firstname = firstname;
    	this.name = name;
    	this.password = password;
    	this.email = email;
    	this.activated = false;
    }

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Long getUserId() {
		return userId;
	}

	private void setUserId(Long userId) {
		this.userId = userId;
	}

	public boolean isActivated() {
		return activated;
	}

	private void setActivated(Boolean activated) {
		this.activated = activated;
	}
}
