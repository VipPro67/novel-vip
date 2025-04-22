import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../dtos/api-response.dto';
import {
  UserDTO,
  UserCreateDTO,
  UserUpdateDTO,
  UserSearchDTO,
  PageResponseUserDTO
} from '../../dtos/user.dto';
import {
  NovelDTO,
  NovelCreateDTO,
  NovelUpdateDTO,
  PageResponseNovelDTO
} from '../../dtos/novel.dto';
import {
  Category,
  PageResponseCategoryDTO
} from '../../dtos/category.dto';
import {
  ReportDTO,
  ReportUpdateDTO,
  PageResponseReportDTO
} from '../../dtos/report.dto';
import {
  FeatureRequestDTO,
  CreateFeatureRequestDTO,
  PageResponseFeatureRequestDTO,
  FeatureRequestStatus
} from '../../dtos/feature-request.dto';
import {
  SubscriptionDTO,
  SubscriptionPlanDTO,
  SubscriptionHistoryDTO,
  PageResponseSubscriptionHistoryDTO
} from '../../dtos/subscription.dto';
import {
  PaymentDTO,
  PaymentStatsDTO,
  PageResponsePaymentDTO
} from '../../dtos/payment.dto';
import {
  ChapterListDTO,
  ChapterDetailDTO,
  ChapterCreateDTO,
  PageResponseChapterListDTO
} from '../../dtos/chapter.dto';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // User Management
  getUsers(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponseUserDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponseUserDTO>>(`${this.apiUrl}/api/users`, { params });
  }

  getUser(userId: string): Observable<ApiResponse<UserDTO>> {
    return this.http.get<ApiResponse<UserDTO>>(`${this.apiUrl}/api/users/${userId}`);
  }

  searchUsers(searchDTO: UserSearchDTO): Observable<ApiResponse<PageResponseUserDTO>> {
    return this.http.get<ApiResponse<PageResponseUserDTO>>(`${this.apiUrl}/api/users/search`, { params: { ...searchDTO } });
  }

  createUser(userData: UserCreateDTO): Observable<ApiResponse<UserDTO>> {
    return this.http.post<ApiResponse<UserDTO>>(`${this.apiUrl}/api/users`, userData);
  }

  updateUser(userId: string, userData: UserUpdateDTO): Observable<ApiResponse<UserDTO>> {
    return this.http.put<ApiResponse<UserDTO>>(`${this.apiUrl}/api/users/${userId}`, userData);
  }

  updateUserRoles(userId: string, roles: string[]): Observable<ApiResponse<UserDTO>> {
    return this.http.put<ApiResponse<UserDTO>>(`${this.apiUrl}/api/users/${userId}/roles`, roles);
  }

  deleteUser(userId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/api/users/${userId}`);
  }

  // Novel Management
  getNovels(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponseNovelDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponseNovelDTO>>(`${this.apiUrl}/api/novels`, { params });
  }

  createNovel(novelData: NovelCreateDTO): Observable<ApiResponse<NovelDTO>> {
    return this.http.post<ApiResponse<NovelDTO>>(`${this.apiUrl}/api/novels`, novelData);
  }

  updateNovel(novelId: string, novelData: NovelUpdateDTO): Observable<ApiResponse<NovelDTO>> {
    return this.http.put<ApiResponse<NovelDTO>>(`${this.apiUrl}/api/novels/${novelId}`, novelData);
  }

  deleteNovel(novelId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/api/novels/${novelId}`);
  }

  // Category Management
  getCategories(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponseCategoryDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponseCategoryDTO>>(`${this.apiUrl}/api/categories`, { params });
  }

  createCategory(categoryData: Category): Observable<ApiResponse<Category>> {
    return this.http.post<ApiResponse<Category>>(`${this.apiUrl}/api/categories`, categoryData);
  }

  updateCategory(categoryId: string, categoryData: Category): Observable<ApiResponse<Category>> {
    return this.http.put<ApiResponse<Category>>(`${this.apiUrl}/api/categories/${categoryId}`, categoryData);
  }

  deleteCategory(categoryId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/api/categories/${categoryId}`);
  }

  // Report Management
  getReports(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponseReportDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponseReportDTO>>(`${this.apiUrl}/api/reports`, { params });
  }

  getPendingReports(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponseReportDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponseReportDTO>>(`${this.apiUrl}/api/reports/pending`, { params });
  }

  updateReportStatus(reportId: string, updateData: ReportUpdateDTO): Observable<ApiResponse<ReportDTO>> {
    return this.http.put<ApiResponse<ReportDTO>>(`${this.apiUrl}/api/reports/${reportId}`, updateData);
  }

  // Feature Request Management
  getFeatureRequests(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponseFeatureRequestDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponseFeatureRequestDTO>>(`${this.apiUrl}/api/feature-requests`, { params });
  }

  getFeatureRequestsByStatus(
    status: FeatureRequestStatus,
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<PageResponseFeatureRequestDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponseFeatureRequestDTO>>(
      `${this.apiUrl}/api/feature-requests/status/${status}`,
      { params }
    );
  }

  updateFeatureRequestStatus(requestId: number, status: FeatureRequestStatus): Observable<ApiResponse<FeatureRequestDTO>> {
    return this.http.put<ApiResponse<FeatureRequestDTO>>(
      `${this.apiUrl}/api/feature-requests/${requestId}/status`,
      null,
      { params: new HttpParams().set('newStatus', status) }
    );
  }

  // Subscription Management
  getSubscriptionPlans(): Observable<ApiResponse<SubscriptionPlanDTO[]>> {
    return this.http.get<ApiResponse<SubscriptionPlanDTO[]>>(`${this.apiUrl}/api/subscriptions/plans`);
  }

  getSubscriptionHistory(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponseSubscriptionHistoryDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponseSubscriptionHistoryDTO>>(`${this.apiUrl}/api/subscriptions/history`, { params });
  }

  getCurrentSubscription(): Observable<ApiResponse<SubscriptionDTO>> {
    return this.http.get<ApiResponse<SubscriptionDTO>>(`${this.apiUrl}/api/subscriptions/current`);
  }

  // Payment Management
  getPayments(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponsePaymentDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponsePaymentDTO>>(`${this.apiUrl}/api/payments/user`, { params });
  }

  getPaymentStats(startDate?: string, endDate?: string): Observable<ApiResponse<PaymentStatsDTO>> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<ApiResponse<PaymentStatsDTO>>(`${this.apiUrl}/api/payments/stats`, { params });
  }

  getPayment(paymentId: string): Observable<ApiResponse<PaymentDTO>> {
    return this.http.get<ApiResponse<PaymentDTO>>(`${this.apiUrl}/api/payments/${paymentId}`);
  }

  cancelPayment(paymentId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/api/payments/${paymentId}`);
  }

  // Chapter Management
  getChaptersByNovel(novelId: string, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponseChapterListDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponseChapterListDTO>>(`${this.apiUrl}/api/chapters/novel/${novelId}`, { params });
  }

  getChapter(chapterId: string): Observable<ApiResponse<ChapterDetailDTO>> {
    return this.http.get<ApiResponse<ChapterDetailDTO>>(`${this.apiUrl}/api/chapters/${chapterId}`);
  }

  createChapter(chapterData: ChapterCreateDTO): Observable<ApiResponse<ChapterListDTO>> {
    return this.http.post<ApiResponse<ChapterListDTO>>(`${this.apiUrl}/api/chapters`, chapterData);
  }

  updateChapter(chapterId: string, chapterData: ChapterCreateDTO): Observable<ApiResponse<ChapterListDTO>> {
    return this.http.put<ApiResponse<ChapterListDTO>>(`${this.apiUrl}/api/chapters/${chapterId}`, chapterData);
  }

  deleteChapter(chapterId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/api/chapters/${chapterId}`);
  }
} 