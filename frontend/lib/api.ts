const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081";

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    statusCode: number;
}

export interface PageResponse<T> {
    totalPages: number;
    totalElements: number;
    pageNumber: number;
    pageSize: number;
    content: T[];
}

export interface User {
    id: string;
    username: string;
    email: string;
    fullName?: string;
    roles: {id: string; name: string}[];
}

export interface Novel {
    id: string;
    title: string;
    description: string;
    slug: string;
    author: string;
    coverImage?: FileMetadata;
    status: string;
    categories?: Category[];
    genres?: Genre[];
    tags?: Tag[];
    totalChapters: number;
    views: number;
    rating: number;
    updatedAt: string;
}

export interface FileMetadata {
    id: string;
    contentType: string;
    publicId: string;
    fileUrl: string;
    uploadedAt: string;
    lastModifiedAt: string;
    fileName: string;
    type: string;
    size: number;
}

export interface Chapter {
    id: string;
    chapterNumber: number;
    title: string;
    novelId: string;
    novelTitle: string;
    createdAt: string;
    updatedAt: string;
}

export interface ChapterDetail extends Chapter {
    jsonUrl?: string;
    audioUrl?: string;
}

export interface Category {
    id: string;
    name: string;
    description?: string;
}

export interface Genre {
    id: string;
    name: string;
    description?: string;
}

export interface Tag {
    id: string;
    name: string;
    description?: string;
}

export interface Comment {
    id: string;
    content: string;
    userId: string;
    username: string;
    novelId?: string;
    chapterId?: string;
    parentId?: string | null;
    replies?: Comment[];
    createdAt: string;
    updatedAt: string;
    edited?: boolean;
}

export interface Bookmark {
    id: string;
    userId: string;
    chapterId: string;
    novelId: string;
    chapterTitle: string;
    novelTitle: string;
    note?: string;
    progress: number;
    createdAt: string;
    updatedAt: string;
}

export interface ReadingHistory {
    id: string;
    userId: string;
    novelId: string;
    novelTitle: string;
    novelCover?: string;
    chapterId: string;
    chapterTitle: string;
    chapterNumber: number;
    progress: number;
    readingTime: number;
    lastReadAt: string;
    createdAt: string;
}

export interface Rating {
    id: string;
    userId: string;
    username: string;
    novelId: string;
    novelTitle: string;
    score: number;
    review?: string;
    createdAt: string;
    updatedAt: string;
}

export interface Review {
    id: string;
    novelId: string;
    novelTitle: string;
    userId: string;
    username: string;
    userAvatar?: string;
    title: string;
    content: string;
    rating: number;
    helpfulVotes: number;
    unhelpfulVotes: number;
    edited: boolean;
    verifiedPurchase: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface Notification {
    id: string;
    userId: string;
    title: string;
    message: string;
    read: boolean;
    type:
    | "SYSTEM"
    | "BOOK_UPDATE"
    | "CHAPTER_UPDATE"
    | "COMMENT"
    | "LIKE"
    | "FOLLOW"
    | "MESSAGE";
    referenceId?: string;
    createdAt: string;
}

export interface ReaderSettings {
    id: string;
    userId: string;
    fontSize: number;
    fontFamily: string;
    lineHeight: number;
    theme: string;
    marginSize: number;
    paragraphSpacing: number;
    autoScroll: boolean;
    autoScrollSpeed: boolean;
    keepScreenOn: boolean;
    showProgress: boolean;
    showChapterTitle: boolean;
    showTime: boolean;
    showBattery: boolean;
}

export interface RoleRequest {
    id: string;
    userId: string;
    username: string;
    email: string;
    requestedRole: string;
    reason: string;
    status: "PENDING" | "APPROVED" | "REJECTED";
    createdAt: string;
    updatedAt: string;
    processedBy?: string;
    rejectReason?: string;
}

class ApiClient {
    private baseURL: string;
    private token: string | null = null;

    constructor(baseURL: string) {
        this.baseURL = baseURL;
        if (typeof window !== "undefined") {
            this.token = localStorage.getItem("token");
        }
    }

    setToken(token: string) {
        this.token = token;
        if (typeof window !== "undefined") {
            localStorage.setItem("token", token);
        }
    }

    clearToken() {
        this.token = null;
        if (typeof window !== "undefined") {
            localStorage.removeItem("token");
        }
    }

    getToken(): string | null {
        return this.token;
    }

