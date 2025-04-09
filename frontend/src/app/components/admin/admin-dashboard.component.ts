import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="admin-dashboard-container">
      <h1>Admin Dashboard</h1>
      <nav class="admin-nav">
        <ul>
          <li><a routerLink="/admin/users">User Management</a></li>
          <li><a routerLink="/admin/roles">Role Management</a></li>
        </ul>
      </nav>
      <div class="admin-content">
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
  styles: [`
    .admin-dashboard-container {
      padding: 2rem;
      max-width: 1200px;
      margin: 0 auto;
    }
    
    .admin-nav {
      margin: 2rem 0;
      padding: 1rem;
      background-color: #f5f5f5;
      border-radius: 4px;
    }
    
    .admin-nav ul {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      gap: 1rem;
    }
    
    .admin-nav a {
      text-decoration: none;
      color: #333;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      transition: background-color 0.2s;
    }
    
    .admin-nav a:hover {
      background-color: #e0e0e0;
    }
    
    .admin-content {
      margin-top: 2rem;
    }
  `]
})
export class AdminDashboardComponent {} 