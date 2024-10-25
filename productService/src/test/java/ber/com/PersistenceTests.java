package ber.com;

import static org.junit.Assert.assertFalse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import com.mongodb.DuplicateKeyException;

import ber.com.microservice.core.product.persistence.ProductEntity;
import ber.com.microservice.core.product.persistence.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;


@DataMongoTest
public class PersistenceTests extends MongoDbTestBase {
	
	@Autowired
	private ProductRepository repository;
	
	private ProductEntity savedProduct;
	
	
	
	@BeforeEach
	void setUp() {
		repository.deleteAll();
		
		ProductEntity product = new ProductEntity( 1, "product1", 987);
		
		savedProduct = repository.save(product);
		
		assertEqualsProducts(savedProduct,product);
	}
	
	@Test
	void create() {
		ProductEntity product = new ProductEntity(3, "3n", 827);
		repository.save(product);
		
		ProductEntity found = repository.findById(product.getId()).get();
		assertEqualsProducts(product, found);
		
		assertEquals(2, repository.count());
	}
	
	@Test
	void update() {
		savedProduct.setName("3ne");
		repository.save(savedProduct);
		
		ProductEntity prod2 = repository.findById(savedProduct.getId()).get();
		
		assertEquals(1, (long)prod2.getVersion());
		assertEqualsProducts(savedProduct, prod2);
	}
	
	@Test
	void delete() {
		repository.delete(savedProduct);
		
		assertFalse(repository.existsById(savedProduct.getId()));
	}
	
	@Test
	void getProductId() {
		ProductEntity prod2 = repository.findByProductId(savedProduct.getProductId()).get();
		
		assertEqualsProducts(savedProduct, prod2);	
	}
	
	
	@Test
	void opimisticLockError() {
		
		ProductEntity prod1 = repository.findByProductId(savedProduct.getProductId()).get();
		ProductEntity prod2 = repository.findByProductId(savedProduct.getProductId()).get();
		
		prod1.setName("NlKL");
		repository.save(prod1);
		
		assertThrows(OptimisticLockingFailureException.class, ()->{
			prod2.setName("RER");
			repository.save(prod2);
		});
		
		ProductEntity prod3 = repository.findByProductId(savedProduct.getProductId()).get();
		assertEquals(1, (long)prod3.getVersion());
		prod2.setName("NlKL");
		assertEqualsProducts(prod2, prod3);
	}
	
	@Test
	void paging() {
		repository.deleteAll();
		List<ProductEntity> entities = IntStream.rangeClosed(1001, 1010)
				.mapToObj(i->new ProductEntity(i, "n-"+i,123+i))
				.collect(Collectors.toList());
		
		repository.saveAll(entities);
		
		Pageable nextPage = PageRequest.of(0, 4, Direction.ASC, "productId");
		nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
		nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
		nextPage = testNextPage(nextPage, "[1009, 1010]", false);
	}
	
	private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
		Page<ProductEntity> productPage = repository.findAll(nextPage);
		assertEquals(expectedProductIds, 
					productPage.getContent().stream().map(p->p.getProductId()).collect(Collectors.toList()).toString()
				);
		assertEquals(expectsNextPage,	productPage.hasNext());
		return productPage.nextPageable();
	}
	
	private void assertEqualsProducts(ProductEntity expected, ProductEntity actual) {
		assertEquals(expected.getProductId(), actual.getProductId());
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getWeight(), actual.getWeight());
	}
	

}
