package ber.com.api.composite.product;

import java.util.List;

public class ProductAggregate {
	
	private final int productId;
	private final String name;
	private final int weight;
	private final List<ReviewSummary> reviews;
	private final ServiceAddresses serviceAfresses;
	
	
	public ProductAggregate(int productId, String name, int weight, List<ReviewSummary> reviews,
			ServiceAddresses serviceAfresses) {

		this.productId = productId;
		this.name = name;
		this.weight = weight;
		this.reviews = reviews;
		this.serviceAfresses = serviceAfresses;
	}


	public int getProductId() {
		return productId;
	}


	public String getName() {
		return name;
	}


	public int getWeight() {
		return weight;
	}


	public List<ReviewSummary> getReviews() {
		return reviews;
	}


	public ServiceAddresses getServiceAfresses() {
		return serviceAfresses;
	}
	
	
	

}
