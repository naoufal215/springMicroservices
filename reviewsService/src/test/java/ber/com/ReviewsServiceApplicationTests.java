package ber.com;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import ber.com.api.core.review.Review;
import ber.com.microservice.core.review.persistence.ReviewRepository;
import reactor.core.publisher.Mono;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewsServiceApplicationTests extends MySQLTestBase {
	
	
	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ReviewRepository repository;
	
	
	@BeforeEach
	void setUp() {
		repository.deleteAll();
	}
	
	@Test
	void getReviewsByProductId() {
		
		int productId =1;
		
		assertEquals(0, repository.findByProductId(productId).size());
		
		postAndVerifyReview(productId, 1, HttpStatus.OK);
		postAndVerifyReview(productId, 2, HttpStatus.OK);
		postAndVerifyReview(productId, 3, HttpStatus.OK);
		
		assertEquals(3, repository.findByProductId(1).size());
		
		getAndVerifyReveiwsByProductId(productId+"", HttpStatus.OK)
		.jsonPath("$.length()").isEqualTo(3)
		.jsonPath("$[2].productId").isEqualTo(productId)
		.jsonPath("$[2].reviewId").isEqualTo(3);
		
	}
	
	@Test
	void getReviewsMissingParameter() {
		
		getAndVerifyReveiwsByProductId("",HttpStatus.BAD_REQUEST)
		.jsonPath("$.path").isEqualTo("/review")
		.jsonPath("$.message").isEqualTo("Type mismatch.");
		
		
	}
	
	@Test
	void getReviewsInvalidParameter() {
		
		getAndVerifyReveiwsByProductId("?productId=no-integer", HttpStatus.BAD_REQUEST)
		.jsonPath("$.path").isEqualTo("/review")
		.jsonPath("$.message").isEqualTo("Type mismatch.");
	}
	
	@Test
	void getReviewsNotFound() {
		
		getAndVerifyReveiwsByProductId("?productId=213", HttpStatus.BAD_REQUEST)
		.jsonPath("$.path").isEqualTo("/review")
		.jsonPath("$.message").isEqualTo("Type mismatch.");
		
	}
	
	
	@Test
	void getReviewsInvalidParameterNegativeValue() {
		
		int productIdInvalid = -1;
		
		getAndVerifyReveiwsByProductId("?productId="+productIdInvalid, HttpStatus.BAD_REQUEST)
		.jsonPath("$.path").isEqualTo("/review")
		.jsonPath("$.message").isEqualTo("Type mismatch.");
		
	}
	
	void deleteReviews() {
		int productId =1;
		int reviewId =1;
		
		postAndVerifyReview(productId, reviewId, HttpStatus.OK);
		
		assertEquals(1, repository.findByProductId(productId).size());
		
		deleteAndVerifyReviewByProductId(productId, HttpStatus.OK);
		assertEquals(0, repository.findByProductId(productId).size());
		
		deleteAndVerifyReviewByProductId(productId, HttpStatus.OK);
		
	}

	@Test
	void contextLoads() {
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyReveiwsByProductId(String productId, HttpStatus expected){
		return getAndVerifyReviewsByProductId("?productId="+productId,expected);
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expected){
		return client.get()
				.uri("/review"+productIdQuery)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expected)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}
	
	private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expected){
		
		Review review = new Review(productId, reviewId, "Author"+reviewId, "Subject"+reviewId, "Content"+reviewId, "SA");
		
		return client.post()
				.uri("/review")
				.body(Mono.just(review), Review.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expected)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}
	
	private WebTestClient.BodyContentSpec deleteAndVerifyReviewByProductId(int productId, HttpStatus expectedStatus){
		return client.delete()
				.uri("/review?productId="+productId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}

}
