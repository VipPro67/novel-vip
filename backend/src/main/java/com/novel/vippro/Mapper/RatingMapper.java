package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Rating.RatingDTO;
import com.novel.vippro.Models.Rating;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {
	@Autowired
	private ModelMapper modelMapper;

	public RatingDTO RatingtoDTO(Rating rating) {
		return modelMapper.map(rating, RatingDTO.class);
	}

	public void updateRatingFromDTO(RatingDTO dto, Rating rating) {
		modelMapper.map(dto, rating);
	}

	public Rating DTOtoRating(RatingDTO ratingDTO) {
		return modelMapper.map(ratingDTO, Rating.class);
	}

}
