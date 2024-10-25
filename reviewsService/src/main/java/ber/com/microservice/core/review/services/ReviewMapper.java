package ber.com.microservice.core.review.services;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ber.com.api.core.review.Review;
import ber.com.microservice.core.review.persistence.ReviewEntity;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

	@Mapping(target = "serviceAddress", ignore = true)
	Review entityToApi(ReviewEntity entity);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "version", ignore = true)
	ReviewEntity apiToEntity(Review product);
	
	List<Review> entityListToApiList(List<ReviewEntity> entities);
	
	List<ReviewEntity> apiListToEntityList(List<Review> reviews);

}
