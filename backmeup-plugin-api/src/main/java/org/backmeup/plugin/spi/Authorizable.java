package org.backmeup.plugin.spi;

import java.util.Map;


public interface Authorizable {

    public enum AuthorizationType {
        OAUTH,
        INPUTBASED
    }

    AuthorizationType getAuthType(); 

    // updates the authorization data and returns the identification of the account
    String authorize(Map<String, String> authData);

}
