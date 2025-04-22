export interface NovelDTO {
  id: string;
  title: string;
  description: string;
  author: string;
  coverImage: string;
  status: 'ONGOING' | 'COMPLETED' | 'HIATUS' | 'DROPPED';
  categories: string[];
  genres: string[];
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface NovelCreateDTO {
  title: string;
  description: string;
  status: 'ONGOING' | 'COMPLETED' | 'HIATUS' | 'DROPPED';
  categories: string[];
  genres: string[];
  tags: string[];
}

export interface NovelUpdateDTO {
  title?: string;
  description?: string;
  status?: 'ONGOING' | 'COMPLETED' | 'HIATUS' | 'DROPPED';
  categories?: string[];
  genres?: string[];
  tags?: string[];
}

export interface PageResponseNovelDTO {
  content: NovelDTO[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
} 