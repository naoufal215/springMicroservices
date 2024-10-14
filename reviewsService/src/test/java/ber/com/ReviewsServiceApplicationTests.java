package ber.com;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReviewsServiceApplicationTests {
	
	
	@Autowired
	private WebTestClient client;
	
	@Test
	void getReviewsByProductId() {
		
		int productId =1;
		
		client.get()
		.uri("/review?productId="+productId)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.length()").isEqualTo(4)
		.jsonPath("$[0].productId").isEqualTo(productId);
		
	}
	
	@Test
	void getReviewsMissingParameter() {
		
		
		
		client.get()
		.uri("/review")
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.path").isEqualTo("/review");
		
	}
	
	@Test
	void getReviewsInvalidParameter() {
		
		String productId ="hello";
		
		client.get()
		.uri("/review?productId="+productId)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.path").isEqualTo("/review");	
	}
	
	@Test
	void getReviewsNotFound() {
		
		int productId =213;
		
		client.get()
		.uri("/review?productId="+productId)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.length()").isEqualTo(0);
		
	}
	
	
	@Test
	void getReviewsInvalidParameterNegativeValue() {
		
		int productId =-1;
		
		client.get()
		.uri("/review?productId="+productId)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.path").isEqualTo("/review")
		.jsonPath("$.message").isEqualTo("Invalid productId: "+ productId);
		
	}

	@Test
	void contextLoads() {
	}

}
