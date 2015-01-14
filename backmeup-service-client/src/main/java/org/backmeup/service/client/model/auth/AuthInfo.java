package org.backmeup.service.client.model.auth;

import java.util.Date;

public class AuthInfo {
	private String accessToken;
	private Date issueDate;

	public AuthInfo() {

	}

	public AuthInfo(String accessToken, Date issueDate) {
		super();
		this.accessToken = accessToken;
		this.issueDate = issueDate;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}
}
