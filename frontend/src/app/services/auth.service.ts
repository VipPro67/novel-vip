import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, from, of } from 'rxjs';
import { tap, switchMap, catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { SupabaseService } from './supabase.service';
import { UserRoleService, UserRole } from './user-role.service';
import { Router } from '@angular/router';

interface User {
  id: string;
  email: string;
  role: string;
  roles?: UserRole[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private userRolesSubject = new BehaviorSubject<UserRole[]>([]);
  public userRoles$ = this.userRolesSubject.asObservable();

  constructor(
    private http: HttpClient,
    private supabaseService: SupabaseService,
    private userRoleService: UserRoleService,
    private router: Router
  ) {
    this.checkStoredSession();
    
    // Subscribe to Supabase auth state changes
    this.supabaseService.currentUser$.subscribe(user => {
      if (user) {
        // User is authenticated with Supabase
        this.currentUserSubject.next({
          id: user.id,
          email: user.email || '',
          role: user.role || 'user'
        });
        
        // Sync user with backend and get roles
        this.syncUserWithBackend();
      } else {
        // User is not authenticated
        this.currentUserSubject.next(null);
        this.userRolesSubject.next([]);
      }
    });
  }

  private async checkStoredSession() {
    try {
      const session = localStorage.getItem('sb-session');
      if (session) {
        const parsedSession = JSON.parse(session);
        if (parsedSession?.access_token) {
          // Set the session in Supabase client
          await this.supabaseService.setSession(parsedSession);
          
          // Get user data
          const { data: { user }, error } = await this.supabaseService.getUser();
          
          if (user && !error) {
            this.currentUserSubject.next({
              id: user.id,
              email: user.email!,
              role: user.role || 'user'
            });
          }
        }
      }
    } catch (error) {
      console.error('Error checking stored session:', error);
      this.logout();
    }
  }

  // Sync user with backend and get roles
  private syncUserWithBackend() {
    this.userRoleService.syncUserWithBackend().subscribe(
      () => {
        // After syncing, get user roles
        this.loadUserRoles();
      },
      error => {
        console.error('Error syncing user with backend:', error);
      }
    );
  }

  // Load user roles from backend
  private loadUserRoles() {
    this.userRoleService.getCurrentUserRoles().subscribe(
      roles => {
        this.userRolesSubject.next(roles);
        
        // Update current user with roles
        const currentUser = this.currentUserSubject.value;
        if (currentUser) {
          this.currentUserSubject.next({
            ...currentUser,
            roles
          });
        }
      },
      error => {
        console.error('Error loading user roles:', error);
      }
    );
  }

  async login(email: string, password: string): Promise<void> {
    try {
      const { user, error } = await this.supabaseService.signIn(email, password);
      
      if (error) throw error;
      
      if (user) {
        this.currentUserSubject.next({
          id: user.id,
          email: user.email!,
          role: user.role || 'user'
        });
        
        await this.router.navigate(['/dashboard']);
      }
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }

  async register(email: string, password: string): Promise<void> {
    try {
      const { user, error } = await this.supabaseService.signUp(email, password);
      
      if (error) throw error;
      
      if (user) {
        this.currentUserSubject.next({
          id: user.id,
          email: user.email!,
          role: user.role || 'user'
        });
        
        await this.router.navigate(['/dashboard']);
      }
    } catch (error) {
      console.error('Registration error:', error);
      throw error;
    }
  }

  async logout(): Promise<void> {
    try {
      await this.supabaseService.signOut();
      this.currentUserSubject.next(null);
      localStorage.removeItem('sb-session');
      await this.router.navigate(['/login']);
    } catch (error) {
      console.error('Logout error:', error);
      throw error;
    }
  }

  getToken(): string | null {
    try {
      const session = localStorage.getItem('sb-session');
      if (session) {
        const parsedSession = JSON.parse(session);
        return parsedSession?.access_token || null;
      }
    } catch (error) {
      console.error('Error getting token:', error);
    }
    return null;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  // Check if user has a specific role
  hasRole(roleName: string): boolean {
    const roles = this.userRolesSubject.value;
    return roles.some(role => role.name === roleName);
  }

  // Get current user
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  // Get user roles
  getUserRoles(): UserRole[] {
    return this.userRolesSubject.value;
  }
} 