    async request<T>(
        endpoint: string,
        options: RequestInit = {}
    ): Promise<ApiResponse<T>> {
        const url = `${this.baseURL}${endpoint}`;

        let headers: HeadersInit = { ...options.headers };

        const isFormData = options.body instanceof FormData;
        if (!isFormData) {
            headers["Content-Type"] = "application/json";
        }

        // Add Authorization header if token exists
        if (this.token) {
            headers.Authorization = `Bearer ${this.token}`;
        }

        try {
            const response = await fetch(url, {
                ...options,
                headers,
            });

            console.log(`API Response: ${response.status} ${response.statusText}`);

            if (!response.ok) {
                // Handle different error status codes
                if (response.status === 401) {
                    // Unauthorized - token might be expired
                    this.clearToken();
                    throw new Error("Authentication failed. Please login again.");
                } else if (response.status === 403) {
                    // Forbidden - insufficient permissions
                    throw new Error("Access denied. Insufficient permissions.");
                } else if (response.status === 404) {
                    throw new Error("Resource not found.");
                } else if (response.status >= 500) {
                    throw new Error("Server error. Please try again later.");
                } else {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
            }

            const data = await response.json();
            return data;
        } catch (error) {
            console.error("API Request Error:", error);
            throw error;
        }
    }

    // Auth endpoints
    async login(email: string, password: string) {
        const response = await this.request<{
            id: string;
            username: string;
            email: string;
            roles: string[];
            tokenType: string;
            accessToken: string;
        }>("/api/auth/signin", {
            method: "POST",
            body: JSON.stringify({ email, password }),
        });

        if (response.success && response.data.accessToken) {
            this.setToken(response.data.accessToken);
        }

        return response;
    }

    async register(username: string, email: string, password: string) {
        return this.request("/api/auth/signup", {
            method: "POST",
            body: JSON.stringify({ username, email, password }),
        });
    }

    async getUserProfile() {
        return this.request<User>("/api/users/profile");
    }

    async updateUserProfile(data: { fullName?: string; avatar?: string }) {
        return this.request<User>("/api/users/profile", {
            method: "PUT",
            body: JSON.stringify(data),
        });
    }

    async changePassword(
        oldPassword: string,
        newPassword: string,
        confirmPassword: string
    ) {
        return this.request<void>("/api/users/change-password", {
            method: "PUT",
            body: JSON.stringify({ oldPassword, newPassword, confirmPassword }),
        });
    }

    // Novel endpoints
    async getNovels(
        params: {
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
            search?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();

        // Set default values
        const page = params.page ?? 0;
        const size = params.size ?? 20;
        const sortBy = params.sortBy ?? "updatedAt";
        const sortDir = params.sortDir ?? "desc";

        // Add pagination parameters
        searchParams.append("page", page.toString());
        searchParams.append("size", size.toString());
        searchParams.append("sortBy", sortBy);
        searchParams.append("sortDir", sortDir);

        // Add search parameter if provided
        if (params.search && params.search.trim()) {
            searchParams.append("search", params.search.trim());
        }

        return this.request<PageResponse<Novel>>(`/api/novels?${searchParams}`);
    }

    async getNovelById(id: string) {
        return this.request<Novel>(`/api/novels/${id}`);
    }

    async getNovelBySlug(slug: string) {
        return this.request<Novel>(`/api/novels/slug/${slug}`);
    }

    async createNovel(data: {
        title: string;
        slug: string;
        description: string;
        author: string;
        coverImage?: string;
        status: string;
        categories: string[];
        genres?: string[];
        tags?: string[];
    }) {
        return this.request<Novel>("/api/novels", {
            method: "POST",
            body: JSON.stringify(data),
        });
    }

    async updateNovel(
        id: string,
        data: {
            title: string;
            slug: string;
            description: string;
            author: string;
            coverImage?: string;
            status: string;
            categories: string[];
            genres?: string[];
            tags?: string[];
        }
    ) {
        return this.request<Novel>(`/api/novels/${id}`, {
            method: "PUT",
            body: JSON.stringify(data),
        });
    }

    async deleteNovel(id: string) {
        return this.request<void>(`/api/novels/${id}`, {
            method: "DELETE",
        });
    }

    async searchNovels(
        params: {
            keyword?: string;
            title?: string;
            author?: string;
            category?: string;
            genre?: string;
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null) {
                const stringValue = value.toString().trim();
                if (stringValue.length > 0) {
                    searchParams.append(key, stringValue);
                }
            }
        });

        return this.request<PageResponse<Novel>>(
            `/api/novels/search?${searchParams.toString()}`
        );
    }

    async getCategories() {
        return this.request<Category[]>("/api/novels/categories");
    }

    async getGenres() {
        return this.request<Genre[]>("/api/novels/genres");
    }

    async getTags() {
        return this.request<Tag[]>("/api/novels/tags");
    }

