package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Bookmark.BookmarkDTO;
import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.Chapter.ChapterDetailDTO;
import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Epub.EpubImportJobDTO;
import com.novel.vippro.DTO.FeatureRequest.CreateFeatureRequestDTO;
import com.novel.vippro.DTO.FeatureRequest.FeatureRequestDTO;
import com.novel.vippro.DTO.File.FileMetadataDTO;
import com.novel.vippro.DTO.Genre.GenreDTO;
import com.novel.vippro.DTO.Group.CreateGroupDTO;
import com.novel.vippro.DTO.Group.GroupDTO;
import com.novel.vippro.DTO.Group.UpdateGroupDTO;
import com.novel.vippro.DTO.GroupMember.GroupMemberDTO;
import com.novel.vippro.DTO.Message.CreateMessageDTO;
import com.novel.vippro.DTO.Message.MessageDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.Rating.RatingDTO;
import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsDTO;
import com.novel.vippro.DTO.ReaderSetting.ReaderSettingsUpdateDTO;
import com.novel.vippro.DTO.ReadingHistory.ReadingHistoryDTO;
import com.novel.vippro.DTO.Report.ReportDTO;
import com.novel.vippro.DTO.Review.ReviewDTO;
import com.novel.vippro.DTO.Role.RoleApprovalDTO;
import com.novel.vippro.DTO.Tag.TagDTO;
import com.novel.vippro.DTO.User.UserDTO;
import com.novel.vippro.Models.*;

import java.util.List;

public interface Mapper {
    // Novel-related mappings
    NovelDTO NoveltoDTO(Novel novel);

    NovelDocument NoveltoDocument(Novel novel);

    List<NovelDTO> NovelListtoDTOList(List<Novel> novels);

    void updateNovelFromDTO(NovelDTO dto, Novel novel);

    // Chapter-related mappings
    ChapterDTO ChaptertoDTO(Chapter chapter);

    ChapterDetailDTO ChaptertoChapterDetailDTO(Chapter chapter);

    ChapterDTO ChaptertoChapterDTO(Chapter chapter);

    List<ChapterDTO> ChapterListtoDTOList(List<Chapter> chapters);

    void updateChapterFromDTO(ChapterDTO dto, Chapter chapter);

    // Category-related mappings
    CategoryDTO CategorytoDTO(Category category);

    Category DTOtoCategory(CategoryDTO categoryDTO);

    List<CategoryDTO> CategoryListtoDTOList(List<Category> categories);

    void updateCategoryFromDTO(CategoryDTO dto, Category category);

    // User-related mappings
    UserDTO UsertoUserDTO(User user);

    User DTOtoUser(UserDTO userDTO);

    void updateUserFromDTO(UserDTO dto, User user);

    // Comment-related mappings
    CommentDTO CommenttoDTO(Comment comment);

    void updateCommentFromDTO(CommentDTO dto, Comment comment);

    // ReaderSettings-related mappings
    ReaderSettingsDTO ReaderSettingsToReaderSettingsDTO(ReaderSettings readerSettings);

    void updateReaderSettingsFromDTO(ReaderSettingsUpdateDTO dto, ReaderSettings settings);

    // FeatureRequest-related mappings
    FeatureRequestDTO RequesttoRequestDTO(FeatureRequest request);

    FeatureRequest RequestDTOtoRequest(FeatureRequestDTO requestDTO);

    FeatureRequest CreateFeatureRequestDTOtoFeatureRequest(CreateFeatureRequestDTO requestDTO);

    void updateFeatureRequestFromDTO(FeatureRequestDTO dto, FeatureRequest featureRequest);

    // Tag-related mappings
    TagDTO TagtoDTO(Tag tag);

    Tag DTOtoTag(TagDTO tagDTO);

    List<TagDTO> TagListtoDTOList(List<Tag> tags);

    void updateTagFromDTO(TagDTO dto, Tag tag);

    // Genre-related mappings
    GenreDTO GenretoDTO(Genre genre);

    Genre DTOtoGenre(GenreDTO genreDTO);

    List<GenreDTO> GenreListtoDTOList(List<Genre> genres);

    void updateGenreFromDTO(GenreDTO dto, Genre genre);

    // ReadingHistory-related mappings
    ReadingHistoryDTO ReadingHistorytoDTO(ReadingHistory readingHistory);

    List<ReadingHistoryDTO> ReadingHistoryListtoDTOList(List<ReadingHistory> readingHistories);

    // Notification-related mappings
    NotificationDTO NotificationtoDTO(Notification notification);

    Notification DTOtoNotification(NotificationDTO notificationDTO);

    void updateNotificationFromDTO(NotificationDTO dto, Notification notification);

    // GroupMember-related mappings
    GroupMemberDTO GroupMembertoDTO(GroupMember groupMember);

    GroupMember DTOtoGroupMember(GroupMemberDTO groupMemberDTO);

    void updateGroupMemberFromDTO(GroupMemberDTO dto, GroupMember groupMember);

    // Message-related mappings
    MessageDTO MessagetoDTO(Message message);

    Message DTOtoMessage(MessageDTO messageDTO);

    Message CreateDTOtoMessage(CreateMessageDTO messageDTO);

    void updateMessageFromDTO(MessageDTO dto, Message message);

    // Group-related mappings
    GroupDTO GroupToDTO(Group group);

    Group DTOtoGroup(GroupDTO groupDTO);

    Group CreateDTOtoGroup(CreateGroupDTO groupDTO);

    void updateGroupFromDTO(UpdateGroupDTO dto, Group group);

    // RoleApprovalRequest-related mappings
    RoleApprovalDTO RoleApprovalRequestToDTO(RoleApprovalRequest roleApprovalRequest);

    RoleApprovalRequest DTOtoRoleApprovalRequest(RoleApprovalDTO roleApprovalDTO);

    void updateRoleApprovalRequestFromDTO(RoleApprovalDTO dto, RoleApprovalRequest roleApprovalRequest);

    // Review-related mappings
    ReviewDTO ReviewtoDTO(Review review);

    Review DTOtoReview(ReviewDTO reviewDTO);

    void updateReviewFromDTO(ReviewDTO dto, Review review);

    // Report-related mappings
    ReportDTO ReporttoDTO(Report report);

    Report DTOtoReport(ReportDTO reportDTO);

    void updateReportFromDTO(ReportDTO dto, Report report);

    // Rating-related mappings
    RatingDTO RatingtoDTO(Rating rating);

    Rating DTOtoRating(RatingDTO ratingDTO);

    void updateRatingFromDTO(RatingDTO dto, Rating rating);

    // Bookmark-related mappings
    BookmarkDTO BookmarktoDTO(Bookmark bookmark);

    Bookmark DTOtoBookmark(BookmarkDTO bookmarkDTO);

    void updateBookmarkFromDTO(BookmarkDTO dto, Bookmark bookmark);

    // FileMetadata-related mappings
    FileMetadataDTO FileMetadataToDTO(FileMetadata metadata);

    FileMetadata DTOToFileMetadata(FileMetadataDTO dto);

    void updateFileMetadataFromDTO(FileMetadataDTO dto, FileMetadata metadata);

    // EPUB import job mappings
    EpubImportJobDTO EpubImportJobToDTO(EpubImportJob job);
}