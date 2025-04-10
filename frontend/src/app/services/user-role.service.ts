import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Role {
  id: string;
  name: string;
  description?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserRoleService {
  constructor(private http: HttpClient) {}

  // Get all available roles
  getAllRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${environment.apiUrl}/api/roles`);
  }

  // Request a new role
  requestRole(roleName: string): Observable<any> {
    return this.http.post(`${environment.apiUrl}/api/role-approval/request`, {
      requestedRole: roleName
    });
  }

  // Get user's role requests
  getUserRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/api/role-approval/my-requests`);
  }

  // Get all pending role requests (admin only)
  getPendingRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/api/role-approval/pending`);
  }

  // Approve a role request (admin only)
  approveRequest(requestId: string): Observable<any> {
    return this.http.post(`${environment.apiUrl}/api/role-approval/approve/${requestId}`, {});
  }

  // Reject a role request (admin only)
  rejectRequest(requestId: string, reason: string): Observable<any> {
    return this.http.post(`${environment.apiUrl}/api/role-approval/reject/${requestId}`, {
      reason
    });
  }
} 