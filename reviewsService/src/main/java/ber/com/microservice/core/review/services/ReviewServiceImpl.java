package ber.com.microservice.core.review.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import ber.com.api.core.review.Review;
import ber.com.api.core.review.ReviewService;
import ber.com.api.exceptions.InvalidInputException;
import ber.com.util.http.ServiceUtil;


@RestController
public class ReviewServiceImpl implements ReviewService {
	
	Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);
	
	private final ServiceUtil serviceUtil;
	
	
	
	@Autowired
	public ReviewServiceImpl(ServiceUtil serviceUtil) {
		this.serviceUtil = serviceUtil;
	}




	@Override
	public List<Review> getReviews(int productId) {
		
		if(productId < 1) {
			throw new InvalidInputException("Invalid productId: "+productId);
		}
		
		if(productId ==213) {
			LOG.debug("No reviews found for productId: {}", productId);
			return new ArrayList<>();
		}
		
		List<Review> reviews = new ArrayList<>() {{
			add(new Review(productId, 1, "Author 1", "subject 1", "content 1", serviceUtil.getServiceAddress()));
			add(new Review(productId, 2, "Author 2", "subject 2", "content 2", serviceUtil.getServiceAddress()));
			add(new Review(productId, 3, "Author 3", "subject 3", "content 3", serviceUtil.getServiceAddress()));
			add(new Review(productId, 4, "Author 4", "subject 4", "content 4", serviceUtil.getServiceAddress()));
		}};
		
		LOG.debug("/reviews response size: {}", reviews.size());
		
		return reviews;
		
		
	}
	
	

}
