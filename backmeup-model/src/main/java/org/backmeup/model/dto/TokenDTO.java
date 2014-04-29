package org.backmeup.model.dto;

import org.backmeup.model.Token;

public class TokenDTO {
	private Long id;
	private String token;
	private Long backupdate;

	public TokenDTO(Token token) {
		this.token = token.getToken();
		this.id = token.getTokenId();
	}

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

	public Long getBackupdate() {
		return backupdate;
	}

	public void setBackupdate(Long backupdate) {
		this.backupdate = backupdate;
	}
}
