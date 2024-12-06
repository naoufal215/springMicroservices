package ber.com;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import ber.com.api.core.product.Product;
import ber.com.api.core.review.Review;
import ber.com.api.exceptions.InvalidInputException;
import ber.com.api.exceptions.NotFoundException;
import ber.com.microservice.composite.product.services.ProductCompositeIntegration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
classes= {TestSecurityConfig.class},
properties = {
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=",
    "eureka.client.enabled=false",
    "spring.main.allow-bean-definition-overriding=true"
})
class ProductCompositeApplicationTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

	@Autowired
	private WebTestClient client;
	
	@MockBean
	private ProductCompositeIntegration integration;

	@BeforeEach
	void setup() throws Exception {
		
		when(integration.getProduct(anyInt(),anyInt(),eq(PRODUCT_ID_OK)))
			.thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "name", 1,"mock-address")));
		
		when(integration.getReviews(PRODUCT_ID_OK))
			.thenReturn(Flux.fromIterable(singletonList(new Review(PRODUCT_ID_OK,1, "author", "subject", "content", "mock-address"))));
		
		when(integration.getProduct(anyInt(),anyInt(),eq(PRODUCT_ID_NOT_FOUND)))
			.thenThrow(new NotFoundException("NOT FOUND: "+ PRODUCT_ID_NOT_FOUND));
		
		when(integration.getProduct(anyInt(),anyInt(),eq(PRODUCT_ID_INVALID)))
		.thenThrow(new InvalidInputException("INVALID: "+ PRODUCT_ID_INVALID));

			
	}
	
	@Test
	void getProductById() {
		
		getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
		.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
		.jsonPath(".reviews.length()").isEqualTo(1);
	}
	
	@Test
	void getProductNotFound() {
		
		getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
		.jsonPath("$.path").isEqualTo("/product-composite/"+PRODUCT_ID_NOT_FOUND)
		.jsonPath("$.message").isEqualTo("NOT FOUND: "+PRODUCT_ID_NOT_FOUND);
	}
	
	@Test
	void getProductInvalidInput() {
		
		getAndVerifyProduct(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY)
		.jsonPath("$.path").isEqualTo("/product-composite/"+PRODUCT_ID_INVALID)
		.jsonPath("$.message").isEqualTo("INVALID: "+PRODUCT_ID_INVALID);
	}

	@Test
	void contextLoads() {
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expected){
		return client.get()
			.uri("/product-composite/"+productId)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expected)
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody();
	}

}
