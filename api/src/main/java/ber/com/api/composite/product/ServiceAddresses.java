package ber.com.api.composite.product;

public class ServiceAddresses {
	
	private  String composite;
	private  String product;
	private  String review;




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
	
	
	
	
	public void setComposite(String composite) {
		this.composite = composite;
	}




	public void setProduct(String product) {
		this.product = product;
	}




	public void setReview(String review) {
		this.review = review;
	}




	@Override
	public String toString() {
		return "ServiceAddresses [composite=" + composite + ", product=" + product + ", review=" + review + "]";
	}
	

}
