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
		CommentDTO dto = modelMapper.map(comment, CommentDTO.class);
		if (comment.getNovel() != null) {
			dto.setNovelId(comment.getNovel().getId());
		} else {
			dto.setNovelId(null);
		}
		if (comment.getChapter() != null) {
			dto.setChapterId(comment.getChapter().getId());
		} else {
			dto.setChapterId(null);
		}
		if(comment.getParent() != null) {
			dto.setParentId(comment.getParent().getId());
		} else {
			dto.setParentId(null);
		}
		dto.setUserId(comment.getUser().getId());
		dto.setUsername(comment.getUser().getUsername());
		return dto;
	}

	public void updateCommentFromDTO(CommentDTO dto, Comment comment) {
		modelMapper.map(dto, comment);
	}
}
