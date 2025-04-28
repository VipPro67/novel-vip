import { Component } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { NavbarComponent } from "./public/navbar/navbar.component";
import { CommonModule } from "@angular/common";
import { NavbarService } from "./services/navbar.service";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  template: `
    <div class="container">
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [
    `
      .container {
        display: flex;
        flex-direction: column;
        height: 100vh;
        padding: 0;
        max-width: 100vw;
      }
    `,
  ],
})
export class AppComponent {
  title = "novel-fe";
  showNavbar$ = this.navbarService.showNavbar$;
  constructor(private navbarService: NavbarService) {}
}
