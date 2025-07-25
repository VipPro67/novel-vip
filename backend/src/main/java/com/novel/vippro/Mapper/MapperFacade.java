package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Bookmark.BookmarkDTO;
import com.novel.vippro.DTO.Category.CategoryDTO;
import com.novel.vippro.DTO.Chapter.ChapterDTO;
import com.novel.vippro.DTO.Chapter.ChapterDetailDTO;
import com.novel.vippro.DTO.Chapter.ChapterListDTO;
import com.novel.vippro.DTO.Comment.CommentDTO;
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
import com.novel.vippro.DTO.Novel.NovelDetailDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("mapper")
public class MapperFacade implements Mapper {

	@Autowired
	private NovelMapper novelMapper;
	@Autowired
	private ChapterMapper chapterMapper;
	@Autowired
	private CategoryMapper categoryMapper;
	@Autowired
	private BookmarkMapper bookmarkMapper;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private CommentMapper commentMapper;
	@Autowired
	private FileMetadataMapper fileMetadataMapper;
	@Autowired
	private ReaderSettingsMapper readerSettingsMapper;
	@Autowired
	private FeatureRequestMapper featureRequestMapper;
	@Autowired
	private TagMapper tagMapper;
	@Autowired
	private GenreMapper genreMapper;
	@Autowired
	private ReadingHistoryMapper readingHistoryMapper;
	@Autowired
	private NotificationMapper notificationMapper;
	@Autowired
	private GroupMemberMapper groupMemberMapper;
	@Autowired
	private MessageMapper messageMapper;
	@Autowired
	private GroupMapper groupMapper;
	@Autowired
	private RoleApprovalMapper roleApprovalMapper;
	@Autowired
	private ReviewMapper reviewMapper;
	@Autowired
	private ReportMapper reportMapper;
	@Autowired
	private RatingMapper ratingMapper;

	// Novel-related mappings
	@Override
	public NovelDTO NoveltoDTO(Novel novel) {
		return novelMapper.NoveltoDTO(novel);
	}

	@Override
	public NovelDetailDTO NoveltoNovelDetailDTO(Novel novel) {
		return novelMapper.NoveltoNovelDetailDTO(novel);
	}

	@Override
	public List<NovelDTO> NovelListtoDTOList(List<Novel> novels) {
		return novelMapper.NovelListtoDTOList(novels);
	}

	@Override
	public void updateNovelFromDTO(NovelDTO dto, Novel novel) {
		novelMapper.updateNovelFromDTO(dto, novel);
	}

	// Chapter-related mappings
	@Override
	public ChapterDTO ChaptertoDTO(Chapter chapter) {
		return chapterMapper.ChaptertoDTO(chapter);
	}

	@Override
	public ChapterDetailDTO ChaptertoChapterDetailDTO(Chapter chapter) {
		return chapterMapper.ChaptertoChapterDetailDTO(chapter);
	}

	@Override
	public ChapterDTO ChaptertoChapterDTO(Chapter chapter) {
		return chapterMapper.ChaptertoChapterDTO(chapter);
	}

	@Override
	public ChapterListDTO ChaptertoChapterListDTO(Chapter chapter) {
		return chapterMapper.ChaptertoChapterListDTO(chapter);
	}

	@Override
	public List<ChapterDTO> ChapterListtoDTOList(List<Chapter> chapters) {
		return chapterMapper.ChapterListtoDTOList(chapters);
	}

	@Override
	public void updateChapterFromDTO(ChapterDTO dto, Chapter chapter) {
		chapterMapper.updateChapterFromDTO(dto, chapter);
	}

	// Category-related mappings
	@Override
	public CategoryDTO CategorytoDTO(Category category) {
		return categoryMapper.CategorytoDTO(category);
	}

	@Override
	public Category DTOtoCategory(CategoryDTO categoryDTO) {
		return categoryMapper.DTOtoCategory(categoryDTO);
	}

	@Override
	public List<CategoryDTO> CategoryListtoDTOList(List<Category> categories) {
		return categoryMapper.CategoryListtoDTOList(categories);
	}

	@Override
	public void updateCategoryFromDTO(CategoryDTO dto, Category category) {
		categoryMapper.updateCategoryFromDTO(dto, category);
	}

	// Implement other methods, delegating to respective mappers
	// User-related mappings
	@Override
	public UserDTO UsertoUserDTO(User user) {
		return userMapper.UsertoUserDTO(user);
	}

	@Override
	public User DTOtoUser(UserDTO userDTO) {
		return userMapper.DTOtoUser(userDTO);
	}

