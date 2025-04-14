import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule, Routes } from "@angular/router";
import { ReactiveFormsModule } from "@angular/forms";
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

// Components
import { AdminDashboardComponent } from "./components/admin-dashboard/admin-dashboard.component";
import { UserManagementComponent } from "./components/user-management/user-management.component";
import { NovelManagementComponent } from "./components/novel-management/novel-management.component";
import { ChapterManagementComponent } from "./components/chapter-management/chapter-management.component";
import { CommentManagementComponent } from "./components/comment-management/comment-management.component";
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
      { path: "comments", component: CommentManagementComponent },
      { path: "", redirectTo: "users", pathMatch: "full" },
    ],
  },
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    ReactiveFormsModule,
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
    AdminDashboardComponent,
    UserManagementComponent,
    NovelManagementComponent,
    ChapterManagementComponent,
    CommentManagementComponent,
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
