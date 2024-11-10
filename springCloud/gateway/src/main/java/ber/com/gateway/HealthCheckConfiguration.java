package ber.com.gateway;

import java.util.LinkedHashMap;

import java.util.Map;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Configuration
public class HealthCheckConfiguration {

		private static final Logger LOG = LoggerFactory.getLogger(HealthCheckConfiguration.class);
		
		private WebClient webClient;


		public HealthCheckConfiguration(
				@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClient
				) {
			
			this.webClient = webClient.build();
		}
		
		
		@Bean
		ReactiveHealthContributor healthCheckMicroservices() {
			
			final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();
			
			registry.put("product", ()->getHealth("http://productsService"));
			registry.put("review", ()->getHealth("http://reviewsService"));
			registry.put("product-composite", ()->getHealth("http://productCompositeService"));
			
			return CompositeReactiveHealthContributor.fromMap(registry);
		}
		
		private Mono<Health> getHealth(String baseUrl){
			String url = baseUrl +"/actuator/health";
			
			LOG.debug("Setting up a call to the health API on URL: {}", url);
			
			return webClient.get()
							.uri(url)
							.retrieve()
							.bodyToMono(String.class)
							.map(result -> new Health.Builder().up().build())
							.onErrorResume(error -> Mono.just(new Health.Builder().down(error).build()))
							.log(LOG.getName(),Level.FINE);
		}
		
}
