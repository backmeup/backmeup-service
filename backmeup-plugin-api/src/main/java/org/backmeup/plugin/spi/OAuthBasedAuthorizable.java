package org.backmeup.plugin.spi;

import java.util.Properties;

public interface OAuthBasedAuthorizable extends Authorizable {

    public static final String QUERY_PARAM_PROPERTY = "oAuthQuery";
    
	public String createRedirectURL(Properties inputProperties, String callbackUrl);

}
