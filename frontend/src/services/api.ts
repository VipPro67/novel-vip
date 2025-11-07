import { ApiClient, API_BASE_URL } from "./api-client"
import { createAdminCommentsApi } from "./api/admin-comments"
import { createAdminNotificationsApi } from "./api/admin-notifications"
import { createAdminReportsApi } from "./api/admin-reports"
import { createAdminReviewsApi } from "./api/admin-reviews"
import { createAdminUsersApi } from "./api/admin-users"
import { createAuthApi } from "./api/auth"
import { createBookmarksApi } from "./api/bookmarks"
import { createChapterApi } from "./api/chapters"
import { createCommentsApi } from "./api/comments"
import { createFavoritesApi } from "./api/favorites"
import { createFilesApi } from "./api/files"
import { createGroupsApi } from "./api/groups"
import { createMessagesApi } from "./api/messages"
import { createNotificationsApi } from "./api/notifications"
import { createNovelApi } from "./api/novels"
import { createRatingsApi } from "./api/ratings"
import { createReaderSettingsApi } from "./api/reader-settings"
import { createReportsApi } from "./api/reports"
import { createReviewsApi } from "./api/reviews"
import { createRoleRequestsApi } from "./api/role-requests"
import { createVideosApi } from "./api/videos"

type ApiModules =
  & ReturnType<typeof createAuthApi>
  & ReturnType<typeof createNovelApi>
  & ReturnType<typeof createChapterApi>
  & ReturnType<typeof createFavoritesApi>
  & ReturnType<typeof createBookmarksApi>
  & ReturnType<typeof createRatingsApi>
  & ReturnType<typeof createCommentsApi>
  & ReturnType<typeof createNotificationsApi>
  & ReturnType<typeof createReaderSettingsApi>
  & ReturnType<typeof createAdminUsersApi>
  & ReturnType<typeof createReportsApi>
  & ReturnType<typeof createRoleRequestsApi>
  & ReturnType<typeof createVideosApi>
  & ReturnType<typeof createFilesApi>
  & ReturnType<typeof createReviewsApi>
  & ReturnType<typeof createAdminNotificationsApi>
  & ReturnType<typeof createAdminCommentsApi>
  & ReturnType<typeof createAdminReviewsApi>
  & ReturnType<typeof createAdminReportsApi>
  & ReturnType<typeof createMessagesApi>
  & ReturnType<typeof createGroupsApi>

type ApiService = ApiClient & ApiModules

const apiClient = new ApiClient(API_BASE_URL) as ApiService

const factories = [
  createAuthApi,
  createNovelApi,
  createChapterApi,
  createFavoritesApi,
  createBookmarksApi,
  createRatingsApi,
  createCommentsApi,
  createNotificationsApi,
  createReaderSettingsApi,
  createAdminUsersApi,
  createReportsApi,
  createRoleRequestsApi,
  createVideosApi,
  createFilesApi,
  createReviewsApi,
  createAdminNotificationsApi,
  createAdminCommentsApi,
  createAdminReviewsApi,
  createAdminReportsApi,
  createMessagesApi,
  createGroupsApi,
] as const

factories.forEach((factory) => {
  Object.assign(apiClient, factory(apiClient))
})

export const api = apiClient
export type { FeatureRequest } from "@/models/feature-request"