	@Override
	public void updateUserFromDTO(UserDTO dto, User user) {
		userMapper.updateUserFromDTO(dto, user);
	}

	// Comment-related mappings
	@Override
	public CommentDTO CommenttoDTO(Comment comment) {
		return commentMapper.CommenttoDTO(comment);
	}

	@Override
	public void updateCommentFromDTO(CommentDTO dto, Comment comment) {
		commentMapper.updateCommentFromDTO(dto, comment);
	}

	// ReaderSettings-related mappings
	@Override
	public ReaderSettingsDTO ReaderSettingsToReaderSettingsDTO(ReaderSettings readerSettings) {
		return readerSettingsMapper.ReaderSettingsToReaderSettingsDTO(readerSettings);
	}

	@Override
	public void updateReaderSettingsFromDTO(ReaderSettingsUpdateDTO dto, ReaderSettings settings) {
		readerSettingsMapper.updateReaderSettingsFromDTO(dto, settings);
	}

	// FeatureRequest-related mappings
	@Override
	public FeatureRequestDTO RequesttoRequestDTO(FeatureRequest request) {
		return featureRequestMapper.RequesttoRequestDTO(request);
	}

	@Override
	public FeatureRequest RequestDTOtoRequest(FeatureRequestDTO requestDTO) {
		return featureRequestMapper.RequestDTOtoRequest(requestDTO);
	}

	@Override
	public FeatureRequest CreateFeatureRequestDTOtoFeatureRequest(CreateFeatureRequestDTO requestDTO) {
		return featureRequestMapper.CreateFeatureRequestDTOtoFeatureRequest(requestDTO);
	}

	@Override
	public void updateFeatureRequestFromDTO(FeatureRequestDTO dto, FeatureRequest featureRequest) {
		featureRequestMapper.updateFeatureRequestFromDTO(dto, featureRequest);
	}

	// Tag-related mappings
	@Override
	public TagDTO TagtoDTO(Tag tag) {
		return tagMapper.TagtoDTO(tag);
	}

	@Override
	public Tag DTOtoTag(TagDTO tagDTO) {
		return tagMapper.DTOtoTag(tagDTO);
	}

	@Override
	public List<TagDTO> TagListtoDTOList(List<Tag> tags) {
		return tagMapper.TagListtoDTOList(tags);
	}

	@Override
	public void updateTagFromDTO(TagDTO dto, Tag tag) {
		tagMapper.updateTagFromDTO(dto, tag);
	}

	// Genre-related mappings
	@Override
	public GenreDTO GenretoDTO(Genre genre) {
		return genreMapper.GenretoDTO(genre);
	}

	@Override
	public Genre DTOtoGenre(GenreDTO genreDTO) {
		return genreMapper.DTOtoGenre(genreDTO);
	}

	@Override
	public List<GenreDTO> GenreListtoDTOList(List<Genre> genres) {
		return genreMapper.GenreListtoDTOList(genres);
	}

	@Override
	public void updateGenreFromDTO(GenreDTO dto, Genre genre) {
		genreMapper.updateGenreFromDTO(dto, genre);
	}

	// ReadingHistory-related mappings
	@Override
	public ReadingHistoryDTO ReadingHistorytoDTO(ReadingHistory readingHistory) {
		return readingHistoryMapper.ReadingHistorytoDTO(readingHistory);
	}

	@Override
	public List<ReadingHistoryDTO> ReadingHistoryListtoDTOList(List<ReadingHistory> readingHistories) {
		return readingHistoryMapper.ReadingHistoryListtoDTOList(readingHistories);
	}

	// Notification-related mappings
	@Override
	public NotificationDTO NotificationtoDTO(Notification notification) {
		return notificationMapper.NotificationtoDTO(notification);
	}

	@Override
	public Notification DTOtoNotification(NotificationDTO notificationDTO) {
		return notificationMapper.DTOtoNotification(notificationDTO);
	}

	@Override
	public void updateNotificationFromDTO(NotificationDTO dto, Notification notification) {
		notificationMapper.updateNotificationFromDTO(dto, notification);
	}

	// GroupMember-related mappings
	@Override
	public GroupMemberDTO GroupMembertoDTO(GroupMember groupMember) {
		return groupMemberMapper.GroupMembertoDTO(groupMember);
	}

	@Override
	public GroupMember DTOtoGroupMember(GroupMemberDTO groupMemberDTO) {
		return groupMemberMapper.DTOtoGroupMember(groupMemberDTO);
	}

	@Override
	public void updateGroupMemberFromDTO(GroupMemberDTO dto, GroupMember groupMember) {
		groupMemberMapper.updateGroupMemberFromDTO(dto, groupMember);
	}

