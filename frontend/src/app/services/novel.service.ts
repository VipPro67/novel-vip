import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { ApiResponse } from "../models/api-response.model";
import { PaginatedResponse } from "../models/paginated-response.model";
import { Novel } from "../models/novel.model";
import { Chapter, ChapterDetail } from "../models/chapter.model";

@Injectable({
  providedIn: "root",
})
export class NovelService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getNovels(
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<PaginatedResponse<Novel>>> {
    return this.http.get<ApiResponse<PaginatedResponse<Novel>>>(
      `${this.apiUrl}/api/novels?page=${page}&size=${size}`
    );
  }

  searchNovels(
    keyword: string,
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<PaginatedResponse<Novel>>> {
    const params = new HttpParams()
      .set("keyword", keyword)
      .set("page", page.toString())
      .set("size", size.toString());

    return this.http.get<ApiResponse<PaginatedResponse<Novel>>>(
      `${this.apiUrl}/api/novels/search`,
      { params }
    );
  }

  getNovelById(id: string): Observable<ApiResponse<Novel>> {
    return this.http.get<ApiResponse<Novel>>(`${this.apiUrl}/api/novels/${id}`);
  }

  getChaptersByNovel(
    novelId: string,
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<PaginatedResponse<Chapter>>> {
    return this.http.get<ApiResponse<PaginatedResponse<Chapter>>>(
      `${this.apiUrl}/api/novels/${novelId}/chapters?page=${page}&size=${size}`
    );
  }

  getChapterById(id: string): Observable<ApiResponse<ChapterDetail>> {
    return this.http.get<ApiResponse<ChapterDetail>>(
      `${this.apiUrl}/api/chapters/${id}`
    );
  }

  getChapterByNovelIdAndNumber(
    novelId: string,
    chapterNumber: number
  ): Observable<ApiResponse<ChapterDetail>> {
    return this.http.get<ApiResponse<ChapterDetail>>(
      `${this.apiUrl}/api/chapters/novel/${novelId}/chapter/${chapterNumber}`
    );
  }

  createNovel(novel: Partial<Novel>): Observable<ApiResponse<Novel>> {
    return this.http.post<ApiResponse<Novel>>(`${this.apiUrl}/api/novels`, novel);
  }

  updateNovel(id: string, novel: Partial<Novel>): Observable<ApiResponse<Novel>> {
    return this.http.put<ApiResponse<Novel>>(`${this.apiUrl}/api/novels/${id}`, novel);
  }

  deleteNovel(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/api/novels/${id}`);
  }

  createChapter(novelId: string, chapter: Partial<ChapterDetail>): Observable<ApiResponse<ChapterDetail>> {
    return this.http.post<ApiResponse<ChapterDetail>>(
      `${this.apiUrl}/api/novels/${novelId}/chapters`,
      chapter
    );
  }

  updateChapter(novelId: string, chapterId: string, chapter: Partial<ChapterDetail>): Observable<ApiResponse<ChapterDetail>> {
    return this.http.put<ApiResponse<ChapterDetail>>(
      `${this.apiUrl}/api/novels/${novelId}/chapters/${chapterId}`,
      chapter
    );
  }

  deleteChapter(novelId: string, chapterId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.apiUrl}/api/novels/${novelId}/chapters/${chapterId}`
    );
  }
}
