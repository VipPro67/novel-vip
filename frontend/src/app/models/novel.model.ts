export interface Novel {
  id: string;
  title: string;
  description: string;
  author: string;
  coverImage: string;
  status: "completed" | "ongoing";
  categories: string[];
  totalChapters: number;
  views: number;
  rating: number;
  chapters: Chapter[];
  updatedAt: string;
}

export interface Chapter {
  id: string;
  title: string;
  chapterNumber: number;
  updatedAt: string;
  views: number;
}

export interface ChapterDetail {
  id: string;
  chapterNumber: number;
  title: string;
  novelId: string;
  novelTitle: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    unpaged: boolean;
    paged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
} 