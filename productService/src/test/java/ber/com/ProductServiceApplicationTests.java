package ber.com;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import ber.com.api.core.product.Product;
import ber.com.api.core.review.Review;
import ber.com.api.event.Event;
import ber.com.api.event.Event.Type;
import ber.com.api.exceptions.InvalidInputException;
import ber.com.microservice.core.product.persistence.ProductRepository;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests  extends MongoDbTestBase{
	
	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private WebTestClient client;
	
	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Integer, Product>> messageProcessor;
	
	@BeforeEach
	void setup() {
		repository.deleteAll().block();
	}
	
	@Test
	void getProductById() {
		
		int productId =1;
		
		assertNull(repository.findByProductId(productId).block());
		assertEquals(0, (long) repository.count().block());
		
		sendCreateProductEvent(productId);
		
		assertNotNull(repository.findByProductId(productId).block());
		assertEquals(1, (long) repository.count().block());
		
		getAndVerifyProduct(productId, HttpStatus.OK)
		.jsonPath("$.productId").isEqualTo(productId);
		
		
	}
	
	void deleteProduct() {
		int productId = 1;
		
		sendCreateProductEvent(productId);
		assertNotNull(repository.findByProductId(productId).block());
		
		sendDeleteProductEvent(productId);
		assertNull(repository.findByProductId(productId).block());
		
		sendDeleteProductEvent(productId);
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
		
		getAndVerifyProduct(productId, HttpStatus.NOT_FOUND)
		.jsonPath("$.path").isEqualTo("/product/"+productId)
		.jsonPath("$.message").isEqualTo("No product found for productId: "+productId);
		
	}
	
	void duplicateError() {
		
		int productId = 1;
		
		assertNull(repository.findByProductId(productId).block());
		
		sendCreateProductEvent(productId);
		
		assertNotNull(repository.findByProductId(productId).block());
		
		InvalidInputException thrown = assertThrows(
				InvalidInputException.class,
				()-> sendCreateProductEvent(productId),
				"Expected a InvalidInputException here!");
		
		assertEquals("duplicate key, product Id: "+productId, thrown.getMessage());
	}
	
	
	@Test
	void getProductInvalidParameterNegativeValue() {
		
		int productId =-13;
		
		getAndVerifyProduct(productId, HttpStatus.UNPROCESSABLE_ENTITY)
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
	
	
	
	private void sendCreateProductEvent(int productId) {
		Product product = new Product(productId,"name"+productId, 12+productId, "SA");
		Event<Integer, Product> event = new Event(Type.CREATE,productId, product);
		
		messageProcessor.accept(event);
	}
	
	
	private void sendDeleteProductEvent(int productId) {
		Event<Integer, Product> event = new Event(Type.DELETE, productId, null);
		messageProcessor.accept(event);
	}

}
