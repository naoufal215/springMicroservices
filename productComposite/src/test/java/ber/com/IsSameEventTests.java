package ber.com;



import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ber.com.api.core.product.Product;
import ber.com.api.event.Event;
import ber.com.api.event.Event.Type;

import static ber.com.IsSameEvent.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class IsSameEventTests {

	ObjectMapper mapper = new ObjectMapper();
	
	
	@Test
	void testEventObjectCompare() throws JsonProcessingException {
		
		Event<Integer, Product> event1 = new Event(Type.CREATE,1, new Product(1, "name",1, null));
		Event<Integer, Product> event2 = new Event(Type.CREATE,1, new Product(1, "name",1, null));
		Event<Integer, Product> event3 = new Event(Type.DELETE,1, null);
		Event<Integer, Product> event4 = new Event(Type.CREATE,1, new Product(2, "name",1, null));
	
	
		String eventJson = mapper.writeValueAsString(event1);
		
		assertThat(eventJson, Matchers.is(sameEventExceptCreatedAt(event2)));
		assertThat(eventJson, Matchers.not(sameEventExceptCreatedAt(event3)));
		assertThat(eventJson, Matchers.not(sameEventExceptCreatedAt(event4)));
		
	}

}
