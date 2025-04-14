import { bootstrapApplication } from "@angular/platform-browser";
import { provideRouter } from "@angular/router";
import { provideHttpClient, withInterceptorsFromDi } from "@angular/common/http";
import { importProvidersFrom } from "@angular/core";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { MatPaginatorModule } from "@angular/material/paginator";
import { AppComponent } from "./app/app.component";
import { routes } from "./app/app.routes";
import { httpInterceptorProviders } from './app/interceptors/interceptors';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    importProvidersFrom(BrowserAnimationsModule, MatPaginatorModule),
    httpInterceptorProviders
  ],
}).catch((err) => console.error(err));
