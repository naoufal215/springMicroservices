package ber.com;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import ber.com.api.core.review.Review;
import ber.com.api.event.Event;
import ber.com.api.event.Event.Type;
import ber.com.microservice.core.review.persistence.ReviewRepository;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
properties = {"spring.cloud.stram.defaultBinder=rabbit",
			  "logging.level.ber.com=DEBUG",
			  "eureka.client.enabled=false"
			 }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewsServiceApplicationTests extends MySQLTestBase {
	
	
	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ReviewRepository repository;
	
	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Integer, Review>> messageProcessor;
	
	
	@BeforeEach
	void setUp() {
		repository.deleteAll();
	}
	
	@Test
	void getReviewsByProductId() {
		
		int productId =1;
		
		assertEquals(0, repository.findByProductId(productId).size());
		
		sendCreateReviewEvent(productId, 1);
		sendCreateReviewEvent(productId, 2);
		sendCreateReviewEvent(productId, 3);
		
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
		
		sendCreateReviewEvent(productId, reviewId);
		
		assertEquals(1, repository.findByProductId(productId).size());
		
		sendDeleteReviewEvent(productId);
		assertEquals(0, repository.findByProductId(productId).size());
		
		sendDeleteReviewEvent(productId);
		
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
	
	
	private void sendCreateReviewEvent(int productId, int reviewId) {
		Review review = new Review(productId, reviewId, "author"+reviewId, "subject"+reviewId,
				"content"+reviewId, "SA");
		Event<Integer, Review> event = new Event(Type.CREATE,productId, review);
		
		messageProcessor.accept(event);
	}
	
	
	private void sendDeleteReviewEvent(int productId) {
		Event<Integer, Review> event = new Event(Type.DELETE, productId, null);
		messageProcessor.accept(event);
	}

}
