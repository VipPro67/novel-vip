import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { Chapter, PaginatedResponse } from "../services/novel.service"
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
  ): Observable<PaginatedResponse<Chapter>> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());
    return this.http.get<PaginatedResponse<Chapter>>(
      `${this.apiUrl}/novel/${novelId}`,
      { params }
    );
  }

  getChapterByNovelIdAndNumber(
    novelId: number,
    chapterNumber: number
  ): Observable<Chapter> {
    return this.http.get<Chapter>(
      `${this.apiUrl}/novel/${novelId}/chapter/${chapterNumber}`
    );
  }

  getChapterById(id: number): Observable<Chapter> {
    return this.http.get<Chapter>(`${this.apiUrl}/${id}`);
  }
}
