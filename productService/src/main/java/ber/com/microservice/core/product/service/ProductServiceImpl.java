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


@RestController
public class ProductServiceImpl implements ProductService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
	
	private final  ServiceUtil serviceUtil;
	
	private final ProductMapper mapper;
	
	private final ProductRepository repository;
	
	@Autowired
	public ProductServiceImpl(ServiceUtil serviceUtil, ProductMapper mapper, ProductRepository repository) {
		this.serviceUtil = serviceUtil;
		this.mapper = mapper;
		this.repository = repository;
	}
	
	public Mono<Product> getProduct(int productId) {
		LOG.debug("GetProduct: product return the found product for productId={}", productId);
		
		if(productId <1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}
		
		return repository.findByProductId(productId)
				.switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: "+productId)))
				.log(LOG.getName(),Level.FINE)
				.map(e->mapper.entityToApi(e))
				.map(this::setServiceAddress);
		
	}
	
	private Product setServiceAddress(Product product) {
		product.setServiceAddress(serviceUtil.getServiceAddress());
		return product;
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

}
