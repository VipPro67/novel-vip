export interface ChapterListDTO {
  id: string;
  chapterNumber: number;
  title: string;
  novelId: string;
  novelTitle: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChapterDetailDTO extends ChapterListDTO {
  content: string;
  audioContent?: string;
}

export interface ChapterCreateDTO {
  chapterNumber: number;
  title: string;
  content: string;
  novelId: string;
}

export interface PageResponseChapterListDTO {
  totalPages: number;
  totalElements: number;
  pageNumber: number;
  pageSize: number;
  content: ChapterListDTO[];
} 