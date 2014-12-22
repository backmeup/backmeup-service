package org.backmeup.plugin.spi;

import java.util.Properties;

public interface OAuthBasedAuthorizable extends Authorizable {

    public static final String PROP_CALLBACK_URL = "callback";

	public String createRedirectURL(Properties inputProperties, String callbackUrl);

}
