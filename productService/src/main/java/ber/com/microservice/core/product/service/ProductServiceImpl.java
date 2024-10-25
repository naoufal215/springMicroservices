package ber.com.microservice.core.product.service;

import ber.com.api.core.product.Product;

import ber.com.api.core.product.ProductService;
import ber.com.api.exceptions.InvalidInputException;
import ber.com.api.exceptions.NotFoundException;
import ber.com.microservice.core.product.persistence.ProductEntity;
import ber.com.microservice.core.product.persistence.ProductRepository;
import ber.com.util.http.ServiceUtil;
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
	
	public Product getProduct(int productId) {
		LOG.debug("GetProduct: product return the found product for productId={}", productId);
		
		if(productId <1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}
		
		ProductEntity entity = repository.findByProductId(productId)
				.orElseThrow(()->new NotFoundException("No product found for productId: "+productId));
		Product response = mapper.entityToApi(entity);
		response.setServiceAddress(serviceUtil.getServiceAddress());
		
		LOG.debug("GetProduct: found product: {}", response);
		
		return response;
		
	}

	@Override
	public Product create(Product product) {
		try {
			ProductEntity entity = mapper.apiToEntity(product);
			ProductEntity newEntity = repository.save(entity);
			
			LOG.debug("CreateProduct: entity created for product: {}",product);
			
			Product prod = mapper.entityToApi(newEntity);
			prod.setServiceAddress(serviceUtil.getServiceAddress());
			return prod;
			
		}catch(DuplicateKeyException ex) {
			throw new InvalidInputException("Duplicate key, product ID: "+product.getProductId());
		}
	}

	@Override
	public void delete(int productId) {
		LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
		repository.findByProductId(productId).ifPresent(e->repository.delete(e));
		
	}

}
