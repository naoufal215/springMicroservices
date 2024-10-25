package ber.com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("ber.com")
public class ProductServiceApplication {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ProductServiceApplication.class, args);
		
		String mongoDBHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
		String port = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
		
		LOG.info("connected to mongoDB:{}:{}",mongoDBHost,port);
	}

}
