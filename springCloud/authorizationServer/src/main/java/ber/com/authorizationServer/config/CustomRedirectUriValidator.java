package ber.com.authorizationServer.config;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

public class CustomRedirectUriValidator implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {
	
	private static Logger LOG = LoggerFactory.getLogger(AuthorizationServerConfig.class);

	@Override
	public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext authenticationContext) {
		OAuth2AuthorizationCodeRequestAuthenticationToken auth2AuthorizationCodeRequestAuthenticationToken
			= authenticationContext.getAuthentication();
		RegisteredClient registeredClient = authenticationContext.getRegisteredClient();
		String requestRedirectUri = auth2AuthorizationCodeRequestAuthenticationToken.getRedirectUri();
		
		LOG.trace("Will validate the redirect uri {}", requestRedirectUri);
		
		if(!registeredClient.getRedirectUris().contains(requestRedirectUri)) {
			LOG.trace("Redirect uri is invalid!");
			OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);
			throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, null);
			
		}
		LOG.trace("Redirect uri is OK!");
		
	}

	
}
