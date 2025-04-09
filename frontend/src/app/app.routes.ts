import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { AuthCallbackComponent } from './components/auth-callback/auth-callback.component';
import { NovelListComponent } from './components/novel-list/novel-list.component';
import { NovelDetailComponent } from './components/novel-detail/novel-detail.component';
import { authGuard, roleGuard } from './guards/auth.guard';
import { noAuthGuard } from './guards/no-auth.guard';

export const routes: Routes = [
  { path: '', component: NovelListComponent },
  { 
    path: 'login', 
    component: LoginComponent,
    canActivate: [noAuthGuard]
  },
  { 
    path: 'register', 
    component: RegisterComponent,
    canActivate: [noAuthGuard]
  },
  { path: 'auth/callback', component: AuthCallbackComponent },
  { path: 'novels/:id', component: NovelDetailComponent },
  {
    path: 'dashboard',
    loadChildren: () => import('./components/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES),
    //canActivate: [authGuard]
  },
  {
    path: 'admin',
    loadChildren: () => import('./components/admin/admin.routes').then(m => m.ADMIN_ROUTES),
    canActivate: [roleGuard(['ADMIN'])]
  },
  { path: '**', redirectTo: '/login' }
]; 