package ber.com.microservice.core.product.service;

import ber.com.api.core.product.Product;
import ber.com.api.core.product.ProductService;
import ber.com.api.exceptions.InvalidInputException;
import ber.com.api.exceptions.NotFoundException;
import ber.com.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ProductServiceImpl implements ProductService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
	
	private final  ServiceUtil serviceUtil;
	
	@Autowired
	public ProductServiceImpl(ServiceUtil serviceUtil) {
		this.serviceUtil = serviceUtil;
	}
	
	public Product getProduct(int productId) {
		LOG.debug("/product return the found product for productId={}", productId);
		
		if(productId <1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}
		
		if(productId == 12) {
			throw new NotFoundException("No product found for productId: "+ productId);
		}
		
		return new Product(productId, "name - "+productId, 123, serviceUtil.getServiceAddress());
	}

}
