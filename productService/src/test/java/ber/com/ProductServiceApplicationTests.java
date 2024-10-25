package ber.com;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import ber.com.api.core.product.Product;
import ber.com.api.core.review.Review;
import ber.com.microservice.core.product.persistence.ProductRepository;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests  extends MongoDbTestBase{
	
	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private WebTestClient client;
	
	@BeforeEach
	void setup() {
		repository.deleteAll();
	}
	
	@Test
	void getProductById() {
		
		int productId =1;
		
		postAndVerifyProduct(productId, HttpStatus.OK);
		assertTrue(repository.findByProductId(productId).isPresent());
		
		getAndVerifyProduct(productId, HttpStatus.OK)
		.jsonPath("$.productId")
		.isEqualTo(productId);
		
	}
	
	void deleteProduct() {
		int productId = 1;
		
		postAndVerifyProduct(productId, HttpStatus.OK);
		assertTrue(repository.findByProductId(productId).isPresent());
		
		deleteAndVerifyProduct(productId, HttpStatus.OK);
		assertFalse(repository.findByProductId(productId).isPresent());
		
		deleteAndVerifyProduct(productId, HttpStatus.OK);
	}
	
	@Test
	void getProductInvalidParameterString() {
		
		getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
		.jsonPath("$.path").isEqualTo("/product/no-integer")
		.jsonPath("$.message").isEqualTo("Type mismatch.");
		
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
	
	
	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expected){
		return getAndVerifyProduct("/"+productId,expected);
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdQuery, HttpStatus expected){
		return client.get()
				.uri("/product"+productIdQuery)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expected)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}
	
	private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expected){
		
		Product review = new Product(productId, "Name"+productId, productId, "SA");
		
		return client.post()
				.uri("/product/")
				.body(Mono.just(review), Review.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expected)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}
	
	private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus){
		return client.delete()
				.uri("/product/"+productId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}

}
