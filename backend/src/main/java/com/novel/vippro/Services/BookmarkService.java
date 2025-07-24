package com.novel.vippro.Services;

import com.novel.vippro.DTO.Bookmark.BookmarkCreateDTO;
import com.novel.vippro.DTO.Bookmark.BookmarkDTO;
import com.novel.vippro.DTO.Bookmark.BookmarkUpdateDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Bookmark;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.BookmarkRepository;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BookmarkService {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private Mapper mapper;

    public PageResponse<BookmarkDTO> getUserBookmarks(UUID userId, Pageable pageable) {
        return new PageResponse<>(bookmarkRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable)
                .map(mapper::BookmarktoDTO));
    }

    public List<BookmarkDTO> getNovelBookmarks(UUID novelId) {
        return bookmarkRepository.findByNovelId(novelId).stream()
                .map(mapper::BookmarktoDTO)
                .toList();
    }

    @Transactional
    public BookmarkDTO createBookmark(UUID userId, BookmarkCreateDTO bookmarkDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Novel novel = novelRepository.findById(bookmarkDTO.getNovelId())
                .orElseThrow(() -> new ResourceNotFoundException("Novel", "id", bookmarkDTO.getNovelId()));

        Chapter chapter = chapterRepository.findById(bookmarkDTO.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", "id", bookmarkDTO.getChapterId()));

        // Check if bookmark already exists
        Bookmark existingBookmark = bookmarkRepository.findByUserIdAndChapterId(userId, bookmarkDTO.getChapterId());
        if (existingBookmark != null) {
            // Update existing bookmark
            existingBookmark.setNote(bookmarkDTO.getNote());
            existingBookmark.setProgress(bookmarkDTO.getProgress());
            return mapper.BookmarktoDTO(bookmarkRepository.save(existingBookmark));
        }

        // Create new bookmark
        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setNovel(novel);
        bookmark.setChapter(chapter);
        bookmark.setNote(bookmarkDTO.getNote());
        bookmark.setProgress(bookmarkDTO.getProgress());

        return mapper.BookmarktoDTO(bookmarkRepository.save(bookmark));
    }

    @Transactional
    public BookmarkDTO updateBookmark(UUID id, BookmarkUpdateDTO bookmarkDTO) {
        Bookmark bookmark = bookmarkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark", "id", id));

        bookmark.setNote(bookmarkDTO.getNote());
        bookmark.setProgress(bookmarkDTO.getProgress());

        return mapper.BookmarktoDTO(bookmarkRepository.save(bookmark));
    }

    @Transactional
    public void deleteBookmark(UUID id) {
        if (!bookmarkRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bookmark", "id", id);
        }
        bookmarkRepository.deleteById(id);
    }
}