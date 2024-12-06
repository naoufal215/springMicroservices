package ber.com.microservice.composite.product.services;

import java.io.IOException;



import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import ber.com.api.core.product.Product;
import ber.com.api.core.product.ProductService;
import ber.com.api.core.review.Review;
import ber.com.api.core.review.ReviewService;
import ber.com.api.event.Event;
import ber.com.api.event.Event.Type;
import ber.com.api.exceptions.InvalidInputException;
import ber.com.api.exceptions.NotFoundException;
import ber.com.util.http.HttpErrorInfo;
import ber.com.util.http.ServiceUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.net.URI;

@Component
public class ProductCompositeIntegration implements ProductService, ReviewService {

	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);


	private final ObjectMapper mapper;

	private final String productServiceUrl = "http://productsService";
	private final String reviewServiceUrl = "http://reviewsService";

	private final WebClient client;

	private final StreamBridge bridge;

	private final Scheduler publishEventScheduler;
	
	private final ServiceUtil serviceUtil;

	@Autowired
	public ProductCompositeIntegration(

			@Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
			StreamBridge streamBridge,
			@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClient,
			RestTemplate restTemplate, 
			ObjectMapper mapper,
			ServiceUtil serviceUtil

	) {

		this.publishEventScheduler = publishEventScheduler;
		this.bridge = streamBridge;
		this.client = webClient.build();
		this.mapper = mapper;
		this.serviceUtil = serviceUtil;
	}

	@Override
	@Retry(name = "product")
	@TimeLimiter(name = "product")
	@CircuitBreaker(name="product", fallbackMethod = "getProductFallbackValue")
	public Mono<Product> getProduct(int delay, int faultPercent,int productId) {
		
		URI url = UriComponentsBuilder.fromUriString(productServiceUrl
			+"/product/{productId}?delay={delay}&faultPercent={faultPercent}"
				).build(productId,delay,faultPercent);

		LOG.debug("Will call getProduct API on URL: {}", url);

		return client.get().uri(url).retrieve().bodyToMono(Product.class).log(LOG.getName(), Level.FINE)
				.onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
	}
	
	private Mono<Product> getProductFallbackValue( 
			int delay, 
			int faultPercent,
			int productId,
			CallNotPermittedException ex){
		LOG.warn("Creating a failt-fast fallback product from product={}, delay={}, faultPercent={}"
				+ "and exception={}",productId,delay,faultPercent,ex);
		if(productId == 12) {
			String errMessage = "product ID= "+productId+" not found in fallback cache!";
			LOG.warn(errMessage);
			throw new NotFoundException(errMessage);
		}
		return Mono.just(new Product(productId, "fallback product"+productId, productId, serviceUtil.getServiceAddress()));
	}

	@Override
	public Flux<Review> getReviews(int productId) {

		String url = reviewServiceUrl + "/review?productId=" + productId;
		
		LOG.debug("Will call the getReviews API on URL: {}", url);
		

		return client.get().uri(url).retrieve().bodyToFlux(Review.class).log(LOG.getName(), Level.FINE)
				.onErrorResume(error -> Flux.empty());

	}

	@Override
	public Mono<Review> create(Review body) {
		
		return Mono.fromCallable(()->{
			sendMessage("reviews-out-0", new Event<>(Type.CREATE, body.getProductId(),body));
			return body;
		}).subscribeOn(publishEventScheduler);
	}

	@Override
	public Mono<Void> deleteReviews(int productId) {

		return Mono.fromRunnable(()-> sendMessage("reviews-out-0", new Event(Type.DELETE, productId, null)))
				.subscribeOn(publishEventScheduler)
				.then();

	}

	@Override
	public Mono<Product> create(Product product) {
		
		return Mono.fromCallable(()->{
			sendMessage("products-out-0", new Event(Type.CREATE, product.getProductId(), product));
			
			return product;
		}).subscribeOn(publishEventScheduler);
	}

	@Override
	public Mono<Void> delete(int productId) {
		return Mono.fromRunnable(()->sendMessage("products-out-0", new Event(Type.DELETE,productId,null)))
				.subscribeOn(publishEventScheduler)
				.then();
	}

	private RuntimeException handleHttpClientException(HttpClientErrorException ex) {

		HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
		if (status == null) {
			return new RuntimeException("Error arrived");
		}
		switch (status) {
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

		} catch (IOException er) {
			return err.getMessage();
		}
	}

	public Mono<Health> getProductHealth() {
		return getHealth(productServiceUrl);
	}

	public Mono<Health> getReviewHealth() {
		return getHealth(reviewServiceUrl);
	}

	private Mono<Health> getHealth(String url){
		url += "/actuator/health";
		
		LOG.debug("Will call the health API on URL: {}",url);
		
		return client.get().uri(url).retrieve().bodyToMono(String.class)
				.map(result->new Health.Builder().up().build())
				.onErrorResume(ex->Mono.just(new Health.Builder().down(ex).build()))
				.log(LOG.getName(), Level.FINE);
	}
	
	private void sendMessage(String bindingName, Event event) {
		LOG.debug("Sending a {} message to {}", event.getEventType(),bindingName);
		
		Message message = MessageBuilder.withPayload(event)
				.setHeader("partitionKey", event.getKey())
				.build();
		bridge.send(bindingName, message);
		
	}
	
	private Throwable handleException(Throwable ex) {
		
		if(!(ex instanceof WebClientResponseException)) {
			LOG.debug("Got a unexpected error: {}, will rethrow it", ex.toString());
			return ex;
		}
		
		WebClientResponseException exp = (WebClientResponseException) ex;
		HttpStatus status = HttpStatus.resolve(exp.getStatusCode().value());
		if(status == null) {
			throw new RuntimeException("can't resolve error status");
		}
		
		switch (status) {
		
		case NOT_FOUND :
			
			return new NotFoundException(getErrorMessage(exp));
		
		case UNPROCESSABLE_ENTITY :
			return new InvalidInputException(getErrorMessage(exp));
			
		default:
			LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", exp.getStatusCode());
			LOG.warn("Erro body: {}", exp.getResponseBodyAsString());
			return ex;
			
		}
	}
	
	private String getErrorMessage(WebClientResponseException exp) {
		try {
			return mapper.readValue(exp.getResponseBodyAsString(), HttpErrorInfo.class)
					.getMessage();
		}catch(IOException ioexp) {
			return exp.getMessage();
		}
	}
	
	

}
