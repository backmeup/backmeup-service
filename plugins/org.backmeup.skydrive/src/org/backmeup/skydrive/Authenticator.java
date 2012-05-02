package org.backmeup.skydrive;

import java.util.Properties;

import org.backmeup.plugin.spi.OAuthBased;
import org.backmeup.skydrive.internal.SkyDriveSupport;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LiveApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class Authenticator implements OAuthBased {
	private String consumerKey;
	private String consumerSecret;
	
	public Authenticator() {
		SkyDriveHelper helper = SkyDriveHelper.getInstance();
		consumerKey = helper.getAppKey();
		consumerSecret = helper.getAppSecret();
	}
	
	private OAuthService buildService(String callback) {
		return new ServiceBuilder().provider(LiveApi.class)
				.apiKey(consumerKey).apiSecret(consumerSecret)
				.scope("wl.skydrive wl.skydrive_update wl.offline_access")
				.callback(callback)
				.build();
	}

	@Override
	public String createRedirectURL(Properties inputProperties, String callback) {
		OAuthService service = buildService(callback);
		return service.getAuthorizationUrl(null);
	}

	@Override
	public void postAuthorize(Properties inputProperties) {
		SkyDriveHelper helper = SkyDriveHelper.getInstance();
		consumerKey = helper.getAppKey();
		consumerSecret = helper.getAppSecret();		
		OAuthService service = buildService(inputProperties.getProperty("callback"));
		String code = inputProperties.getProperty("code");
		Verifier verifier = new Verifier(code);		
		Token accessToken = service.getAccessToken(null, verifier);
		inputProperties.setProperty(SkyDriveSupport.CONSUMER_KEY, consumerKey );
		inputProperties.setProperty(SkyDriveSupport.CONSUMER_SECRET, consumerSecret );
		inputProperties.setProperty(SkyDriveSupport.ACCESS_TOKEN, accessToken.getToken());
		// 6.) The refresh token is an additional parameter within the response.
		//     Parse it by using the JSON-Library.
		Token refreshToken = SkyDriveSupport.parseRefreshToken(accessToken.getRawResponse());
		inputProperties.setProperty(SkyDriveSupport.REFRESH_TOKEN, refreshToken.getToken());
	}

	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.OAuth;
	}
}
