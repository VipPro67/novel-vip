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
