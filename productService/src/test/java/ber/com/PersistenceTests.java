package ber.com;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;


import ber.com.microservice.core.product.persistence.ProductEntity;
import ber.com.microservice.core.product.persistence.ProductRepository;
import reactor.test.StepVerifier;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase {

	@Autowired
	private ProductRepository repository;

	private ProductEntity savedProduct;

	@BeforeEach
	void setUp() {
		StepVerifier.create(repository.deleteAll()).verifyComplete();

		ProductEntity product = new ProductEntity(1, "product1", 987);

		StepVerifier.create(repository.save(product)).expectNextMatches(created -> {
			savedProduct = created;
			return assertEqualsProducts(created, product);
		}).verifyComplete();

	}

	@Test
	void create() {
		ProductEntity product = new ProductEntity(3, "3n", 827);
		StepVerifier.create(repository.save(product))
				.expectNextMatches(created -> created.getProductId() == product.getProductId()).verifyComplete();

		StepVerifier.create(repository.findByProductId(product.getProductId()))
				.expectNextMatches(founded -> assertEqualsProducts(founded, product)).verifyComplete();

		StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();
	}

	@Test
	void update() {
		savedProduct.setName("3ne");
		StepVerifier.create(repository.save(savedProduct))
				.expectNextMatches(updated -> updated.getName().equals(savedProduct.getName())).verifyComplete();

		StepVerifier.create(repository.findById(savedProduct.getId()))
				.expectNextMatches(
						founded -> founded.getVersion() == 1 && founded.getName().equals(savedProduct.getName()))
				.verifyComplete();
	}

	@Test
	void delete() {
		StepVerifier.create(repository.delete(savedProduct)).verifyComplete();
		StepVerifier.create(repository.findByProductId(savedProduct.getProductId())).verifyComplete();
	}

	@Test
	void getProductId() {
		StepVerifier.create(repository.findByProductId(savedProduct.getProductId()))
				.expectNextMatches(founded -> assertEqualsProducts(founded, savedProduct)).verifyComplete();
	}

	@Test
	void duplicateError() {
		ProductEntity product = new ProductEntity(savedProduct.getProductId(), "n", 134);

		StepVerifier.create(repository.save(product)).expectError(DuplicateKeyException.class).verify();

	}

	@Test
	void opimisticLockError() {

		ProductEntity prod1 = repository.findByProductId(savedProduct.getProductId()).block();
		ProductEntity prod2 = repository.findByProductId(savedProduct.getProductId()).block();

		prod1.setName("NlKL");
		repository.save(prod1).block();

		StepVerifier.create(repository.save(prod2)).expectError(OptimisticLockingFailureException.class).verify();
	}

	private boolean assertEqualsProducts(ProductEntity expected, ProductEntity actual) {
		return expected.getProductId() == actual.getProductId() && expected.getName().equals(actual.getName())
				&& expected.getWeight() == actual.getWeight();
	}

}
