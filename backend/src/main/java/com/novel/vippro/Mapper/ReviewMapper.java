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
		ReviewDTO dto = modelMapper.map(review, ReviewDTO.class);
        if(review.getNovel() != null) {
            dto.setNovelId(review.getNovel().getId());
            dto.setNovelTitle(review.getNovel().getTitle());
        }
        if(review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
            dto.setUsername(review.getUser().getUsername());
            dto.setUserAvatar(review.getUser().getAvatar());
        }
        return dto;
	}

	public Review DTOtoReview(ReviewDTO reviewDTO) {
		return modelMapper.map(reviewDTO, Review.class);
	}

	public void updateReviewFromDTO(ReviewDTO dto, Review review) {
		modelMapper.map(dto, review);
	}

}
