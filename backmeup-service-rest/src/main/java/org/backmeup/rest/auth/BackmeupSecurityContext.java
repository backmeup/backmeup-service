package org.backmeup.rest.auth;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import org.backmeup.model.BackMeUpUser;

public class BackmeupSecurityContext implements SecurityContext{
    private final BackmeupPrincipal user;

    public BackmeupSecurityContext(BackMeUpUser user) {
        this.user = new BackmeupPrincipal(user.getUserId().toString(), user);
    }

    @Override
    public Principal getUserPrincipal() {
        return user;
    }

    @Override
    public boolean isUserInRole(String role) {
        return true;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return null;
    }

}
