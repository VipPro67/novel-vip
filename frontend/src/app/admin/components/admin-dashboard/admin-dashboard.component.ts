import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule, Router } from "@angular/router";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatListModule } from "@angular/material/list";
import { MatIconModule } from "@angular/material/icon";

@Component({
  selector: "app-admin-dashboard",
  templateUrl: "./admin-dashboard.component.html",
  styleUrls: ["./admin-dashboard.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
  ],
})
export class AdminDashboardComponent {
  navLinks = [
    { path: 'users', label: 'Users', icon: 'people' },
    { path: 'novels', label: 'Novels', icon: 'book' },
    { path: 'categories', label: 'Categories', icon: 'category' },
    { path: 'reports', label: 'Reports', icon: 'report_problem' },
    { path: 'feature-requests', label: 'Feature Requests', icon: 'lightbulb' },
    { path: 'subscriptions', label: 'Subscriptions', icon: 'subscriptions' },
    { path: 'payments', label: 'Payments', icon: 'payments' }
  ];

  constructor(private router: Router) {}

  isActive(path: string): boolean {
    return this.router.isActive(`/admin/${path}`, {
      paths: 'exact',
      queryParams: 'exact',
      fragment: 'ignored',
      matrixParams: 'ignored'
    });
  }
}
