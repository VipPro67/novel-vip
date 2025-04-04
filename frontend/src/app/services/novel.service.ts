import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Novel, Chapter, PaginatedResponse } from '../models/novel.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class NovelService {
  private apiUrl = `${environment.apiUrl}/api/novels`;

  constructor(private http: HttpClient) { }

  getAllNovels(page: number = 0, size: number = 10, sortBy: string = 'views'): Observable<PaginatedResponse<Novel>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy);
    return this.http.get<PaginatedResponse<Novel>>(this.apiUrl, { params });
  }

  getNovelById(id: number): Observable<Novel> {
    return this.http.get<Novel>(`${this.apiUrl}/${id}`);
  }

  getNovelsByCategory(category: string, page: number = 0, size: number = 10): Observable<PaginatedResponse<Novel>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PaginatedResponse<Novel>>(`${this.apiUrl}/category/${category}`, { params });
  }

  getNovelsByStatus(status: string, page: number = 0, size: number = 10): Observable<PaginatedResponse<Novel>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PaginatedResponse<Novel>>(`${this.apiUrl}/status/${status}`, { params });
  }

  searchNovels(keyword: string, page: number = 0, size: number = 10): Observable<PaginatedResponse<Novel>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PaginatedResponse<Novel>>(`${this.apiUrl}/search`, { params });
  }

  getHotNovels(page: number = 0, size: number = 10): Observable<PaginatedResponse<Novel>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PaginatedResponse<Novel>>(`${this.apiUrl}/hot`, { params });
  }

  getTopRatedNovels(page: number = 0, size: number = 10): Observable<PaginatedResponse<Novel>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PaginatedResponse<Novel>>(`${this.apiUrl}/top-rated`, { params });
  }
} 