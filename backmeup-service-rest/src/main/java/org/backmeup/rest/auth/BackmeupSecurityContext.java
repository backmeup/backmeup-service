package org.backmeup.rest.auth;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class BackmeupSecurityContext implements SecurityContext{
    /**
     * String identifier for Token authentication. Value "TOKEN"
     */
    public static final String AUTH_TOKEN = "TOKEN";
    
    private final BackmeupPrincipal principal;

    public BackmeupSecurityContext(String entityId, Object entity, String authToken) {
        this(new BackmeupPrincipal(entityId, entity, authToken));
    }
    
    public BackmeupSecurityContext(BackmeupPrincipal principal) {
        this.principal = principal;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
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
        return AUTH_TOKEN;
    }

}
