import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule } from "@angular/router";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatListModule } from "@angular/material/list";
import { MatIconModule } from "@angular/material/icon";

@Component({
  selector: "app-admin-dashboard",
  templateUrl: "./admin-dashboard.component.html",
  styleUrls: ["./admin-dashboard.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
  ],
})
export class AdminDashboardComponent {
  navItems = [
    { path: "users", icon: "people", label: "Users" },
    { path: "novels", icon: "book", label: "Novels" },
    { path: "chapters", icon: "menu_book", label: "Chapters" },
    { path: "comments", icon: "comment", label: "Comments" },
  ];
}
