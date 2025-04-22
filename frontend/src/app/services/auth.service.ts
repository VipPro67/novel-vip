import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable } from "rxjs";
import { tap, catchError, map } from "rxjs/operators";
import { environment } from "../../environments/environment";
import { Router } from "@angular/router";
import { ApiResponse } from "../models/api-response.model";

interface User {
  id: string;
  email: string;
  username: string;
  roles: string[];
}

interface LoginResponse {
  accessToken: string;
  id: string;
  username: string;
  email: string;
  roles: string[];
  tokenType: string;
}

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private tokenSubject = new BehaviorSubject<string | null>(null);

  constructor(private http: HttpClient, private router: Router) {
    this.checkStoredSession();
  }

  private checkStoredSession() {
    const token = localStorage.getItem("token");
    if (token) {
      this.tokenSubject.next(token);
      this.loadUserProfile();
    }
  }

  private loadUserProfile() {
    const token = this.getToken();
    if (!token) {
      console.warn("No token available for profile loading");
      this.logout();
      return;
    }

    this.http
      .get<ApiResponse<User>>(`${environment.apiUrl}/api/users/profile`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .pipe(
        tap((response) => {
          if (response.success && response.data) {
            this.currentUserSubject.next(response.data);
          } else {
            console.error("Failed to load user profile:", response.message);
            this.logout();
          }
        }),
        catchError((error) => {
          console.error("Error loading user profile:", error);
          if (error.status === 401) {
            this.logout();
          }
          return [];
        })
      )
      .subscribe();
  }

  async login(email: string, password: string): Promise<void> {
    try {
      const response = await this.http
        .post<ApiResponse<LoginResponse>>(
          `${environment.apiUrl}/api/auth/signin`,
          {
            email,
            password,
          }
        )
        .toPromise();

      if (response?.success && response.data) {
        // Store token
        localStorage.setItem("token", response.data.accessToken);
        this.tokenSubject.next(response.data.accessToken);

        // Set user
        this.currentUserSubject.next({
          id: response.data.id,
          email: response.data.email,
          username: response.data.username,
          roles: response.data.roles,
        });

        // Navigate to home
        await this.router.navigate(["/"]);
      } else if (response) {
        throw new Error(response.message || "Login failed");
      }
    } catch (error: any) {
      if (error.error?.statusCode === 400) {
        const errorMessage = error.error.message;
        throw new Error(errorMessage);
      }
      throw new Error(error.error?.message || "An error occurred during login");
    }
  }

  async register(email: string, password: string): Promise<void> {
    try {
      const response = await this.http
        .post<ApiResponse<LoginResponse>>(
          `${environment.apiUrl}/api/auth/signup`,
          {
            email,
            password,
          }
        )
        .toPromise();

      if (response?.success && response.data) {
        // Store token
        localStorage.setItem("token", response.data.accessToken);
        this.tokenSubject.next(response.data.accessToken);

        // Set user
        this.currentUserSubject.next({
          id: response.data.id,
          email: response.data.email,
          username: response.data.username,
          roles: response.data.roles,
        });

        // Navigate to home
        await this.router.navigate(["/"]);
      } else if (response) {
        throw new Error(response.message || "Registration failed");
      }
    } catch (error: any) {
      if (error.error?.statusCode === 400) {
        const errorMessage = error.error.message || "Invalid registration data";
        throw new Error(errorMessage);
      }
      throw new Error(
        error.error?.message || "An error occurred during registration"
      );
    }
  }

  async logout(): Promise<void> {
    try {
      // Clear token and user
      localStorage.removeItem("token");
      this.tokenSubject.next(null);
      this.currentUserSubject.next(null);

      // Navigate to login
      await this.router.navigate(["/login"]);
    } catch (error) {
      console.error("Logout error:", error);
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

  isAdmin(): boolean | null {
    const user = this.currentUserSubject.value;
    return user && user.roles && user.roles.includes('ADMIN');
  }
}
