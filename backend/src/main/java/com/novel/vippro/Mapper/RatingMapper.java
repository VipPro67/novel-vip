package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Rating.RatingDTO;
import com.novel.vippro.Models.Rating;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RatingMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "novelId", source = "novel.id")
    @Mapping(target = "novelTitle", source = "novel.title")
    RatingDTO RatingtoDTO(Rating rating);

    Rating DTOtoRating(RatingDTO ratingDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRatingFromDTO(RatingDTO dto, @MappingTarget Rating rating);
}
