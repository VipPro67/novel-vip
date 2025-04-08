import { bootstrapApplication } from "@angular/platform-browser";
import { provideRouter, withComponentInputBinding } from "@angular/router";
import { provideHttpClient } from "@angular/common/http";
import { importProvidersFrom } from "@angular/core";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { MatPaginatorModule } from "@angular/material/paginator";
import { AppComponent } from "./app/app.component";
import { NovelListComponent } from "./app/components/novel-list/novel-list.component";
import { NovelDetailComponent } from "./app/components/novel-detail/novel-detail.component";

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(
      [
        { path: "", component: NovelListComponent },
        { path: "novel/:id", component: NovelDetailComponent },
        { path: "**", redirectTo: "", pathMatch: "full" },
      ],
      withComponentInputBinding()
    ),
    provideHttpClient(),
    importProvidersFrom(BrowserAnimationsModule, MatPaginatorModule),
  ],
}).catch((err) => console.error(err));
