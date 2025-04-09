import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Check if the request is to our API
    if (request.url.startsWith(environment.apiUrl)) {
      // Get the token from the auth service
      const token = this.authService.getToken();
      
      if (token) {
        // Clone the request and add the authorization header
        const authReq = request.clone({
          headers: request.headers.set('Authorization', `Bearer ${token}`)
        });
        
        // Pass the cloned request with the authorization header to the next handler
        return next.handle(authReq);
      }
    }
    
    // If no token or not an API request, proceed with the original request
    return next.handle(request);
  }
} 