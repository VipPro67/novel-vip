import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Get the token directly from localStorage
    const token = localStorage.getItem('token');    
    // If token exists, add it to the request headers
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    } else {
      console.log('AuthInterceptor - No token available, request will proceed without auth');
    }
    
    // Handle the request
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        console.log('AuthInterceptor - Request error:', error.status);
        // If unauthorized (401), clear token and redirect to login
        if (error.status === 401) {
          console.log('AuthInterceptor - Unauthorized, logging out');
          localStorage.removeItem('token');
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }
} 