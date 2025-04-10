import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

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

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  statusCode: number;
}

@Injectable({
  providedIn: 'root'
})
export class NovelService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  getNovels(page: number = 0, size: number = 10): Observable<ApiResponse<PaginatedResponse<Novel>>> {
    return this.http.get<ApiResponse<PaginatedResponse<Novel>>>(`${this.apiUrl}/novels?page=${page}&size=${size}`);
  }

  getNovelById(id: string): Observable<ApiResponse<Novel>> {
    return this.http.get<ApiResponse<Novel>>(`${this.apiUrl}/novels/${id}`);
  }

  getChaptersByNovel(novelId: string, page: number = 0, size: number = 10): Observable<ApiResponse<PaginatedResponse<Chapter>>> {
    return this.http.get<ApiResponse<PaginatedResponse<Chapter>>>(`${this.apiUrl}/chapters/novel/${novelId}?page=${page}&size=${size}`);
  }

  getChapterById(id: string): Observable<ApiResponse<ChapterDetail>> {
    return this.http.get<ApiResponse<ChapterDetail>>(`${this.apiUrl}/chapters/${id}`);
  }
} 