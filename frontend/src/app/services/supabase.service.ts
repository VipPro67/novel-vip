import { Injectable } from "@angular/core";
import { createClient, SupabaseClient, User } from "@supabase/supabase-js";
import { BehaviorSubject, Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: "root",
})
export class SupabaseService {
  private supabase: SupabaseClient;
  private currentUser = new BehaviorSubject<User | null>(null);
  private currentSession = new BehaviorSubject<any>(null);

  constructor() {
    this.supabase = createClient(
      environment.supabaseUrl,
      environment.supabaseAnonKey
    );

    // Check for existing session
    this.supabase.auth.getSession().then(({ data: { session } }) => {
      this.currentSession.next(session);
      this.currentUser.next(session?.user ?? null);
    });

    // Listen for auth changes
    this.supabase.auth.onAuthStateChange((event, session) => {
      this.currentSession.next(session);
      this.currentUser.next(session?.user ?? null);
    });
  }

  // Get the supabase client
  get client(): SupabaseClient {
    return this.supabase;
  }

  // Get current user as observable
  get currentUser$(): Observable<User | null> {
    return this.currentUser.asObservable();
  }

  // Get current session as observable
  get currentSession$(): Observable<any> {
    return this.currentSession.asObservable();
  }

  // Get current session
  async getSession() {
    return await this.supabase.auth.getSession();
  }

  // Get access token
  async getAccessToken(): Promise<string | null> {
    const { data } = await this.supabase.auth.getSession();
    return data.session?.access_token || null;
  }

  // Sign up with email and password
  async signUp(
    email: string,
    password: string
  ): Promise<{ user: User | null; error: any }> {
    const { data, error } = await this.supabase.auth.signUp({
      email,
      password,
    });
    return { user: data.user, error };
  }

  // Sign in with email and password
  async signIn(
    email: string,
    password: string
  ): Promise<{ user: User | null; error: any }> {
    const { data, error } = await this.supabase.auth.signInWithPassword({
      email,
      password,
    });
    return { user: data.user, error };
  }

  // Sign in with OAuth provider (Google, GitHub, etc.)
  async signInWithOAuth(
    provider: "google" | "github" | "facebook"
  ): Promise<{ error: any }> {
    const { error } = await this.supabase.auth.signInWithOAuth({
      provider,
      options: {
        redirectTo: `${window.location.origin}/auth/callback`,
        queryParams: {
          access_type: "offline",
          prompt: "consent",
        },
      },
    });
    return { error };
  }

  // Sign out
  async signOut(): Promise<{ error: any }> {
    const { error } = await this.supabase.auth.signOut();
    return { error };
  }

  // Verify token with backend
  async verifyToken(): Promise<{ valid: boolean }> {
    try {
      const token = await this.getAccessToken();
      if (!token) return { valid: false };
      
      // Make a request to your backend to verify the token
      const response = await fetch(`${environment.apiUrl}/auth/verify`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });
      
      return { valid: response.ok };
    } catch (error) {
      console.error('Error verifying token:', error);
      return { valid: false };
    }
  }

  // Logout from backend
  async logoutFromBackend(): Promise<{ success: boolean; error?: string }> {
    const token = this.getAccessToken();
    if (!token) {
      return { success: false, error: "No token available" };
    }

    try {
      const response = await fetch(`${environment.apiUrl}/api/auth/logout`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const data = await response.json();
      return data;
    } catch (error) {
      return { success: false, error: "Failed to logout from backend" };
    }
  }

  async setSession(session: any) {
    return await this.supabase.auth.setSession(session);
  }

  async getUser() {
    return await this.supabase.auth.getUser();
  }

  getClient(): SupabaseClient {
    return this.supabase;
  }
}
