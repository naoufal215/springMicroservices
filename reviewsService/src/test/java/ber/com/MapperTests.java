package ber.com;

import static org.junit.Assert.assertNotNull;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ber.com.api.core.product.Product;
import ber.com.api.core.review.Review;
import ber.com.microservice.core.review.persistence.ReviewEntity;
import ber.com.microservice.core.review.services.ReviewMapper;

class MapperTests {
	
	
	private ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);
	
	@Test
	void mapperTests() {
		assertNotNull(mapper);
		
		Review api = new Review(1, 1, "aa", "bb", "cc", "dd");
		
		ReviewEntity entity = mapper.apiToEntity(api);
		
		assertEquals(api.getAuthor(), entity.getAuthor());
		assertEquals(api.getProductId(), entity.getProductId());
		assertEquals(api.getContent(), entity.getContent());
		assertEquals(api.getSubject(), entity.getSubject());
		
		Review api2 = mapper.entityToApi(entity);
		
		assertEquals(api2.getAuthor(), entity.getAuthor());
		assertEquals(api2.getProductId(), entity.getProductId());
		assertEquals(api2.getContent(), entity.getContent());
		assertEquals(api2.getSubject(), entity.getSubject());
	}

}
