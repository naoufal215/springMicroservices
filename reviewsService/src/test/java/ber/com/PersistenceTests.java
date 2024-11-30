package ber.com;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import ber.com.microservice.core.review.persistence.ReviewEntity;
import ber.com.microservice.core.review.persistence.ReviewRepository;


@DataJpaTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PersistenceTests extends MySQLTestBase{
	
	@Autowired
	private ReviewRepository repository;
	
	private ReviewEntity savedEntity;
	
	@BeforeEach
	void setup() {
		repository.deleteAll();
		
		ReviewEntity ent = new ReviewEntity(1,2, "a", "s", "c");
		
		savedEntity = repository.save(ent);
		
	}
	
	
	@Test
	void create() {
		ReviewEntity rev = new ReviewEntity(1, 3, "a", "s", "c");
		
		repository.save(rev);
		
		ReviewEntity rev2 = repository.findById(rev.getId()).get();
		
		assertEqualsReview(rev, rev2);
	}
	
	@Test
	void update() {
		savedEntity.setAuthor("ABDC");
		repository.save(savedEntity);
		
		ReviewEntity rev = repository.findById(savedEntity.getId()).get();
		
		
		assertEqualsReview(savedEntity, rev);
	}
	
	@Test
	void getProductId() {
		List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());
		
		MatcherAssert.assertThat(entityList, Matchers.hasSize(1));
		
		assertEquals(savedEntity.getId(), entityList.get(0).getId());
	}
	
	
	void assertEqualsReview(ReviewEntity expected, ReviewEntity actuel) {
		
		assertEquals(expected.getProductId(), actuel.getProductId());
		assertEquals(expected.getAuthor(), actuel.getAuthor());
		assertEquals(expected.getContent(), actuel.getContent());
		assertEquals(expected.getReviewId(), actuel.getReviewId());
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

}
