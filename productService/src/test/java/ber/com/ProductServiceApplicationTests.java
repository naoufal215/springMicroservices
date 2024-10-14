package ber.com;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {
	
	@Autowired
	private WebTestClient client;
	
	@Test
	void getProductById() {
		
		int productId =1;
		
		client.get()
		.uri("/product/"+productId)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.productId").isEqualTo(productId);
		
	}
	
	@Test
	void getProductInvalidParameterString() {
		
		String productId ="invalid";
		
		client.get()
		.uri("/product/"+productId)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.path").isEqualTo("/product/invalid");
		
	}
	
	@Test
	void getProductNotFound() {
		
		int productId =12;
		
		client.get()
		.uri("/product/"+productId)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.path").isEqualTo("/product/"+productId)
		.jsonPath("$.message").isEqualTo("No product found for productId: "+productId);
		
	}
	
	
	@Test
	void getProductInvalidParameterNegativeValue() {
		
		int productId =-13;
		
		client.get()
		.uri("/product/"+productId)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.path").isEqualTo("/product/"+productId)
		.jsonPath("$.message").isEqualTo("Invalid productId: "+productId);
		
	}

	@Test
	void contextLoads() {
	} 

}
