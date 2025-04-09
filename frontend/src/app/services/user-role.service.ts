import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { SupabaseService } from './supabase.service';
import { environment } from '../../environments/environment';

export interface UserRole {
  id: number;
  name: string;
  description?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserRoleService {
  private apiUrl = `${environment.apiUrl}/api/users`;

  constructor(
    private http: HttpClient,
    private supabaseService: SupabaseService
  ) {}

  // Get current user's roles from backend
  getCurrentUserRoles(): Observable<UserRole[]> {
    return this.http.get<UserRole[]>(`${this.apiUrl}/me/roles`);
  }

  // Assign a role to a user
  assignRole(userId: string, roleId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${userId}/roles`, { roleId });
  }

  // Remove a role from a user
  removeRole(userId: string, roleId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${userId}/roles/${roleId}`);
  }

  // Check if user has a specific role
  hasRole(roleName: string): Observable<boolean> {
    return this.getCurrentUserRoles().pipe(
      map(roles => roles.some(role => role.name === roleName)),
      catchError(() => of(false))
    );
  }

  // Sync Supabase user with backend
  syncUserWithBackend(): Observable<any> {
    return this.supabaseService.currentUser$.pipe(
      map(async user => {
        if (user) {
          // Get the Supabase token
          const { data: { session } } = await this.supabaseService.getSession();
          if (session?.access_token) {
            // Send token to backend to sync user
            return this.http.post(`${this.apiUrl}/sync`, { 
              supabaseId: user.id,
              email: user.email,
              token: session.access_token
            }).toPromise();
          }
        }
        return null;
      })
    );
  }
} 