import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { AppComponent } from "./app.component";
import { RouterModule } from "@angular/router";
import { TruncateWordsPipe } from "./pipes/truncate-words.pipe"; // Import the pipe
import { NovelListComponent } from "./components/novel-list/novel-list.component";
import { routes } from "./app.routes";

@NgModule({
  declarations: [],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule.forRoot(routes),
    AppComponent,
    TruncateWordsPipe,
    NovelListComponent,
  ],
  providers: [],
})
export class AppModule {}
