package org.backmeup.rest.auth;

import java.security.Principal;

import org.backmeup.model.BackMeUpUser;

public class BackmeupPrincipal implements Principal {
    private String userId;
    private final BackMeUpUser user;

    public BackmeupPrincipal(String userId, BackMeUpUser user) {
        super();
        this.userId = userId;
        this.user = user;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BackMeUpUser getUser() {
        return user;
    }

    @Override
    public String getName() {
        return userId;
    }
}
