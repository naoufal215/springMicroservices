package ber.com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan("ber.com")
public class ProductCompositeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeApplication.class, args);
	}
	
	
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
