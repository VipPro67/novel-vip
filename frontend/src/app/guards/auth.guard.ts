import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export const authGuard = () => {
  const router = inject(Router);
  const authService = inject(AuthService);

  if (authService.isAuthenticated()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};

// Role-based guard
export const roleGuard = () => {
  return () => {
    const router = inject(Router);
    const authService = inject(AuthService);

    if (!authService.isAuthenticated()) {
      router.navigate(['/login']);
      return false;
    }

    // Check if user has admin role
    if (!authService.hasRole('admin')) {
      router.navigate(['/']);
      return false;
    }

    return true;
  };
}; 