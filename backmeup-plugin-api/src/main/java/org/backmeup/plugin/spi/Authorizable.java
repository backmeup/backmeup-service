package org.backmeup.plugin.spi;

import java.util.Map;


public interface Authorizable {
	
	public enum AuthorizationType {
		OAuth,
		InputBased
	}
	
	public AuthorizationType getAuthType(); 
	
	// updates the authorization data and returns the identification of the account
	public String authorize(Map<String, String> authData);
		
}
