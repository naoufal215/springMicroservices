package ber.com;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ber.com.api.core.product.Product;
import ber.com.microservice.core.product.persistence.ProductEntity;
import ber.com.microservice.core.product.service.ProductMapper;

class MapperTests {
	
	
	private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);
	
	@Test
	void mapperTests() {
		assertNotNull(mapper);
		
		Product api = new Product(1, "n", 1, "sa");
		
		ProductEntity entity = mapper.apiToEntity(api);
		
		assertEquals(api.getName(), entity.getName());
		assertEquals(api.getProductId(), entity.getProductId());
		assertEquals(api.getWeight(), entity.getWeight());
		
		Product api2 = mapper.entityToApi(entity);
		
		assertEquals(api2.getName(), entity.getName());
		assertEquals(api2.getProductId(), entity.getProductId());
		assertEquals(api2.getWeight(), entity.getWeight());
	}

}
