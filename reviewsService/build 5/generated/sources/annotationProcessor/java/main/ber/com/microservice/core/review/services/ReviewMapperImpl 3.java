package ber.com.microservice.core.review.services;

import ber.com.api.core.review.Review;
import ber.com.microservice.core.review.persistence.ReviewEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-11-09T11:42:49+0100",
    comments = "version: 1.6.2, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.11.jar, environment: Java 17.0.8 (Eclipse Adoptium)"
)
@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public Review entityToApi(ReviewEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Review review = new Review();

        review.setProductId( entity.getProductId() );
        review.setReviewId( entity.getReviewId() );
        review.setAuthor( entity.getAuthor() );
        review.setSubject( entity.getSubject() );
        review.setContent( entity.getContent() );

        return review;
    }

    @Override
    public ReviewEntity apiToEntity(Review product) {
        if ( product == null ) {
            return null;
        }

        ReviewEntity reviewEntity = new ReviewEntity();

        reviewEntity.setProductId( product.getProductId() );
        reviewEntity.setReviewId( product.getReviewId() );
        reviewEntity.setAuthor( product.getAuthor() );
        reviewEntity.setSubject( product.getSubject() );
        reviewEntity.setContent( product.getContent() );

        return reviewEntity;
    }

    @Override
    public List<Review> entityListToApiList(List<ReviewEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<Review> list = new ArrayList<Review>( entities.size() );
        for ( ReviewEntity reviewEntity : entities ) {
            list.add( entityToApi( reviewEntity ) );
        }

        return list;
    }

    @Override
    public List<ReviewEntity> apiListToEntityList(List<Review> reviews) {
        if ( reviews == null ) {
            return null;
        }

        List<ReviewEntity> list = new ArrayList<ReviewEntity>( reviews.size() );
        for ( Review review : reviews ) {
            list.add( apiToEntity( review ) );
        }

        return list;
    }
}
