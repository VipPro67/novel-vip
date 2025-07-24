package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.Models.Comment;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
	@Autowired
	private ModelMapper modelMapper;

	public CommentDTO CommenttoDTO(Comment comment) {
		return modelMapper.map(comment, CommentDTO.class);
	}

	public void updateCommentFromDTO(CommentDTO dto, Comment comment) {
		modelMapper.map(dto, comment);
	}
}
