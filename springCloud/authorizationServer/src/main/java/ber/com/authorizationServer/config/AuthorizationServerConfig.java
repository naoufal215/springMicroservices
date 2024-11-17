package ber.com.authorizationServer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationValidator;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import ber.com.authorizationServer.jose.Jwks;

import java.util.function.Consumer;
import java.util.List;

import java.util.UUID;
import java.time.Duration;


@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {
	
	private static Logger LOG = LoggerFactory.getLogger(AuthorizationServerConfig.class);
	
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer
		 = new OAuth2AuthorizationServerConfigurer();
		
		authorizationServerConfigurer.authorizationEndpoint(
				authorizationEndpoint -> 
					authorizationEndpoint.authenticationProviders(configureAuthenticationValidator())
				); 
		RequestMatcher endpointsMatcher =  authorizationServerConfigurer.getEndpointsMatcher();
		
		http.securityMatcher(endpointsMatcher)
			.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
			.csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
			.apply(authorizationServerConfigurer);
		
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
		.oidc(Customizer.withDefaults());
		
		http.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
		.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
		
		return http.build();
		
	}
	
	@Bean
	public RegisteredClientRepository registredClientRepository() {
		
		RegisteredClient writerClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("writer")
				.clientSecret("{noop}secret-writer")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.redirectUri("https://my.redirect.uri")
				.redirectUri("https://localhost:8443/openapi/webjars/swagger-ui/oauth2-redirect.html")
				.scope(OidcScopes.OPENID)
				.scope("product:read")
				.scope("product:write")
				.tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofHours(1)).build())
				.build();
		
		RegisteredClient readerClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("reader")
				.clientSecret("{noop}secret-reader")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.redirectUri("https://my.redirect.uri")
				.redirectUri("https://localhost:8443/openapi/webjars/swagger-ui/oauth2-redirect.html")
				.scope(OidcScopes.OPENID)
				.scope("product:read")
				.tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofHours(1)).build())
				.build();
		
		return new InMemoryRegisteredClientRepository(writerClient,readerClient);
		
	}
	
	
	
	
	
	
	
	@Bean
	public JWKSource<SecurityContext> jwkSource(){
		RSAKey rsaKey = Jwks.generateRsa();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}
	
	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}
	
	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().issuer("http://auth-server:9999").build();
	}
	
	private Consumer<List<AuthenticationProvider>> configureAuthenticationValidator(){
		return (authenticationProviders) -> 
			authenticationProviders.forEach(
					authenticationProvider ->
					{
						if(authenticationProvider instanceof OAuth2AuthorizationCodeRequestAuthenticationProvider) {
							Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> authenticationValidator
							= new CustomRedirectUriValidator()
								.andThen(OAuth2AuthorizationCodeRequestAuthenticationValidator.DEFAULT_SCOPE_VALIDATOR);
							((OAuth2AuthorizationCodeRequestAuthenticationProvider) authenticationProvider)
							.setAuthenticationValidator(authenticationValidator);
						}
						
					}
					);
	}
	
	
}
