package ber.com.api.composite.product;

public class ServiceAddresses {
	
	private final String composite;
	private final String product;
	private final String review;
	
	
	
	
	public ServiceAddresses() {
		this.composite = null;
		this.product = null;
		this.review = null;
	}




	public ServiceAddresses(String composite, String product, String review) {
		this.composite = composite;
		this.product = product;
		this.review = review;
	}




	public String getComposite() {
		return composite;
	}




	public String getProduct() {
		return product;
	}




	public String getReview() {
		return review;
	}
	
	
	
	
	
	
	

}
