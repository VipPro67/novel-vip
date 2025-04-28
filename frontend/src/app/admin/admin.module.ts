import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule, Routes } from "@angular/router";
import { ReactiveFormsModule, FormsModule } from "@angular/forms";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatListModule } from "@angular/material/list";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatTableModule } from "@angular/material/table";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatSortModule } from "@angular/material/sort";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatDialogModule } from "@angular/material/dialog";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatTabsModule } from "@angular/material/tabs";
import { MatChipsModule } from "@angular/material/chips";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatMenuModule } from "@angular/material/menu";
import { MatAutocompleteModule } from "@angular/material/autocomplete";

// Components
import { AdminDashboardComponent } from "./components/admin-dashboard/admin-dashboard.component";
import { UserManagementComponent } from "./components/user-management/user-management.component";
import { UserDialogComponent } from "./components/user-management/user-dialog/user-dialog.component";
import { UpdateRolesDialogComponent } from "./components/user-management/update-roles-dialog/update-roles-dialog.component";
import { NovelManagementComponent } from "./components/novel-management/novel-management.component";
import { ChapterManagementComponent } from "./components/chapter-management/chapter-management.component";
import { ChapterDialogComponent } from "./components/chapter-management/chapter-dialog/chapter-dialog.component";
import { CategoryManagementComponent } from "./components/category-management/category-management.component";
import { CategoryDialogComponent } from "./components/category-management/category-dialog/category-dialog.component";
import { ReportManagementComponent } from "./components/report-management/report-management.component";
import { ReportDialogComponent } from "./components/report-management/report-dialog/report-dialog.component";
import { FeatureRequestManagementComponent } from "./components/feature-request-management/feature-request-management.component";
import { FeatureRequestDialogComponent } from "./components/feature-request-management/feature-request-dialog/feature-request-dialog.component";
import { SubscriptionManagementComponent } from "./components/subscription-management/subscription-management.component";
import { SubscriptionDialogComponent } from "./components/subscription-management/subscription-dialog/subscription-dialog.component";
import { PaymentManagementComponent } from "./components/payment-management/payment-management.component";
import { PaymentDialogComponent } from "./components/payment-management/payment-dialog/payment-dialog.component";
import { AuthInterceptor } from "../interceptors/auth.interceptor";
import { HTTP_INTERCEPTORS } from "@angular/common/http";

const routes: Routes = [
  {
    path: "",
    component: AdminDashboardComponent,
    children: [
      { path: "users", component: UserManagementComponent },
      { path: "novels", component: NovelManagementComponent },
      { path: "chapters", component: ChapterManagementComponent },
      { path: "categories", component: CategoryManagementComponent },
      { path: "reports", component: ReportManagementComponent },
      {
        path: "feature-requests",
        component: FeatureRequestManagementComponent,
      },
      { path: "subscriptions", component: SubscriptionManagementComponent },
      { path: "payments", component: PaymentManagementComponent },
      { path: "", redirectTo: "users", pathMatch: "full" },
    ],
  },
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    ReactiveFormsModule,
    FormsModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTabsModule,
    MatChipsModule,
    MatTooltipModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatAutocompleteModule,
    // Import standalone components
    AdminDashboardComponent,
    UserManagementComponent,
    UserDialogComponent,
    UpdateRolesDialogComponent,
    NovelManagementComponent,
    ChapterManagementComponent,
    ChapterDialogComponent,
    CategoryManagementComponent,
    CategoryDialogComponent,
    ReportManagementComponent,
    ReportDialogComponent,
    FeatureRequestManagementComponent,
    FeatureRequestDialogComponent,
    SubscriptionManagementComponent,
    SubscriptionDialogComponent,
    PaymentManagementComponent,
    PaymentDialogComponent,
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
  ],
})
export class AdminModule {}
