import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Router, NavigationEnd } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class NavbarService {
  private showNavbarSubject = new BehaviorSubject<boolean>(true);
  showNavbar$ = this.showNavbarSubject.asObservable();

  constructor(private router: Router) {
    // Subscribe to router events to update navbar visibility
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        // Hide navbar on admin routes
        const isAdminRoute = event.url.startsWith('/admin');
        this.showNavbarSubject.next(!isAdminRoute);
      }
    });
  }
} 