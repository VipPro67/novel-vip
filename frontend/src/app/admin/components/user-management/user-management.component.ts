import { Component, OnInit, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
  FormControl,
} from "@angular/forms";
import { MatTableModule, MatTableDataSource } from "@angular/material/table";
import { MatPaginatorModule, MatPaginator } from "@angular/material/paginator";
import { MatSortModule, MatSort } from "@angular/material/sort";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatCardModule } from "@angular/material/card";
import { MatChipsModule } from "@angular/material/chips";
import { User } from "../../../models/user.model";
import { UserDialogComponent } from "./user-dialog/user-dialog.component";
import { AdminService } from "../../services/admin.service";
import { UpdateRolesDialogComponent } from "./update-roles-dialog/update-roles-dialog.component";

@Component({
  selector: "app-user-management",
  templateUrl: "./user-management.component.html",
  styleUrls: ["./user-management.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCardModule,
    MatChipsModule,
    MatChipsModule,
  ],
})
export class UserManagementComponent implements OnInit {
  displayedColumns: string[] = [
    /*'id',*/ "username",
    "fullName",
    "email",
    "roles",
    "actions",
  ];
  dataSource: MatTableDataSource<any>;
  userForm: FormGroup;
  totalItems = 0;
  pageSize = 10;
  pageSizeOptions = [5, 10, 25, 50];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private adminService: AdminService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.dataSource = new MatTableDataSource();
    this.userForm = new FormGroup({
      username: new FormControl("", [Validators.required]),
      email: new FormControl("", [Validators.required, Validators.email]),
      role: new FormControl("", [Validators.required]),
      password: new FormControl("", [Validators.required]),
    });
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(page: number = 0): void {
    this.adminService.getUsers(page, this.pageSize).subscribe({
      next: (response) => {
        this.dataSource.data = response.data.content;
        this.totalItems = response.data.totalElements;
      },
      error: (error) => {
        this.snackBar.open("Error loading users: " + error.message, "Close", {
          duration: 3000,
        });
      },
    });
  }

  onPageChange(event: any): void {
    this.pageSize = event.pageSize;
    this.loadUsers(event.pageIndex);
  }

  updateUserRoles(userId: string, currentRoles: string[]): void {
    const dialogRef = this.dialog.open(UpdateRolesDialogComponent, {
      data: { userId, currentRoles },
    });

    dialogRef.afterClosed().subscribe((roles) => {
      if (roles) {
        this.adminService.updateUserRoles(userId, roles).subscribe({
          next: () => {
            this.snackBar.open("User roles updated successfully", "Close", {
              duration: 3000,
            });
            this.loadUsers();
          },
          error: (error) => {
            this.snackBar.open(
              "Error updating user roles: " + error.message,
              "Close",
              {
                duration: 3000,
              }
            );
          },
        });
      }
    });
  }

  deleteUser(userId: string): void {
    if (confirm("Are you sure you want to delete this user?")) {
      this.adminService.deleteUser(userId).subscribe({
        next: () => {
          this.snackBar.open("User deleted successfully", "Close", {
            duration: 3000,
          });
          this.loadUsers();
        },
        error: (error) => {
          this.snackBar.open("Error deleting user: " + error.message, "Close", {
            duration: 3000,
          });
        },
      });
    }
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  openCreateUserDialog(): void {
    const dialogRef = this.dialog.open(UserDialogComponent, {
      width: "500px",
      data: { user: null },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.adminService.createUser(result).subscribe({
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
        this.adminService.updateUser(result.id, result).subscribe({
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
  onSubmit(): void {
    if (this.userForm.valid) {
      this.adminService
        .updateUser(this.userForm.value.id, this.userForm.value)
        .subscribe({
          next: () => {
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
  }
}
