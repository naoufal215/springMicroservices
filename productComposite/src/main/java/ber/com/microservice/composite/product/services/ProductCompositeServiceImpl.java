package ber.com.microservice.composite.product.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import ber.com.api.composite.product.ProductAggregate;
import ber.com.api.composite.product.ProductCompositeService;
import ber.com.api.composite.product.ReviewSummary;
import ber.com.api.composite.product.ServiceAddresses;
import ber.com.api.core.product.Product;
import ber.com.api.core.review.Review;
import ber.com.api.exceptions.NotFoundException;
import ber.com.util.http.ServiceUtil;

@RestController
public class ProductCompositeServiceImpl  implements ProductCompositeService{
	
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);
	
	private final ServiceUtil serviceUtil;
	
	private ProductCompositeIntegration integration;
	
	
	
	
	
	@Autowired
	public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
		
		this.serviceUtil = serviceUtil;
		this.integration = integration;
	}



	@Override
	public ProductAggregate getProduct(int productId) {
		
		LOG.debug("getProduct: lookup a product aggregate for productId: {}", productId);
		
		Product product = integration.getProduct(productId);
		
		if(product == null ) {
			throw new NotFoundException("No product found for productID: "+productId);
		}
		
		List<Review> reviews = integration.getReviews(productId);
		
		LOG.debug("getProduct: aggregate entity found for productId: {}", productId);
		
		return createProductAggregate(product, reviews, serviceUtil.getServiceAddress());
		
	}





	@Override
	public void createProduct(ProductAggregate body) {
		try {
			LOG.debug("createCompositeProduct: creates a new composite entity for product: {}", body);
			
			Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
			
			integration.create(product);
			
			if(body.getReviews() != null) {
				body.getReviews().forEach(r ->{
					Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
					try {
						integration.create(review);
					} catch (Exception e) {
						LOG.warn("createCOmpositeProduct failed to create review", e);
					}
				});
			}
			
			LOG.debug("createCompositeProduct: composite entities created for the productID : {}", body.getProductId());
		}catch(RuntimeException ex) {
			LOG.warn("createCompositeProduct failed", ex);
			throw ex;
			
		}
		
	}

	@Override
	public void deleteProduct(int productId) {
		LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);
		
		try {
			integration.deleteReviews(productId);
			integration.delete(productId);
		} catch (RuntimeException e) {
			LOG.warn("createCompositeProduct failed", e);
			throw e;
		}
		
		
		LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId); 
		
	}
	
	
	private ProductAggregate createProductAggregate(Product product, List<Review> reviews, String serviceAddress) {
		int productId = product.getProductId();
		String name = product.getName();
		int weight = product.getWeight();
		
		List<ReviewSummary> reviewSummaries = (reviews == null)?
				null: reviews.stream()
				.map(r-> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
				.toList();
		
		String productAddress = product.getServiceAddress();
		String reviewAddress = (reviews != null && !reviews.isEmpty()) ? reviews.get(0).getServiceAddress():"";
		ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress);
		return new ProductAggregate(productId, name, weight, reviewSummaries, serviceAddresses);
	}

}
