package com.novel.vippro.mapper;

import com.novel.vippro.dto.NovelDTO;
import com.novel.vippro.dto.NovelDetailDTO;
import com.novel.vippro.dto.ReaderSettingsDTO;
import com.novel.vippro.dto.SubscriptionDTO;
import com.novel.vippro.dto.SubscriptionHistoryDTO;
import com.novel.vippro.dto.SubscriptionPlanDTO;
import com.novel.vippro.dto.TagDTO;
import com.novel.vippro.dto.UserDTO;
import com.novel.vippro.dto.CategoryDTO;
import com.novel.vippro.dto.ChapterDTO;
import com.novel.vippro.dto.ChapterDetailDTO;
import com.novel.vippro.dto.ChapterListDTO;
import com.novel.vippro.dto.CommentDTO;
import com.novel.vippro.dto.CreateFeatureRequestDTO;
import com.novel.vippro.dto.FeatureRequestDTO;
import com.novel.vippro.dto.GenreDTO;
import com.novel.vippro.dto.ReaderSettingsUpdateDTO;
import com.novel.vippro.dto.ReadingHistoryDTO;
import com.novel.vippro.models.Novel;
import com.novel.vippro.models.ReaderSettings;
import com.novel.vippro.models.ReadingHistory;
import com.novel.vippro.models.Tag;
import com.novel.vippro.models.User;
import com.novel.vippro.models.Category;
import com.novel.vippro.models.Chapter;
import com.novel.vippro.models.Comment;
import com.novel.vippro.models.FeatureRequest;
import com.novel.vippro.models.Genre;

import org.checkerframework.checker.units.qual.C;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Mapper {

    @Autowired
    private ModelMapper modelMapper;

    public NovelDTO NoveltoDTO(Novel novel) {
        return modelMapper.map(novel, NovelDTO.class);
    }

    public CategoryDTO CategorytoDTO(Category category) {
        return modelMapper.map(category, CategoryDTO.class);
    }

    public NovelDetailDTO NoveltoNovelDetailDTO(Novel novel) {
        return modelMapper.map(novel, NovelDetailDTO.class);
    }

    public ChapterDTO ChaptertoDTO(Chapter chapter) {
        return modelMapper.map(chapter, ChapterDTO.class);
    }

    public List<NovelDTO> NovelListtoDTOList(List<Novel> novels) {
        return novels.stream()
                .map(this::NoveltoDTO)
                .collect(Collectors.toList());
    }

    public List<ChapterDTO> ChapterListtoDTOList(List<Chapter> chapters) {
        return chapters.stream()
                .map(this::ChaptertoDTO)
                .collect(Collectors.toList());
    }

    public CommentDTO CommenttoDTO(Comment comment) {
        return modelMapper.map(comment, CommentDTO.class);
    }

    public ChapterDetailDTO ChaptertoChapterDetailDTO(Chapter chapter) {
        ChapterDetailDTO chapterDetailDTO = modelMapper.map(chapter, ChapterDetailDTO.class);
        chapterDetailDTO.setNovelId(chapter.getNovel().getId());
        chapterDetailDTO.setNovelTitle(chapter.getNovel().getTitle());
        return chapterDetailDTO;
    }

    public ChapterDTO ChaptertoChapterDTO(Chapter chapter) {
        return modelMapper.map(chapter, ChapterDTO.class);
    }

    public ChapterListDTO ChaptertoChapterListDTO(Chapter chapter) {
        ChapterListDTO chapterListDTO = modelMapper.map(chapter, ChapterListDTO.class);
        chapterListDTO.setNovelId(chapter.getNovel().getId());
        chapterListDTO.setNovelTitle(chapter.getNovel().getTitle());
        return chapterListDTO;
    }

    public UserDTO UsertoUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public ReaderSettingsDTO ReaderSettingsToReaderSettingsDTO(ReaderSettings readerSettings) {
        return modelMapper.map(readerSettings, ReaderSettingsDTO.class);
    }

    public void updateReaderSettingsFromDTO(ReaderSettingsUpdateDTO dto, ReaderSettings settings) {
        modelMapper.map(dto, settings);
    }

    public FeatureRequestDTO RequesttoRequestDTO(FeatureRequest request) {
        return modelMapper.map(request, FeatureRequestDTO.class);
    }

    public FeatureRequest RequestDTOtoRequest(FeatureRequestDTO requestDTO) {
        return modelMapper.map(requestDTO, FeatureRequest.class);
    }

    public FeatureRequest CreateFeatureRequestDTOtoFeatureRequest(CreateFeatureRequestDTO requestDTO) {
        return modelMapper.map(requestDTO, FeatureRequest.class);
    }

    public TagDTO TagtoDTO(Tag tag) {
        return modelMapper.map(tag, TagDTO.class);
    }

    public Tag DTOtoTag(TagDTO tagDTO) {
        return modelMapper.map(tagDTO, Tag.class);
    }

    public List<TagDTO> TagListtoDTOList(List<Tag> tags) {
        return tags.stream()
                .map(this::TagtoDTO)
                .collect(Collectors.toList());
    }

    public GenreDTO GenretoDTO(Genre genre) {
        return modelMapper.map(genre, GenreDTO.class);
    }

    public Genre DTOtoGenre(GenreDTO genreDTO) {
        return modelMapper.map(genreDTO, Genre.class);
    }

    public List<GenreDTO> GenreListtoDTOList(List<Genre> genres) {
        return genres.stream()
                .map(this::GenretoDTO)
                .collect(Collectors.toList());
    }

    public Category DTOtoCategory(CategoryDTO categoryDTO) {
        return modelMapper.map(categoryDTO, Category.class);
    }

    public CategoryDTO DTOtoCategoryDTO(Category category) {
        return modelMapper.map(category, CategoryDTO.class);
    }

    public List<CategoryDTO> CategoryListtoDTOList(List<Category> categories) {
        return categories.stream()
                .map(this::CategorytoDTO)
                .collect(Collectors.toList());
    }

    public ReadingHistoryDTO ReadingHistorytoDTO(ReadingHistory readingHistory) {
        return modelMapper.map(readingHistory, ReadingHistoryDTO.class);
    }

    public List<ReadingHistoryDTO> ReadingHistoryListtoDTOList(List<ReadingHistory> readingHistories) {
        return readingHistories.stream()
                .map(this::ReadingHistorytoDTO)
                .collect(Collectors.toList());
    }

}