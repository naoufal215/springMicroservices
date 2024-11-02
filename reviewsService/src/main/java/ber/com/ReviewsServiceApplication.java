package ber.com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@ComponentScan("ber.com")
public class ReviewsServiceApplication {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReviewsServiceApplication.class);
	
	private final Integer threadPoolSize;
	private final Integer taskQueueSize;
	
	public ReviewsServiceApplication(
			@Value("${app.theadPoolSize:10}") Integer threadPoolSize,
			@Value("${app.taskQueueSize:100}") Integer taskQueueSize
			) {
		this.threadPoolSize = threadPoolSize;
		this.taskQueueSize = taskQueueSize;
	}
	
	@Bean
	public Scheduler jdbcScheduler() {
		LOG.info("Creates a jdbcScheduler with thread pool size= {}",threadPoolSize);
		return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "jdbc-review-pool");
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ReviewsServiceApplication.class, args);
		
		String dbUrl = ctx.getEnvironment().getProperty("spring.datasource.url");
		
		LOG.info("Connected to MYSQL: {}",dbUrl);
	}

}
