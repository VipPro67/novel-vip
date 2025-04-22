import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { AdminService } from '../../services/admin.service';
import { NovelDTO, NovelCreateDTO, NovelUpdateDTO } from '../../../dtos/novel.dto';
import { NovelDialogComponent } from './novel-dialog/novel-dialog.component';

@Component({
  selector: 'app-novel-management',
  templateUrl: './novel-management.component.html',
  styleUrls: ['./novel-management.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatPaginatorModule,
    MatSortModule,
    MatTableModule,
    MatDialogModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    NovelDialogComponent
  ]
})
export class NovelManagementComponent implements OnInit {
  displayedColumns: string[] = [
    'title',
    'author',
    'status',
    'categories',
    'totalChapters',
    'views',
    'rating',
    'actions'
  ];
  dataSource: MatTableDataSource<NovelDTO>;
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
    this.loadNovels();
  }

  loadNovels(page: number = 0): void {
    this.adminService.getNovels(page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.dataSource.data = response.data.content;
          this.totalItems = response.data.totalElements;
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error loading novels: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  onPageChange(event: any): void {
    this.pageSize = event.pageSize;
    this.loadNovels(event.pageIndex);
  }

  openNovelDialog(novel?: NovelDTO): void {
    const dialogRef = this.dialog.open(NovelDialogComponent, {
      width: '600px',
      data: novel || null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (novel) {
          this.updateNovel(novel.id, result);
        } else {
          this.createNovel(result);
        }
      }
    });
  }


  createNovel(novelData: NovelCreateDTO): void {
    this.adminService.createNovel(novelData).subscribe({
      next: (response) => {
        if (response.success) {
          this.snackBar.open('Novel created successfully', 'Close', { duration: 3000 });
          this.loadNovels();
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error creating novel: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  updateNovel(id: string, novelData: NovelUpdateDTO): void {
    this.adminService.updateNovel(id, novelData).subscribe({
      next: (response) => {
        if (response.success) {
          this.snackBar.open('Novel updated successfully', 'Close', { duration: 3000 });
          this.loadNovels();
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error updating novel: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  deleteNovel(id: string): void {
    if (confirm('Are you sure you want to delete this novel?')) {
      this.adminService.deleteNovel(id).subscribe({
        next: (response) => {
          if (response.success) {
            this.snackBar.open('Novel deleted successfully', 'Close', { duration: 3000 });
            this.loadNovels();
          } else {
            this.snackBar.open(response.message, 'Close', { duration: 3000 });
          }
        },
        error: (error) => {
          this.snackBar.open('Error deleting novel: ' + error.message, 'Close', {
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
  editNovel(novel: NovelDTO): void {
    this.openNovelDialog(novel);
  }
} 