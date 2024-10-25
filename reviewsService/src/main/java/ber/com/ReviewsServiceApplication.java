package ber.com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("ber.com")
public class ReviewsServiceApplication {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReviewsServiceApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ReviewsServiceApplication.class, args);
		
		String dbUrl = ctx.getEnvironment().getProperty("spring.datasource.url");
		
		LOG.info("Connected to MYSQL: {}",dbUrl);
	}

}
