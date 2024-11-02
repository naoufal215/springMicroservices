package ber.com.api.event;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import static java.time.ZonedDateTime.now;

public class Event<K, T> {
	
	public enum Type {
		CREATE,
		DELETE
	}
	
	private final Type eventType;
	private final K key;
	private final T data;
	
	@JsonSerialize(using = ZonedDateTimeSerializer.class)
	private final ZonedDateTime eventCreatedAt;
	
	
	public Event() {
		this.eventType = null;
		this.key = null;
		this.data = null;
		this.eventCreatedAt = null;
		
	}
	
	public Event(Type eventType, K key, T data) {
		this.eventType = eventType;
		this.key = key;
		this.data = data;
		this.eventCreatedAt = now();
		
	}

	public Type getEventType() {
		return eventType;
	}

	public K getKey() {
		return key;
	}

	public T getData() {
		return data;
	}
	
	
	public ZonedDateTime getEventCreatedAt() {
		return eventCreatedAt;
	}

	
	
	
	

}
