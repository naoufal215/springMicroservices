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
				.append("/review?productId=")
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
			switch (HttpStatus.resolve(err.getStatusCode().value())) {
			case NOT_FOUND: {
				
				throw new NotFoundException(getErrorMessage(err));
			}
			case UNPROCESSABLE_ENTITY: {
				throw new InvalidInputException(getErrorMessage(err));
			}
			default:
				LOG.warn("Got an unxpected HTTP erro: {}, will rethrow it", err.getStatusCode());
				LOG.warn("Error body: {}", err.getResponseBodyAsString());
				throw err;
			}
		}
	}
	

	@Override
	public List<Review> getReviews(int productId) {
		try {
			String url = reviewServiceUrl + productId;
			
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
	
	
	private String getErrorMessage(HttpClientErrorException err) {
		try {
			return mapper.readValue(err.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
			
		}catch(IOException er) {
			return err.getMessage();
		}
	}


	
	

}
