package ber.com.api.exceptions;

public class EventProcessingException extends RuntimeException {

	public EventProcessingException() {
	}

	public EventProcessingException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public EventProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public EventProcessingException(String message) {
		super(message);
	}

	public EventProcessingException(Throwable cause) {
		super(cause);
	}
	
	

}
