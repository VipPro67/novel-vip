import { Routes } from "@angular/router";
import { LoginComponent } from "./public/login/login.component";
import { RegisterComponent } from "./public/register/register.component";
import { authGuard } from "./guards/auth.guard";
import { noAuthGuard } from "./guards/no-auth.guard";
import { AdminGuard } from "./guards/admin.guard";

export const routes: Routes = [
  {
    path: "login",
    component: LoginComponent,
    //   canActivate: [noAuthGuard],
  },
  {
    path: "register",
    component: RegisterComponent,
    //  canActivate: [noAuthGuard],
  },
  {
    path: "admin",
    loadChildren: () =>
      import("./admin/admin.module").then((m) => m.AdminModule),
    // canActivate: [AdminGuard],
  },
  { path: "**", redirectTo: "/login" },
];
