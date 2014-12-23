package org.backmeup.plugin.spi;

import java.util.Properties;

public interface OAuthBasedAuthorizable extends Authorizable {

	public String createRedirectURL(Properties inputProperties, String callbackUrl);

}
