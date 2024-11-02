package ber.com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan("ber.com")
public class ProductCompositeApplication {
	
	private final Logger LOG = LoggerFactory.getLogger(ProductCompositeApplication.class);
	


	
	
	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeApplication.class, args);
	}


}
