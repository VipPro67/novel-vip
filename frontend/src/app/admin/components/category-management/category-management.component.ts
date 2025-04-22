import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminService } from '../../services/admin.service';
import { Category } from '../../../dtos/category.dto';
import { CategoryDialogComponent } from './category-dialog/category-dialog.component';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-category-management',
  templateUrl: './category-management.component.html',
  styleUrls: ['./category-management.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
  ]
})
export class CategoryManagementComponent implements OnInit {
  displayedColumns: string[] = ['name', 'slug', 'description', 'actions'];
  dataSource: MatTableDataSource<Category>;
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
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(page: number = 0): void {
    this.adminService.getCategories(page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.dataSource.data = response.data.content;
          this.totalItems = response.data.totalElements;
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error loading categories: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  onPageChange(event: any): void {
    this.pageSize = event.pageSize;
    this.loadCategories(event.pageIndex);
  }

  createCategory(): void {
    const dialogRef = this.dialog.open(CategoryDialogComponent, {
      data: null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.createCategory(result).subscribe({
          next: (response) => {
            if (response.success) {
              this.snackBar.open('Category created successfully', 'Close', { duration: 3000 });
              this.loadCategories();
            } else {
              this.snackBar.open(response.message, 'Close', { duration: 3000 });
            }
          },
          error: (error) => {
            this.snackBar.open('Error creating category: ' + error.message, 'Close', {
              duration: 3000
            });
          }
        });
      }
    });
  }

  editCategory(category: Category): void {
    const dialogRef = this.dialog.open(CategoryDialogComponent, {
      data: category
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.updateCategory(category.id, result).subscribe({
          next: (response) => {
            if (response.success) {
              this.snackBar.open('Category updated successfully', 'Close', { duration: 3000 });
              this.loadCategories();
            } else {
              this.snackBar.open(response.message, 'Close', { duration: 3000 });
            }
          },
          error: (error) => {
            this.snackBar.open('Error updating category: ' + error.message, 'Close', {
              duration: 3000
            });
          }
        });
      }
    });
  }

  deleteCategory(category: Category): void {
    if (confirm(`Are you sure you want to delete category "${category.name}"?`)) {
      this.adminService.deleteCategory(category.id).subscribe({
        next: (response) => {
          if (response.success) {
            this.snackBar.open('Category deleted successfully', 'Close', { duration: 3000 });
            this.loadCategories();
          } else {
            this.snackBar.open(response.message, 'Close', { duration: 3000 });
          }
        },
        error: (error) => {
          this.snackBar.open('Error deleting category: ' + error.message, 'Close', {
            duration: 3000
          });
        }
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
} 