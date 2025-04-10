import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';

interface User {
  id: string;
  email: string;
  username: string;
  roles: string[];
}

interface LoginResponse {
  token: string;
  user: User;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private tokenSubject = new BehaviorSubject<string | null>(null);

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.checkStoredSession();
  }

  private checkStoredSession() {
    const token = localStorage.getItem('token');
    if (token) {
      this.tokenSubject.next(token);
      this.loadUserProfile();
    }
  }

  private loadUserProfile() {
    this.http.get<User>(`${environment.apiUrl}/api/users/me`)
      .pipe(
        tap(user => {
          this.currentUserSubject.next(user);
        }),
        catchError(error => {
          console.error('Error loading user profile:', error);
          this.logout();
          return [];
        })
      )
      .subscribe();
  }

  async login(email: string, password: string): Promise<void> {
    try {
      const response = await this.http.post<LoginResponse>(`${environment.apiUrl}/api/auth/login`, {
        email,
        password
      }).toPromise();
      
      if (response) {
        // Store token
        localStorage.setItem('token', response.token);
        this.tokenSubject.next(response.token);
        
        // Set user
        this.currentUserSubject.next(response.user);
        
        // Navigate to dashboard
        await this.router.navigate(['/dashboard']);
      }
    } catch (error: any) {
      console.error('Login error:', error);
      throw error;
    }
  }

  async register(email: string, password: string): Promise<void> {
    try {
      const response = await this.http.post<LoginResponse>(`${environment.apiUrl}/api/auth/register`, {
        email,
        password
      }).toPromise();
      
      if (response) {
        // Store token
        localStorage.setItem('token', response.token);
        this.tokenSubject.next(response.token);
        
        // Set user
        this.currentUserSubject.next(response.user);
        
        // Navigate to dashboard
        await this.router.navigate(['/dashboard']);
      }
    } catch (error: any) {
      console.error('Registration error:', error);
      throw error;
    }
  }

  async logout(): Promise<void> {
    try {
      // Clear token and user
      localStorage.removeItem('token');
      this.tokenSubject.next(null);
      this.currentUserSubject.next(null);
      
      // Navigate to login
      await this.router.navigate(['/login']);
    } catch (error) {
      console.error('Logout error:', error);
      throw error;
    }
  }

  getToken(): string | null {
    return this.tokenSubject.value;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  // Check if user has a specific role
  hasRole(roleName: string): boolean {
    const user = this.currentUserSubject.value;
    return user?.roles?.includes(roleName) || false;
  }

  // Get current user
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
} 