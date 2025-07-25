package com.novel.vippro.Mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.novel.vippro.DTO.Bookmark.BookmarkDTO;
import com.novel.vippro.Models.Bookmark;

@Component
public class BookmarkMapper {

	@Autowired
	private ModelMapper modelMapper;

	public BookmarkDTO BookmarktoDTO(Bookmark bookmark) {
		return modelMapper.map(bookmark, BookmarkDTO.class);
	}

	public Bookmark DTOtoBookmark(BookmarkDTO bookmarkDTO) {
		return modelMapper.map(bookmarkDTO, Bookmark.class);
	}

	public void updateBookmarkFromDTO(BookmarkDTO dto, Bookmark bookmark) {
		modelMapper.map(dto, bookmark);
	}
}