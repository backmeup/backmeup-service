package org.backmeup.plugin.api;

import java.util.Map;

public interface OAuthBasedAuthorizable extends Authorizable {

    String createRedirectURL(Map<String, String> authData, String callbackUrl);

}
