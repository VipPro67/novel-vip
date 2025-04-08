export interface Novel {
  id: number;
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
  pageable: {
    sort: {
      unsorted: boolean;
      sorted: boolean;
      empty: boolean;
    };
    pageNumber: number;
    pageSize: number;
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  first: boolean;
  numberOfElements: number;
  size: number;
  number: number;
  sort: {
    unsorted: boolean;
    sorted: boolean;
    empty: boolean;
  };
  empty: boolean;
}
