import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: "root",
})
export class HomeService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getFeaturedNovels(): Observable<any> {
    return this.http.get(`${this.apiUrl}/api/novels/hot?page=0&size=4`);
  }

  getLatestUpdates(): Observable<any> {
    return this.http.get(
      `${this.apiUrl}/api/novels/latest-updates?page=0&size=10`
    );
  }

  getPopularNovels(): Observable<any> {
    return this.http.get(`${this.apiUrl}/api/novels/top-rated?page=0&size=4`);
  }

  getCategories(): Observable<any> {
    return this.http.get(`${this.apiUrl}/api/categories`);
  }
}
