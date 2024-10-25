package ber.com;

import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;



@Testcontainers
@EntityScan(basePackages = "ber.com.microservice.core.product.persistence")
public abstract class MongoDbTestBase {
	
    @Container
    private static MongoDBContainer database = new MongoDBContainer("mongo:6.0.4");
    
    static {
    	if(!database.isRunning()) {
    		database.start();
    	}
    }

	
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", () -> database.getReplicaSetUrl());
		
	}
	
}
