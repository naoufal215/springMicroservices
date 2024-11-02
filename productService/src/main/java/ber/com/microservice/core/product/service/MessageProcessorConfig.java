package ber.com.microservice.core.product.service;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ber.com.api.core.product.Product;
import ber.com.api.core.product.ProductService;
import ber.com.api.event.Event;
import ber.com.api.exceptions.EventProcessingException;

@Configuration
public class MessageProcessorConfig {

	private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

	private final ProductService productService;


	public MessageProcessorConfig(ProductService productService) {
		this.productService = productService;
	}

	@Bean
	Consumer<Event<Integer, Product>> messageProcessor() {

		return event -> {

			switch (event.getEventType()) {
			case CREATE: {

				Product product = event.getData();
				LOG.info("Create product with ID: {}", product.getProductId());
				productService.create(product).block();
				break;
			}
			case DELETE: {
				int productId = event.getKey();
				LOG.info("Delete product ith productId: {}", productId);
				productService.delete(productId).block();
				break;
			}
			default:
				String errorMessage = "Incorrect event type: " + event.getEventType()
						+ " expected a Create or DELETE event";
				LOG.warn(errorMessage);
				throw new EventProcessingException(errorMessage);
			}
			LOG.info("Product message processing done!");
		};
		
	}

}
