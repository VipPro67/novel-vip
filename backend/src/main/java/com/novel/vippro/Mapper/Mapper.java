package com.novel.vippro.Mapper;

import com.google.cloud.Role;
import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.Chapter.ChapterDetailDTO;
import com.novel.vippro.DTO.Chapter.ChapterListDTO;
import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.FeatureRequest.CreateFeatureRequestDTO;
import com.novel.vippro.DTO.FeatureRequest.FeatureRequestDTO;
import com.novel.vippro.DTO.Genre.GenreDTO;
import com.novel.vippro.DTO.Group.GroupDTO;
import com.novel.vippro.DTO.GroupMember.GroupMemberDTO;
import com.novel.vippro.DTO.Message.MessageDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.Novel.NovelDetailDTO;
import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsDTO;
import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsUpdateDTO;
import com.novel.vippro.DTO.ReadingHistory.ReadingHistoryDTO;
import com.novel.vippro.DTO.Role.RoleApprovalDTO;
import com.novel.vippro.DTO.Tag.TagDTO;
import com.novel.vippro.DTO.User.UserDTO;
import com.novel.vippro.Models.Category;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.Comment;
import com.novel.vippro.Models.FeatureRequest;
import com.novel.vippro.Models.Genre;
import com.novel.vippro.Models.Group;
import com.novel.vippro.Models.GroupMember;
import com.novel.vippro.Models.Message;
import com.novel.vippro.Models.Notification;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.ReaderSettings;
import com.novel.vippro.Models.ReadingHistory;
import com.novel.vippro.Models.RoleApprovalRequest;
import com.novel.vippro.Models.Tag;
import com.novel.vippro.Models.User;

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

    public User DTOtoUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
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

    public NotificationDTO NotificationtoDTO(Notification notification) {
        return modelMapper.map(notification, NotificationDTO.class);
    }

    public GroupMemberDTO GroupMembertoDTO(GroupMember groupMember) {
        return modelMapper.map(groupMember, GroupMemberDTO.class);
    }

    public GroupMember DTOtoGroupMember(GroupMemberDTO groupMemberDTO) {
        GroupMember groupMember = modelMapper.map(groupMemberDTO, GroupMember.class);
        groupMember.setUser(new User());
        groupMember.getUser().setId(groupMemberDTO.getUserId());
        groupMember.setGroup(new Group());
        groupMember.getGroup().setId(groupMemberDTO.getGroupId());
        return groupMember;
    }

    public void updateGroupMemberFromDTO(GroupMemberDTO dto, GroupMember groupMember) {
        modelMapper.map(dto, groupMember);
    }

    public MessageDTO MessagetoDTO(Message message) {
        return modelMapper.map(message, MessageDTO.class);
    }

    public Message DTOtoMessage(MessageDTO messageDTO) {
        return modelMapper.map(messageDTO, Message.class);
    }

    public Group DTOtoGroup(GroupDTO groupDTO) {
        return modelMapper.map(groupDTO, Group.class);
    }

    public GroupDTO GroupToDTO(Group group) {
        return modelMapper.map(group, GroupDTO.class);
    }

    public void updateGroupFromDTO(GroupDTO dto, Group group) {
        modelMapper.map(dto, group);
    }

    public void updateNovelFromDTO(NovelDTO dto, Novel novel) {
        modelMapper.map(dto, novel);
    }

    public void updateChapterFromDTO(ChapterDTO dto, Chapter chapter) {
        modelMapper.map(dto, chapter);
    }

    public void updateCommentFromDTO(CommentDTO dto, Comment comment) {
        modelMapper.map(dto, comment);
    }

    public void updateTagFromDTO(TagDTO dto, Tag tag) {
        modelMapper.map(dto, tag);
    }

    public void updateUserFromDTO(UserDTO dto, User user) {
        modelMapper.map(dto, user);
    }

    public void updateCategoryFromDTO(CategoryDTO dto, Category category) {
        modelMapper.map(dto, category);
    }

    public void updateGenreFromDTO(GenreDTO dto, Genre genre) {
        modelMapper.map(dto, genre);
    }

    public void updateFeatureRequestFromDTO(FeatureRequestDTO dto, FeatureRequest featureRequest) {
        modelMapper.map(dto, featureRequest);
    }

    public void updateNotificationFromDTO(NotificationDTO dto, Notification notification) {
        modelMapper.map(dto, notification);
    }

    public void updateMessageFromDTO(MessageDTO dto, Message message) {
        modelMapper.map(dto, message);
    }

    public RoleApprovalDTO RoleApprovalRequestToDTO(RoleApprovalRequest roleApprovalRequest) {
        return modelMapper.map(roleApprovalRequest, RoleApprovalDTO.class);
    }

}