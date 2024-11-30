package com.ber.config_server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	
	@Value("dev-user")
	private String username;
	
	@Value("dev-pwd")
	private String password;
	
	private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

		@Bean
		SecurityFilterChain configure(HttpSecurity http) throws Exception {
			
			http.csrf().disable()
			.authorizeRequests()
			.requestMatchers("/actuator/**").permitAll()
			.requestMatchers("/**").authenticated()
			.and()
			.httpBasic();
			
			return http.build();
		}
		
	    @Bean
	    InMemoryUserDetailsManager userDetailsService() {
	        UserDetails user = User.withUsername(username)
	            .password(passwordEncoder().encode(password))
	            .roles("USER")
	            .build();
	        return new InMemoryUserDetailsManager(user);
	    }
	    
	    @Bean
	    PasswordEncoder passwordEncoder() {
	        return NoOpPasswordEncoder.getInstance(); // Use a proper password encoder in production
	    }

	
}
