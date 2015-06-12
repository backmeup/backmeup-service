package org.backmeup.rest.auth;

import java.util.Date;

public class AuthInfo {
    private String accessToken;
    private Date expiresAt;

    public AuthInfo() {

    }

    public AuthInfo(String accessToken, Date expiresAt) {
        super();
        this.accessToken = accessToken;
        this.expiresAt = (Date) expiresAt.clone();
    }

    public AuthInfo(String accessToken, long expiresAt) {
        super();
        this.accessToken = accessToken;
        this.expiresAt = new Date(expiresAt);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Date getExpiresAt() {
        if (this.expiresAt == null) {
            return null;
        }
        return (Date) expiresAt.clone();
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = (Date) expiresAt.clone();
    }
}
