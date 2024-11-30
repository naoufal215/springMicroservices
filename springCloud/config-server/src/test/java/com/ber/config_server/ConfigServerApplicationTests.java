package com.ber.config_server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, 
properties = {"spring.profiles.active=native"})
class ConfigServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
