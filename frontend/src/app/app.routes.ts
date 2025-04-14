import { Routes } from "@angular/router";
import { LoginComponent } from "./components/login/login.component";
import { RegisterComponent } from "./components/register/register.component";
import { AuthCallbackComponent } from "./components/auth-callback/auth-callback.component";
import { NovelListComponent } from "./components/novel-list/novel-list.component";
import { NovelDetailComponent } from "./components/novel-detail/novel-detail.component";
import { authGuard, roleGuard } from "./guards/auth.guard";
import { noAuthGuard } from "./guards/no-auth.guard";
import { ChapterReaderComponent } from "./components/chapter-reader/chapter-reader.component";
import { HomeComponent } from "./components/home/home.component";
export const routes: Routes = [
  { path: "", component: HomeComponent },
  {
    path: "login",
    component: LoginComponent,
    canActivate: [noAuthGuard],
  },
  {
    path: "register",
    component: RegisterComponent,
    canActivate: [noAuthGuard],
  },
  { path: "auth/callback", component: AuthCallbackComponent },
  { path: "novels/:id", component: NovelDetailComponent },
  {
    path: "novels/:novelId/chapters/:chapterNumber",
    component: ChapterReaderComponent,
  },
  {
    path: "admin",
    loadChildren: () =>
      import("./admin/admin.module").then((m) => m.AdminModule),
    canActivate: [authGuard, roleGuard],
    data: { role: "admin" },
  },
  { path: "**", redirectTo: "/login" },
];
