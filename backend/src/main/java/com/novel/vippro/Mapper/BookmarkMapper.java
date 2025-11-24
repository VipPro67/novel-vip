package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Bookmark.BookmarkDTO;
import com.novel.vippro.Models.Bookmark;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface BookmarkMapper {

    BookmarkDTO BookmarktoDTO(Bookmark bookmark);

    Bookmark DTOtoBookmark(BookmarkDTO bookmarkDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBookmarkFromDTO(BookmarkDTO dto, @MappingTarget Bookmark bookmark);
}
