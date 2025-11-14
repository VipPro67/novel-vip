package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Review.ReviewDTO;
import com.novel.vippro.Models.Review;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "novelId", source = "novel.id")
    @Mapping(target = "novelTitle", source = "novel.title")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userAvatar", source = "user.avatar")
    ReviewDTO ReviewtoDTO(Review review);

    Review DTOtoReview(ReviewDTO reviewDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReviewFromDTO(ReviewDTO dto, @MappingTarget Review review);
}
