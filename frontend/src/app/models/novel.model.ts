export interface Novel {
    id: number;
    title: string;
    description: string;
    author: string;
    coverImage: string;
    status: string;
    categories: string[];
    totalChapters: number;
    views: number;
    rating: number;
    chapters: Chapter[];
}

export interface Chapter {
    id: number;
    title: string;
    content: string;
    chapterNumber: number;
    views: number;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
} 