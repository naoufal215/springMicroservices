package ber.com.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
properties = {"eureka.client.enabled=false"})
class GatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
