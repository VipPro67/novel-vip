import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { Chapter, PaginatedResponse, ApiResponse } from "../services/novel.service"
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: "root",
})
export class ChapterService {
  private apiUrl = `${environment.apiUrl}/api/chapters`;

  constructor(private http: HttpClient) {}

  getChaptersByNovel(
    novelId: string,
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<PaginatedResponse<Chapter>>> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());
    return this.http.get<ApiResponse<PaginatedResponse<Chapter>>>(
      `${this.apiUrl}/novel/${novelId}`,
      { params }
    );
  }

  getChapterByNovelIdAndNumber(
    novelId: string,
    chapterNumber: number
  ): Observable<ApiResponse<Chapter>> {
    return this.http.get<ApiResponse<Chapter>>(
      `${this.apiUrl}/novel/${novelId}/chapter/${chapterNumber}`
    );
  }

  getChapterById(id: string): Observable<ApiResponse<Chapter>> {
    return this.http.get<ApiResponse<Chapter>>(`${this.apiUrl}/${id}`);
  }
}
