import { Component } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { NavbarComponent } from "./components/navbar/navbar.component";
import { CommonModule } from "@angular/common";
import { NavbarService } from "./services/navbar.service";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, CommonModule],
  template: `
    <div class="container">
      <app-navbar *ngIf="showNavbar$ | async"></app-navbar>
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [
    `
      .container {
        padding: 20px;
      }
    `,
  ],
})
export class AppComponent {
  title = "novel-fe";
  showNavbar$ = this.navbarService.showNavbar$;

  constructor(private navbarService: NavbarService) {}
}
