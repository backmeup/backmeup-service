package org.backmeup.plugin.spi;

import java.util.Map;

public interface OAuthBasedAuthorizable extends Authorizable {

    public static final String QUERY_PARAM_PROPERTY = "oAuthQuery";
    
	public String createRedirectURL(Map<String, String> authData, String callbackUrl);

}
