package ber.com.microservice.core.product.service;

import ber.com.api.core.product.Product;


import ber.com.api.core.product.ProductService;
import ber.com.api.exceptions.InvalidInputException;
import ber.com.api.exceptions.NotFoundException;
import ber.com.microservice.core.product.persistence.ProductEntity;
import ber.com.microservice.core.product.persistence.ProductRepository;
import ber.com.util.http.ServiceUtil;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.dao.DuplicateKeyException;

import java.time.Duration;
import java.util.Random;


@RestController
public class ProductServiceImpl implements ProductService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
	
	private final  ServiceUtil serviceUtil;
	
	private final ProductMapper mapper;
	
	private final ProductRepository repository;
	
	private final Random random = new Random();
	
	
	
	@Autowired
	public ProductServiceImpl(ServiceUtil serviceUtil, ProductMapper mapper, ProductRepository repository) {
		this.serviceUtil = serviceUtil;
		this.mapper = mapper;
		this.repository = repository;
	}
	
	@Override
	public Mono<Product> getProduct(int delay, int faultPercent,int productId ) {
		LOG.debug("GetProduct: product return the found product for productId={}", productId);
		
		if(productId <1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}
		
		return repository.findByProductId(productId)
				.map(e -> throwErrorIfBadLuck(e, faultPercent))
				.delayElement(Duration.ofSeconds(delay))
				.switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: "+productId)))
				.log(LOG.getName(),Level.FINE)
				.map(e->mapper.entityToApi(e))
				.map(this::setServiceAddress);
		
	}
	
	

	@Override
	public Mono<Product> create(Product product) {
		
			LOG.debug("CreateProduct: entity created for product: {}",product);

			ProductEntity entity = mapper.apiToEntity(product);
			
			
			
			return repository.save(entity)
					.log(LOG.getName(), Level.FINE)
					.onErrorMap(DuplicateKeyException.class,
					 ex -> new InvalidInputException("Duplicate key, product Id: "+product.getProductId())
					).map(e-> mapper.entityToApi(e));


	}

	@Override
	public Mono<Void> delete(int productId) {
		LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
		return repository.findByProductId(productId)
				.log(LOG.getName(), Level.FINE)
				.map(e->repository.delete(e))
				.flatMap(e->e);
		
	}
	
	
	private Product setServiceAddress(Product product) {
		product.setServiceAddress(serviceUtil.getServiceAddress());
		return product;
	}
	
	private ProductEntity throwErrorIfBadLuck(ProductEntity entity, int faultPercent) {
		if(faultPercent == 0) {
			return entity;
		}
		int randomThreshold = getRandomNumber(1, 100);
		
		if(faultPercent < randomThreshold) {
			LOG.debug("We got lucky, no error occurred, {}  < {}", faultPercent, randomThreshold);
		}else {
			LOG.info("Bad luck, an error occurred, {} >= {}", faultPercent, randomThreshold);
			throw new RuntimeException("Something wen wrong...");
		}
		return entity;
	}
	
	private int getRandomNumber(int min, int max) {
		if(max < min) {
			throw new IllegalArgumentException("Max must be greater then min");
		}
		return random.nextInt((max-min)+1)+min;
	}

}
