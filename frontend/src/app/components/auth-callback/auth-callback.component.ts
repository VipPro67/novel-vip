import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SupabaseService } from '../../services/supabase.service';

@Component({
  selector: 'app-auth-callback',
  template: `
    <div class="auth-callback-container">
      <div class="auth-callback-card">
        <h2>Completing login...</h2>
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-callback-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background-color: #f5f5f5;
    }
    .auth-callback-card {
      background: white;
      padding: 2rem;
      border-radius: 8px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      text-align: center;
    }
    .spinner-border {
      width: 3rem;
      height: 3rem;
      margin-top: 1rem;
    }
  `],
  imports: [CommonModule]
})
export class AuthCallbackComponent implements OnInit {
  constructor(
    private router: Router,
    private supabaseService: SupabaseService
  ) {}

  ngOnInit() {
    // Handle the OAuth callback
    this.supabaseService.client.auth.onAuthStateChange((event, session) => {
      if (event === 'SIGNED_IN' && session) {
        // Verify token with backend
        this.supabaseService.verifyToken()
          .then(result => {
            if (result.valid) {
              // Navigate to home page
              this.router.navigate(['/']);
            } else {
              console.error('Token verification failed');
              this.router.navigate(['/login']);
            }
          })
          .catch(err => {
            console.error('Failed to verify token', err);
            this.router.navigate(['/login']);
          });
      } else if (event === 'SIGNED_OUT') {
        this.router.navigate(['/login']);
      }
    });
  }
} 