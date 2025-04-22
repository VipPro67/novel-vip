import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { UserManagementComponent } from './components/user-management/user-management.component';
import { NovelManagementComponent } from './components/novel-management/novel-management.component';
import { CategoryManagementComponent } from './components/category-management/category-management.component';
import { ReportManagementComponent } from './components/report-management/report-management.component';
import { FeatureRequestManagementComponent } from './components/feature-request-management/feature-request-management.component';
import { SubscriptionManagementComponent } from './components/subscription-management/subscription-management.component';
import { PaymentManagementComponent } from './components/payment-management/payment-management.component';
import { AdminGuard } from '../guards/admin.guard';

const routes: Routes = [
  {
    path: '',
    component: AdminDashboardComponent,
    canActivate: [AdminGuard],
    children: [
      { path: 'users', component: UserManagementComponent },
      { path: 'novels', component: NovelManagementComponent },
      { path: 'categories', component: CategoryManagementComponent },
      { path: 'reports', component: ReportManagementComponent },
      { path: 'feature-requests', component: FeatureRequestManagementComponent },
      { path: 'subscriptions', component: SubscriptionManagementComponent },
      { path: 'payments', component: PaymentManagementComponent },
      { path: '', redirectTo: 'users', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { } 