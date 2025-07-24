package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Review.ReviewDTO;
import com.novel.vippro.Models.Review;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
	@Autowired
	private ModelMapper modelMapper;

	public ReviewDTO ReviewtoDTO(Review review) {
		return modelMapper.map(review, ReviewDTO.class);
	}

	public Review DTOtoReview(ReviewDTO reviewDTO) {
		return modelMapper.map(reviewDTO, Review.class);
	}

	public void updateReviewFromDTO(ReviewDTO dto, Review review) {
		modelMapper.map(dto, review);
	}

}
