package ber.com.eurekaServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {
	
	private final String username;
	private final String password;
	
	
	@Autowired
	public SecurityConfig(
				@Value("${app.eureka-username}") String username,
				@Value("${app.eureka-password}") String password
			) {
		this.username = username;
		this.password = password;
	}
	
	@Bean
	public InMemoryUserDetailsManager userDetailsManager() {
		
		UserDetails user = User.withDefaultPasswordEncoder()
				.username(username)
				.password(password)
				.roles("USER")
				.build();
		return new InMemoryUserDetailsManager(user);
	}
	
	@Bean
	public SecurityFilterChain configure(HttpSecurity http) throws Exception{
		http
		.csrf().disable()
		.authorizeHttpRequests()
		.anyRequest().authenticated()
		.and()
		.httpBasic();
		return http.build();
	}
}
