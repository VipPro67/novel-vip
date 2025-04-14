import { Component, OnInit, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatTableModule, MatTableDataSource } from "@angular/material/table";
import { MatPaginatorModule, MatPaginator } from "@angular/material/paginator";
import { MatSortModule, MatSort } from "@angular/material/sort";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { UserService } from "../../../services/user.service";
import { User } from "../../../models/user.model";
import { UserDialogComponent } from "../user-dialog/user-dialog.component";

@Component({
  selector: "app-user-management",
  template: `
    <div class="mat-elevation-z8">
      <div class="header">
        <h2>User Management</h2>
        <button
          mat-raised-button
          color="primary"
          (click)="openCreateUserDialog()"
        >
          <mat-icon>add</mat-icon>
          Create User
        </button>
      </div>

      <table mat-table [dataSource]="dataSource" matSort>
        <ng-container matColumnDef="id">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
          <td mat-cell *matCellDef="let user">{{ user.id }}</td>
        </ng-container>

        <ng-container matColumnDef="username">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Username</th>
          <td mat-cell *matCellDef="let user">{{ user.username }}</td>
        </ng-container>

        <ng-container matColumnDef="email">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Email</th>
          <td mat-cell *matCellDef="let user">{{ user.email }}</td>
        </ng-container>

        <ng-container matColumnDef="roles">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Roles</th>
          <td mat-cell *matCellDef="let user">{{ user.roles }}</td>
        </ng-container>

        <ng-container matColumnDef="fullName">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Full Name</th>
          <td mat-cell *matCellDef="let user">{{ user.fullName }}</td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let user">
            <button
              mat-icon-button
              color="primary"
              (click)="openEditUserDialog(user)"
            >
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button color="warn" (click)="deleteUser(user)">
              <mat-icon>delete</mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
      </table>

      <mat-paginator
        [pageSizeOptions]="[5, 10, 25, 100]"
        aria-label="Select page of users"
      ></mat-paginator>
    </div>
  `,
  styles: [
    `
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px;
      }
      table {
        width: 100%;
      }
      .mat-column-actions {
        width: 120px;
        text-align: center;
      }
    `,
  ],
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
  ],
})
export class UserManagementComponent implements OnInit {
  displayedColumns: string[] = [
   // "id",
    "username",
    "email",
    "roles",
    "fullName",
    "actions",
  ];
  dataSource: MatTableDataSource<User>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private userService: UserService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.dataSource = new MatTableDataSource<User>([]);
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadUsers(): void {
    this.userService.getUsers().subscribe({
      next: (response) => {
        if (response.success) {
          this.dataSource.data = response.data.content;
          this.paginator.length = response.data.totalElements;
        } else {
          this.snackBar.open("Error loading users", "Close", {
            duration: 3000,
          });
        }
      },
      error: (error) => {
        this.snackBar.open("Error loading users", "Close", { duration: 3000 });
        console.error("Error loading users:", error);
      },
    });
  }

  openCreateUserDialog(): void {
    const dialogRef = this.dialog.open(UserDialogComponent, {
      width: "500px",
      data: { user: null },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.createUser(result).subscribe({
          next: () => {
            this.loadUsers();
            this.snackBar.open("User created successfully", "Close", {
              duration: 3000,
            });
          },
          error: (error) => {
            this.snackBar.open("Error creating user", "Close", {
              duration: 3000,
            });
            console.error("Error creating user:", error);
          },
        });
      }
    });
  }

  openEditUserDialog(user: User): void {
    const dialogRef = this.dialog.open(UserDialogComponent, {
      width: "500px",
      data: { user },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.updateUser(result.id, result).subscribe({
          next: () => {
            this.loadUsers();
            this.snackBar.open("User updated successfully", "Close", {
              duration: 3000,
            });
          },
          error: (error) => {
            this.snackBar.open("Error updating user", "Close", {
              duration: 3000,
            });
            console.error("Error updating user:", error);
          },
        });
      }
    });
  }

  deleteUser(user: User): void {
    if (confirm(`Are you sure you want to delete user ${user.username}?`)) {
      this.userService.deleteUser(user.id).subscribe({
        next: () => {
          this.loadUsers();
          this.snackBar.open("User deleted successfully", "Close", {
            duration: 3000,
          });
        },
        error: (error) => {
          this.snackBar.open("Error deleting user", "Close", {
            duration: 3000,
          });
          console.error("Error deleting user:", error);
        },
      });
    }
  }
}
