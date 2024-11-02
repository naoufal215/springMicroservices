package ber.com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

import ber.com.api.core.product.Product;
import ber.com.microservice.core.product.persistence.ProductEntity;


@SpringBootApplication
@ComponentScan("ber.com")
public class ProductServiceApplication {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceApplication.class);
	
	private final ReactiveMongoOperations mongoTemplate;
	
	

	@Autowired
	public ProductServiceApplication(ReactiveMongoOperations mongoTemplate) {
		
		this.mongoTemplate = mongoTemplate;
	}
	
	@EventListener(classes = ContextRefreshedEvent.class)
	public void initIndicesAfterStartup() {
		
		MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext
			= mongoTemplate.getConverter().getMappingContext();
		
		IndexResolver reslover = new MongoPersistentEntityIndexResolver(mappingContext);
		
		ReactiveIndexOperations indexOperations = mongoTemplate.indexOps(Product.class);
		
		reslover.resolveIndexFor(ProductEntity.class)
				.forEach(e -> indexOperations.ensureIndex(e).block());
	}



	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ProductServiceApplication.class, args);
		
		String mongoDBHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
		String port = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
		
		LOG.info("connected to mongoDB:{}:{}",mongoDBHost,port);
	}

}
