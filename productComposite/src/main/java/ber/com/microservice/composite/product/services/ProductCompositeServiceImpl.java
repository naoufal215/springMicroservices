package ber.com.microservice.composite.product.services;

import java.util.List;import java.util.stream.Collector;
import java.util.stream.Collectors;

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
	
	private final ServiceUtil serviceUtil;
	
	private ProductCompositeIntegration integration;
	
	
	
	
	
	@Autowired
	public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
		
		this.serviceUtil = serviceUtil;
		this.integration = integration;
	}






	@Override
	public ProductAggregate getProduct(int productId) {
		
		Product product = integration.getProduct(productId);
		
		if(product == null) {
			throw new NotFoundException("Nor product found for productId: "+productId);
		}
		
		List<Review> reviews = integration.getReviews(productId);
		
		return createProductAggregate(product, reviews, serviceUtil.getServiceAddress());
		
	}






	private ProductAggregate createProductAggregate(Product product, List<Review> reviews, String serviceAddress) {
		
		int productId = product.getProductId();
		String name = product.getName();
		int weight = product.getWeight();
		
		List<ReviewSummary> reviewSummaries =
				(reviews == null)? null:
					reviews.stream()
					.map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
					.collect(Collectors.toList());
		
		String productAddress = product.getServiceAddress();
		String reveiwsAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress():"";
		
		ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reveiwsAddress );
		
		return new ProductAggregate(productId, name, weight, reviewSummaries, serviceAddresses);
		
		
	}

}
