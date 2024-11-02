package ber.com.microservice.composite.product.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ber.com.microservice.composite.product.services.ProductCompositeIntegration;

@Configuration
public class HealthCheckConfig {

	
	ProductCompositeIntegration integration;
	
	
	@Autowired
	public HealthCheckConfig(ProductCompositeIntegration integration) {
		this.integration = integration;
	}



	@Bean
	ReactiveHealthContributor coreServices() {
		
		final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();
		
		registry.put("product", ()->integration.getProductHealth());
		registry.put("review", ()->integration.getReviewHealth());
		
		return CompositeReactiveHealthContributor.fromMap(registry);
		
	}
}
