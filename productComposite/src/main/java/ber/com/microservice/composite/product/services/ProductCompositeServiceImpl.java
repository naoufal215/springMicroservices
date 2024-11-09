package ber.com.microservice.composite.product.services;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

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
import reactor.core.publisher.Mono;

@RestController
public class ProductCompositeServiceImpl  implements ProductCompositeService{
	
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);
	
	private final ServiceUtil serviceUtil;
	
	private ProductCompositeIntegration integration;
	
	
	private Function<List<Review>, String> concatAdresses = reviews ->{
		
		return reviews.stream()
				.map(Review::getServiceAddress)
				.distinct()
				.reduce("", (add1,add2)->add1+"\n"+add2);
	};
	
	
	
	
	
	@Autowired
	public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
		
		this.serviceUtil = serviceUtil;
		this.integration = integration;
	}



	@Override
	public Mono<ProductAggregate> getProduct(int productId) {
		
		LOG.debug("getProduct: lookup a product aggregate for productId: {}", productId);
		
		return Mono.zip(
				values -> createProductAggregate((Product)values[0],(List<Review>) values[1], serviceUtil.getServiceAddress()),
				integration.getProduct(productId),
				integration.getReviews(productId).collectList())
				.doOnError(ex -> LOG.warn("getCompositeProduct failed: {}", ex.toString()))
				.log(LOG.getName(), Level.FINE);
		
		
	}





	@Override
	public Mono<Void> createProduct(ProductAggregate body) {
		try {
			List<Mono> monoList = new ArrayList<>();
			
			LOG.info("Will create a new composite entity for product.id: {}", body.getProductId());
			
			Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
			
			monoList.add(integration.create(product));
			
			if(body.getReviews() != null) {
				body.getReviews().forEach(review ->{
					Review reviewApi = new Review(body.getProductId(), review.getReviewId(), 
							review.getAuthor(), review.getSubject(), review.getContent(), null);
					monoList.add(integration.create(reviewApi));
				});
			}
			
			LOG.debug("createCompositeProduct: composite entities created for productId: {}",
					body.getProductId());
			
			return Mono.zip(element -> "", monoList.toArray(new Mono[0]))
					.doOnError(ex -> LOG.warn("createCOmpositeProduct failed: {}", body.getProductId()))
					.then();
			
			
		}catch(RuntimeException ex) {
			LOG.warn("createCompositeProduct failed", ex);
			throw ex;
			
		}
		
	}

	@Override
	public Mono<Void> deleteProduct(int productId) {
		LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);
		
		try {
			
			return Mono.zip(
					r -> "",
					integration.delete(productId),
					integration.deleteReviews(productId)
					).doOnError(ex -> LOG.warn("delete failed: {}", ex.toString()))
					.log(LOG.getName(), Level.FINE)
					.then();
			
		} catch (RuntimeException e) {
			LOG.warn("deleteProduct failed", e);
			throw e;
		}
		
		
		
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
		String reviewAddress = (reviews != null && !reviews.isEmpty()) ? concatAdresses.apply(reviews):"";
		ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress);
		return new ProductAggregate(productId, name, weight, reviewSummaries, serviceAddresses);
	}
	
	
	

}