	// Message-related mappings
	@Override
	public MessageDTO MessagetoDTO(Message message) {
		return messageMapper.MessagetoDTO(message);
	}

	@Override
	public Message DTOtoMessage(MessageDTO messageDTO) {
		return messageMapper.DTOtoMessage(messageDTO);
	}

	@Override
	public Message CreateDTOtoMessage(CreateMessageDTO messageDTO) {
		return messageMapper.CreateDTOtoMessage(messageDTO);
	}

	@Override
	public void updateMessageFromDTO(MessageDTO dto, Message message) {
		messageMapper.updateMessageFromDTO(dto, message);
	}

	// Group-related mappings
	@Override
	public GroupDTO GroupToDTO(Group group) {
		return groupMapper.GroupToDTO(group);
	}

	@Override
	public Group DTOtoGroup(GroupDTO groupDTO) {
		return groupMapper.DTOtoGroup(groupDTO);
	}

	@Override
	public Group CreateDTOtoGroup(CreateGroupDTO groupDTO) {
		return groupMapper.CreateDTOtoGroup(groupDTO);
	}

	@Override
	public void updateGroupFromDTO(UpdateGroupDTO dto, Group group) {
		groupMapper.updateGroupFromDTO(dto, group);
	}

	// RoleApprovalRequest-related mappings
	@Override
	public RoleApprovalDTO RoleApprovalRequestToDTO(RoleApprovalRequest roleApprovalRequest) {
		return roleApprovalMapper.RoleApprovalRequestToDTO(roleApprovalRequest);
	}

	@Override
	public RoleApprovalRequest DTOtoRoleApprovalRequest(RoleApprovalDTO roleApprovalDTO) {
		return roleApprovalMapper.DTOtoRoleApprovalRequest(roleApprovalDTO);
	}

	@Override
	public void updateRoleApprovalRequestFromDTO(RoleApprovalDTO dto, RoleApprovalRequest roleApprovalRequest) {
		roleApprovalMapper.updateRoleApprovalRequestFromDTO(dto, roleApprovalRequest);
	}

	@Override
	public ReviewDTO ReviewtoDTO(Review review) {
		return reviewMapper.ReviewtoDTO(review);
	}

	@Override
	public Review DTOtoReview(ReviewDTO reviewDTO) {
		return reviewMapper.DTOtoReview(reviewDTO);
	}

	@Override
	public void updateReviewFromDTO(ReviewDTO dto, Review review) {
		reviewMapper.updateReviewFromDTO(dto, review);
	}

	@Override
	public ReportDTO ReporttoDTO(Report report) {
		return reportMapper.ReporttoDTO(report);
	}

	@Override
	public Report DTOtoReport(ReportDTO reportDTO) {
		return reportMapper.DTOtoReport(reportDTO);
	}

	@Override
	public void updateReportFromDTO(ReportDTO dto, Report report) {
		reportMapper.updateReportFromDTO(dto, report);
	}

	// Rating-related mappings
	@Override
	public RatingDTO RatingtoDTO(Rating rating) {
		return ratingMapper.RatingtoDTO(rating);
	}

	@Override
	public Rating DTOtoRating(RatingDTO ratingDTO) {
		return ratingMapper.DTOtoRating(ratingDTO);
	}

	@Override
	public void updateRatingFromDTO(RatingDTO dto, Rating rating) {
		ratingMapper.updateRatingFromDTO(dto, rating);
	}

	@Override
	public BookmarkDTO BookmarktoDTO(Bookmark bookmark) {
		return bookmarkMapper.BookmarktoDTO(bookmark);
	}

	@Override
	public Bookmark DTOtoBookmark(BookmarkDTO bookmarkDTO) {
		return bookmarkMapper.DTOtoBookmark(bookmarkDTO);
	}

	@Override
	public void updateBookmarkFromDTO(BookmarkDTO dto, Bookmark bookmark) {
		bookmarkMapper.updateBookmarkFromDTO(dto, bookmark);
	}

	@Override
	public FileMetadataDTO FileMetadataToDTO(FileMetadata metadata) {
		return fileMetadataMapper.FileMetadataToDTO(metadata);
	}

	@Override
	public FileMetadata DTOToFileMetadata(FileMetadataDTO dto) {
		return fileMetadataMapper.DTOToFileMetadata(dto);
	}

	@Override
	public void updateFileMetadataFromDTO(FileMetadataDTO dto, FileMetadata metadata) {
		fileMetadataMapper.updateFileMetadataFromDTO(dto, metadata);
	}

}