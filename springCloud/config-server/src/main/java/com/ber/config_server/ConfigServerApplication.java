package com.ber.config_server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConfigServerApplication.class);
	

	public static void main(String[] args) {
		
		
		
		ConfigurableApplicationContext ctx = SpringApplication.run(ConfigServerApplication.class, args);
		
		String repoLocation = ctx.getEnvironment().getProperty("spring.cloud.config.server.git.uri");
		
		LOG.info("Serving configuration from folder: "+repoLocation);
	}

}
