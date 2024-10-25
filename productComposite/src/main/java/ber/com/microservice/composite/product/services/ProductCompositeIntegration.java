package ber.com.microservice.composite.product.services;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import ber.com.api.core.product.Product;
import ber.com.api.core.product.ProductService;
import ber.com.api.core.review.Review;
import ber.com.api.core.review.ReviewService;
import ber.com.api.exceptions.InvalidInputException;
import ber.com.api.exceptions.NotFoundException;
import ber.com.util.http.HttpErrorInfo;

@Component
public class ProductCompositeIntegration implements ProductService, ReviewService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);
	
	private final RestTemplate restTemplate;
	private final ObjectMapper mapper;
	
	private final String productServiceUrl;
	private final String reviewServiceUrl;
	
	
	
	@Autowired
	public ProductCompositeIntegration(
			RestTemplate restTemplate,
			ObjectMapper mapper,
			@Value("${app.product-service.host}") String productServiceHost,
			@Value("${app.product-service.port}") String productServicePort,
			@Value("${app.review-service.host}") String reviewServiceHost,
			@Value("${app.review-service.port}") String reviewServicePort
			
	) {
		
		this.restTemplate = restTemplate;
		this.mapper = mapper;
		
		this.productServiceUrl = new StringBuilder().append("http://")
													.append(productServiceHost)
													.append(":")
													.append(productServicePort)
													.append("/product/")
													.toString();
		this.reviewServiceUrl = new StringBuilder().append("http://")
				.append(reviewServiceHost)
				.append(":")
				.append(reviewServicePort)
				.append("/review")
				.toString();
	}
	
	@Override
	public Product getProduct(int productId) {
		try {
			String url = productServiceUrl + productId;
			
			LOG.debug("Will call getProduct API on URL: {}", url);
			
			Product product = restTemplate.getForObject(url, Product.class);
			LOG.debug("Found a product with id: {}", product.getProductId());
			
			return product;
			
		}catch (HttpClientErrorException err) {
			throw handleHttpClientException(err);
		}
	}
	

	@Override
	public List<Review> getReviews(int productId) {
		try {
			String url = reviewServiceUrl+"?productId=" + productId;
			
			LOG.debug("will call getReviews API on url: {}", url);
			
			List<Review> reviews = restTemplate.exchange(
					url,
					HttpMethod.GET,
					null,
					new ParameterizedTypeReference<List<Review>>() {})
					.getBody();
			
			LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId );
			
			return reviews;
		}catch (Exception err) {
			LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", err.getMessage());
			return new ArrayList<>();
			
		}
	}

	@Override
	public Review create(Review body){
		try {
			String url = reviewServiceUrl;
			LOG.debug("Will post a new review to URL: {}", url);
			
			Review review = restTemplate.postForObject(url, body, Review.class);
			LOG.debug("Created a review with id: {}", review.getProductId());
			
			return review;
		}catch(HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
			
		}
	}

	@Override
	public void deleteReviews(int productId) {
		
		try {
			String url = reviewServiceUrl +"?productId="+productId;
			LOG.debug("Will call the deleteReviews API on url: {}", url);
			
			restTemplate.delete(url);
		}catch(HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
		
	}

	@Override
	public Product create(Product product) {
		try {
			String url = productServiceUrl;
			LOG.debug("will post a new product to URL: {}", url);
			
			Product prod = restTemplate.postForObject(url, product, Product.class);
			LOG.debug("Created a product with id: {}", prod.getProductId());
			
			return prod;
			
		}catch(HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
	}

	@Override
	public void delete(int productId){
		try {
				String url = productServiceUrl+"/"+productId;
				LOG.debug("Will call the deleteProduct API on URL: {}", url);
				
				restTemplate.delete(url);
		}catch(HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
		
	}
	
	
	private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
		
		HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
		if( status == null) {
			return new RuntimeException("Error arrived");
		}
		switch(status) {
		case NOT_FOUND:
				return new NotFoundException(getErrorMessage(ex));
		case UNPROCESSABLE_ENTITY:
				return new InvalidInputException(getErrorMessage(ex));
		default:
				LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
				LOG.warn("Error body: {}", ex.getResponseBodyAsString());
				return ex;
		}
		
	}
	
	private String getErrorMessage(HttpClientErrorException err) {
		try {
			return mapper.readValue(err.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
			
		}catch(IOException er) {
			return err.getMessage();
		}
	}


	
	

}
