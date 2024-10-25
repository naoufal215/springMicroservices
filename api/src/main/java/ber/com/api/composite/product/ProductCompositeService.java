package ber.com.api.composite.product;

import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag( name= "ProductComposite", description= "Rest API for composite product information.")
public interface ProductCompositeService {
	
	
	
	@Operation(
				summary="${api.product-composite.create-composite-product.description}",
				description ="${api.product-composite.create-composite-product.notes}"
			)
	@ApiResponses(
			value = {
					@ApiResponse(responseCode = "400", description="${api.responseCodes.badRequest.description}"),
					@ApiResponse(responseCode="422", description="${api.responseCodes.uprocessableEntity.description}")
			})
	@PostMapping(
			value = "/product-composite",
			consumes="application/json"
			)
	void createProduct(@RequestBody ProductAggregate body) ;
	
	
	
	
	/**
	 * Sample usage: "curl $host:$port/product-composite/1"
	 * 
	 * @param productId : id of the product
	 * @return the composite product info, if found, else null
	 */
	@Operation(
			summary = "${api.product-composite.get-composite-product.description}",
			description = "${api.product-composite.get-composite-product.notes}"
			)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
			}	
		)
	@GetMapping(
			value= "/product-composite/{productId}",
			produces= "application/json"
			)
	ProductAggregate getProduct(@PathVariable("productId") int productId);
	
	
	@Operation(
			summary = "${api.product-composite.delete-composite-product.description}",
			description = "${api.product-composite.delete-composite-product.notes}"
			)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "400", description ="${api.responseCodes.badRequest.description}"),
			@ApiResponse(responseCode = "422", description="${api.responseCodes.unprocessableEntity.description}")
	})
	@DeleteMapping(value="/product-composite/{productId}")
	void deleteProduct(@PathVariable(name = "productId") int productId);

}
