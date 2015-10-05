package org.backmeup.rest.auth;

import java.security.Principal;

import org.backmeup.keyserver.model.Token.Kind;
import org.backmeup.keyserver.model.dto.TokenDTO;

public class BackmeupPrincipal implements Principal {
    private String entityId;
    private Object entity;
    private TokenDTO authToken;
    private String userId;

    public BackmeupPrincipal(String entityId, Object entity, String authToken) {
        this(entityId, entity, new TokenDTO(Kind.INTERNAL, authToken));
    }
    
    public BackmeupPrincipal(String entityId, Object entity, TokenDTO authToken) {
        super();
        this.entityId = entityId;
        this.entity = entity;
        this.authToken = authToken;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Object getEntity() {
        return entity;
    }
    
    public <T> T getEntity(Class<T> type) {
        return type.cast(entity);
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public TokenDTO getAuthToken() {
        return authToken;
    }

    public void setAuthToken(TokenDTO authToken) {
        this.authToken = authToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return entityId;
    }
}