    async getNovelsByCategory(
        categoryId: string,
        params: {
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<Novel>>(
            `/api/novels/category/${categoryId}?${searchParams}`
        );
    }

    async getHotNovels(
        params: {
            page?: number;
            size?: number;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<Novel>>(`/api/novels/hot?${searchParams}`);
    }

    async getTopRatedNovels(
        params: {
            page?: number;
            size?: number;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<Novel>>(
            `/api/novels/top-rated?${searchParams}`
        );
    }

    async getLatestNovels(
        params: {
            page?: number;
            size?: number;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<Novel>>(
            `/api/novels/latest-updates?${searchParams}`
        );
    }

    // Chapter endpoints
    async getChaptersByNovel(
        novelId: string,
        params: {
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<Chapter>>(
            `/api/chapters/novel/${novelId}?${searchParams}`
        );
    }

    async getChapterById(id: string) {
        return this.request<ChapterDetail>(`/api/chapters/${id}`);
    }

    async getChapterByNumber(novelId: string, chapterNumber: number) {
        return this.request<ChapterDetail>(
            `/api/chapters/novel/${novelId}/chapter/${chapterNumber}`
        );
    }

    async createChapter(data: {
        title: string;
        chapterNumber: number;
        contentHtml: string;
        novelId: string;
    }) {
        return this.request<Chapter>("/api/chapters", {
            method: "POST",
            body: JSON.stringify(data),
        });
    }

    async updateChapter(
        id: string,
        data: {
            title: string;
            chapterNumber: number;
            content: string;
            novelId: string;
        }
    ) {
        return this.request<Chapter>(`/api/chapters/${id}`, {
            method: "PUT",
            body: JSON.stringify(data),
        });
    }

    async deleteChapter(id: string) {
        return this.request<void>(`/api/chapters/${id}`, {
            method: "DELETE",
        });
    }

    async getChapterJsonMetadata(chapterId: string) {
        return this.request<FileMetadata>(
            `/api/chapters/${chapterId}/json-metadata`
        );
    }

    // Reading History endpoints
    async addReadingHistory(chapterId: string) {
        return this.request<ReadingHistory>(
            `/api/reading-history/chapter/${chapterId}`,
            {
                method: "POST",
            }
        );
    }

    async updateReadingProgress(
        novelId: string,
        chapterId: string,
        progress: number,
        readingTime = 0
    ) {
        const params = new URLSearchParams({
            progress: progress.toString(),
            readingTime: readingTime.toString(),
        });
        return this.request<ReadingHistory>(
            `/api/reading-history/novel/${novelId}/chapter/${chapterId}/progress?${params}`,
            { method: "POST" }
        );
    }

    async getReadingHistory(
        params: {
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<ReadingHistory>>(
            `/api/reading-history?${searchParams}`
        );
    }

    async getRecentlyRead(limit = 5) {
        return this.request<Novel[]>(`/api/reading-history/recent?limit=${limit}`);
    }

    // Favorites endpoints
    async getFavorites(
        params: {
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<Novel>>(`/api/favorites?${searchParams}`);
    }

    async addToFavorites(novelId: string) {
        return this.request<void>(`/api/favorites/${novelId}`, {
            method: "POST",
        });
    }

    async removeFromFavorites(novelId: string) {
        return this.request<void>(`/api/favorites/${novelId}`, {
            method: "DELETE",
        });
    }

    async checkFavoriteStatus(novelId: string) {
        return this.request<boolean>(`/api/favorites/${novelId}/status`);
    }

    // Bookmarks endpoints
    async getBookmarks(
        params: {
            page?: number;
            size?: number;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<Bookmark>>(
            `/api/bookmarks?${searchParams}`
        );
    }

    async createBookmark(data: {
        chapterId: string;
        novelId: string;
        note?: string;
        progress?: number;
    }) {
        return this.request<Bookmark>("/api/bookmarks", {
            method: "POST",
            body: JSON.stringify(data),
        });
    }

    async updateBookmark(
        id: string,
        data: {
            note?: string;
            progress?: number;
        }
    ) {
        return this.request<Bookmark>(`/api/bookmarks/${id}`, {
            method: "PUT",
            body: JSON.stringify(data),
        });
    }

    async deleteBookmark(id: string) {
        return this.request<void>(`/api/bookmarks/${id}`, {
            method: "DELETE",
        });
    }

    // Ratings endpoints
    async rateNovel(novelId: string, score: number, review?: string) {
        return this.request<Rating>(`/api/ratings/novel/${novelId}`, {
            method: "POST",
            body: JSON.stringify({ score, review }),
        });
    }

    async getUserRating(novelId: string) {
        return this.request<Rating>(`/api/ratings/novel/${novelId}/user`);
    }

    // Comments endpoints
    async getNovelComments(
        novelId: string,
        params: {
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<Comment>>(
            `/api/comments/novel/${novelId}?${searchParams}`
        );
    }

    async getChapterComments(
        chapterId: string,
        params: {
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<Comment>>(
            `/api/comments/chapter/${chapterId}?${searchParams}`
        );
    }

    async addComment(data: {
        content: string;
        novelId?: string;
        chapterId?: string;
        parentId?: string;
    }) {
        return this.request<Comment>("/api/comments", {
            method: "POST",
            body: JSON.stringify(data),
        });
    }

    async updateComment(commentId: string, data: { content: string }) {
        return this.request<Comment>(`/api/comments/${commentId}`, {
            method: "PUT",
            body: JSON.stringify(data),
        });
    }

    async deleteComment(commentId: string) {
        return this.request<void>(`/api/comments/${commentId}`, {
            method: "DELETE",
        });
    }

    // Notifications endpoints
    async getNotifications(
        params: {
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<Notification>>(
            `/api/notifications?${searchParams}`
        );
    }

    async markNotificationAsRead(notificationId: string) {
        return this.request<Notification>(
            `/api/notifications/${notificationId}/read`,
            {
                method: "PUT",
            }
        );
    }

    async markAllNotificationsAsRead() {
        return this.request<void>("/api/notifications/read-all", {
            method: "PUT",
        });
    }

    async getUnreadNotificationCount() {
        return this.request<ApiResponse<number>>("/api/notifications/unread/count");
    }

    // Reader Settings endpoints
    async getReaderSettings() {
        return this.request<ReaderSettings>("/api/reader-settings");
    }

    async updateReaderSettings(settings: Partial<ReaderSettings>) {
        return this.request<ReaderSettings>("/api/reader-settings", {
            method: "PUT",
            body: JSON.stringify(settings),
        });
    }

    async getThemeOptions() {
        return this.request<string[]>("/api/reader-settings/themes");
    }

    async getFontOptions() {
        return this.request<string[]>("/api/reader-settings/fonts");
    }

    // Admin endpoints
    async getAllUsers(
        params: {
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
            search?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();

        // Set default values
        const page = params.page ?? 0;
        const size = params.size ?? 10;
        const sortBy = params.sortBy ?? "id";
        const sortDir = params.sortDir ?? "asc";

        // Add pagination parameters
        searchParams.append("page", page.toString());
        searchParams.append("size", size.toString());
        searchParams.append("sortBy", sortBy);
        searchParams.append("sortDir", sortDir);

        // Add search parameter if provided
        if (params.search && params.search.trim()) {
            searchParams.append("search", params.search.trim());
        }

        return this.request<PageResponse<User>>(`/api/users?${searchParams}`);
    }

    async updateUserRoles(userId: string, roles: string[]) {
        return this.request<User>(`/api/users/${userId}/roles`, {
            method: "PUT",
            body: JSON.stringify(roles),
        });
    }

    async deleteUser(userId: string) {
        return this.request<void>(`/api/users/${userId}`, {
            method: "DELETE",
        });
    }

    // Role Approval endpoints
    async requestRole(requestedRole: string, reason: string) {
        return this.request<RoleRequest>("/api/role-approval/request", {
            method: "POST",
            body: JSON.stringify({ requestedRole, reason }),
        });
    }

    async getPendingRoleRequests(
        params: {
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();

        // Set default values
        const page = params.page ?? 0;
        const size = params.size ?? 10;
        const sortBy = params.sortBy ?? "createdAt";
        const sortDir = params.sortDir ?? "desc";

        // Add pagination parameters
        searchParams.append("page", page.toString());
        searchParams.append("size", size.toString());
        searchParams.append("sortBy", sortBy);
        searchParams.append("sortDir", sortDir);

        return this.request<PageResponse<RoleRequest>>(
            `/api/role-approval/pending?${searchParams}`
        );
    }

    async getMyRoleRequests(
        params: {
            page?: number;
            size?: number;
            sortBy?: string;
            sortDir?: string;
        } = {}
    ) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined) {
                searchParams.append(key, value.toString());
            }
        });

        return this.request<PageResponse<RoleRequest>>(
            `/api/role-approval/my-requests?${searchParams}`
        );
    }

    async approveRoleRequest(requestId: string) {
        return this.request<RoleRequest>(
            `/api/role-approval/approve/${requestId}`,
            {
                method: "POST",
            }
        );
    }

    async rejectRoleRequest(requestId: string) {
        return this.request<RoleRequest>(`/api/role-approval/reject/${requestId}`, {
            method: "POST",
        });
    }

    // File upload method
    async uploadFile(
        file: File,
        type?: string
    ): Promise<ApiResponse<FileMetadata>> {
        const formData = new FormData();
        formData.append("file", file);

        if (type) {
            formData.append("type", type);
        }

        const headers: HeadersInit = {};
        if (this.token) {
            headers.Authorization = `Bearer ${this.token}`;
        }

        const response = await fetch(`${this.baseURL}/api/files/upload`, {
            method: "POST",
            headers,
            body: formData,
        });

        if (!response.ok) {
            throw new Error(`Upload failed: ${response.statusText}`);
        }

        return response.json();
    }
}

export const api = new ApiClient(API_BASE_URL);
