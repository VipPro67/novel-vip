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
