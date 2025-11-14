package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.Models.Comment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "novelId", source = "novel.id")
    @Mapping(target = "chapterId", source = "chapter.id")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    CommentDTO CommenttoDTO(Comment comment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCommentFromDTO(CommentDTO dto, @MappingTarget Comment comment);
}
