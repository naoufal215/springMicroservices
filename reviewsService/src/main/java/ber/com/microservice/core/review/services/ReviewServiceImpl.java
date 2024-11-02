package ber.com.microservice.core.review.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import ber.com.api.core.review.Review;
import ber.com.api.core.review.ReviewService;
import ber.com.api.exceptions.InvalidInputException;
import ber.com.microservice.core.review.persistence.ReviewEntity;
import ber.com.microservice.core.review.persistence.ReviewRepository;
import ber.com.util.http.ServiceUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
public class ReviewServiceImpl implements ReviewService {

	private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

	private final ServiceUtil serviceUtil;

	private final ReviewMapper mapper;

	private final ReviewRepository repository;
	
	private final Scheduler jdbcScheduler;

	@Autowired
	public ReviewServiceImpl(
			@Qualifier("jdbcScheduler")
			Scheduler scheduler,
			ServiceUtil serviceUtil, 
			ReviewMapper mapper, 
			ReviewRepository repository) {
		this.serviceUtil = serviceUtil;
		this.mapper = mapper;
		this.repository = repository;
		this.jdbcScheduler = scheduler;
	}

	@Override
	public Flux<Review> getReviews(int productId) {

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}
		LOG.info("Will get reviews from product with ID: {}", productId);
		return Mono.fromCallable(
					()->internalGetReviews(productId)
		).flatMapMany(Flux::fromIterable)
		 .log(LOG.getName(), Level.FINE )
		 .subscribeOn(jdbcScheduler);

	}

	private List<Review> internalGetReviews(int productId) {
		List<ReviewEntity> entityList = repository.findByProductId(productId);
		
		List<Review> reviews = mapper.entityListToApiList(entityList);
		reviews = reviews
				.stream()
				.map(e->{
					e.setServiceAddress(serviceUtil.getServiceAddress()); 
					return e;
					})
				.toList();

		LOG.debug("GetReviews: response size: {}", reviews.size());
		
		return reviews;
	}

	@Override
	public Mono<Review> create(Review body) {
		
		if(body.getProductId() < 1) {
			throw new InvalidInputException("Invalid productId: "+ body.getProductId());
		}
		
		return Mono.fromCallable(()->internalCreateReview(body))
				.subscribeOn(jdbcScheduler);
		
	}

	private Review internalCreateReview(Review body) {
		try {
			
			ReviewEntity reviewEntity = mapper.apiToEntity(body);
			ReviewEntity newCreated = repository.save(reviewEntity);
			
			LOG.debug("Create: created a review entity: Review {}",body);
			
			Review review =  mapper.entityToApi(newCreated);
			review.setServiceAddress(this.serviceUtil.getServiceAddress());
			return review;
		}catch(DataIntegrityViolationException ex) {
			throw new InvalidInputException("Duplicate key, product Id: "+body.getProductId()+", Review Id: "+body.getReviewId());
		}
	}

	@Override
	public Mono<Void> deleteReviews(int productId) {
		if(productId < 1) {
			throw new InvalidInputException("Invalid productId: "+ productId);
		}
		
		return Mono.fromRunnable(()->internalDeleteReviews(productId))
				.subscribeOn(jdbcScheduler)
				.then();
		

	}

	private void internalDeleteReviews(int productId) {
		LOG.debug("deleteReviews: tries to delete reviews for the product with procutId: {}", productId);
		repository.deleteAll(repository.findByProductId(productId));
	}

}
