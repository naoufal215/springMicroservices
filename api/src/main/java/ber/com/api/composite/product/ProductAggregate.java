package ber.com.api.composite.product;

import java.util.List;


public class ProductAggregate {
	
	
	private  int productId;
	
	private  String name;
	
	private  int weight;
	
	private  List<ReviewSummary> reviews;
	
	private  ServiceAddresses serviceAdresses;
	
	

	public ProductAggregate() {
		this.productId = 0;
		this.name = null;
		this.weight = 0;
		this.reviews = null;
		this.serviceAdresses = null;
	}


	public ProductAggregate(
			 int productId,
			 String name ,
			 int weight ,
			List<ReviewSummary> reviews,
			ServiceAddresses serviceAdresses){
		this.productId = productId;
		this.name = name;
		this.weight = weight;
		this.reviews = reviews;
		this.serviceAdresses = serviceAdresses;
	}


	public int getProductId() {
		return productId;
	}


	public void setProductId(int productId) {
		this.productId = productId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getWeight() {
		return weight;
	}


	public void setWeight(int weight) {
		this.weight = weight;
	}


	public List<ReviewSummary> getReviews() {
		return reviews;
	}


	public void setReviews(List<ReviewSummary> reviews) {
		this.reviews = reviews;
	}


	public ServiceAddresses getServiceAdresses() {
		return serviceAdresses;
	}


	public void setServiceAdresses(ServiceAddresses serviceAdresses) {
		this.serviceAdresses = serviceAdresses;
	}


	@Override
	public String toString() {
		return "ProductAggregate [productId=" + productId + ", name=" + name + ", weight=" + weight + ", reviews="
				+ reviews + ", serviceAdresses=" + serviceAdresses + "]";
	}
	
	
	
	

}
