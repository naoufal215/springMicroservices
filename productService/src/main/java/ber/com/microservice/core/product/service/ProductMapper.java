package ber.com.microservice.core.product.service;

import org.mapstruct.Mapper;

import org.mapstruct.Mapping;

import ber.com.api.core.product.Product;
import ber.com.microservice.core.product.persistence.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {

	@Mapping(target = "serviceAddress", ignore = true)
	Product entityToApi(ProductEntity entity);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "version", ignore = true)
	ProductEntity apiToEntity(Product product);

}
