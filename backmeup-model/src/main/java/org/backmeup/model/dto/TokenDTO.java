package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TokenDTO {
	private Long id;
	private String token;

	public TokenDTO() {

	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getTokenId() {
		return id;
	}

	public void setTokenId(Long tokenId) {
		this.id = tokenId;
	}
}
