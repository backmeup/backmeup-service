package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TokenDTO {
    private Long id;
    private String token;
    private Long validity;

    public TokenDTO() {

    }

    public TokenDTO(Long id, String token) {
        this(id, token, null);
    }

    public TokenDTO(Long id, String token, Long validity) {
        this.id = id;
        this.token = token;
        this.validity = validity;
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

    public Long getValidity() {
        return validity;
    }

    public void setValidity(Long validity) {
        this.validity = validity;
    }
}
