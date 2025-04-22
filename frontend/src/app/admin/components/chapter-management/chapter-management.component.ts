import { Component, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminService } from '../../services/admin.service';
import { NovelDTO } from '../../../dtos/novel.dto';
import { ChapterListDTO } from '../../../dtos/chapter.dto';
import { ChapterDialogComponent } from './chapter-dialog/chapter-dialog.component';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-chapter-management',
  templateUrl: './chapter-management.component.html',
  styleUrls: ['./chapter-management.component.scss'],
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
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCardModule,
    MatChipsModule,
    ChapterDialogComponent
  ]
})
export class ChapterManagementComponent implements OnInit {
  displayedColumns: string[] = [
    'chapterNumber',
    'title',
    'createdAt',
    'updatedAt',
    'actions'
  ];
  dataSource: MatTableDataSource<ChapterListDTO>;
  totalItems = 0;
  pageSize = 10;
  pageSizeOptions = [5, 10, 25, 50];
  selectedNovel: NovelDTO | null = null;
  novelControl = new FormControl('');
  novels: NovelDTO[] = [];

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
    this.novelControl.valueChanges.subscribe(novelId => {
      if (novelId) {
        this.selectedNovel = this.novels.find(n => n.id === novelId) || null;
        this.loadChapters();
      } else {
        this.selectedNovel = null;
        this.dataSource.data = [];
      }
    });
  }

  loadNovels(): void {
    this.adminService.getNovels(0, 1000).subscribe({
      next: (response) => {
        if (response.success) {
          this.novels = response.data.content;
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

  loadChapters(page: number = 0): void {
    if (!this.selectedNovel) return;

    this.adminService.getChaptersByNovel(this.selectedNovel.id, page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.dataSource.data = response.data.content;
          this.totalItems = response.data.totalElements;
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error loading chapters: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  onPageChange(event: any): void {
    this.pageSize = event.pageSize;
    this.loadChapters(event.pageIndex);
  }

  createChapter(): void {
    if (!this.selectedNovel) {
      this.snackBar.open('Please select a novel first', 'Close', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(ChapterDialogComponent, {
      data: {
        novelId: this.selectedNovel.id,
        chapter: null,
        nextChapterNumber: this.getNextChapterNumber()
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.createChapter(result).subscribe({
          next: (response) => {
            if (response.success) {
              this.snackBar.open('Chapter created successfully', 'Close', { duration: 3000 });
              this.loadChapters();
            } else {
              this.snackBar.open(response.message, 'Close', { duration: 3000 });
            }
          },
          error: (error) => {
            this.snackBar.open('Error creating chapter: ' + error.message, 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  editChapter(chapter: ChapterListDTO): void {
    if (!this.selectedNovel) return;

    const dialogRef = this.dialog.open(ChapterDialogComponent, {
      data: {
        novelId: this.selectedNovel.id,
        chapter,
        nextChapterNumber: chapter.chapterNumber
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.updateChapter(chapter.id, result).subscribe({
          next: (response) => {
            if (response.success) {
              this.snackBar.open('Chapter updated successfully', 'Close', { duration: 3000 });
              this.loadChapters();
            } else {
              this.snackBar.open(response.message, 'Close', { duration: 3000 });
            }
          },
          error: (error) => {
            this.snackBar.open('Error updating chapter: ' + error.message, 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  deleteChapter(chapter: ChapterListDTO): void {
    if (confirm(`Are you sure you want to delete chapter ${chapter.chapterNumber}?`)) {
      this.adminService.deleteChapter(chapter.id).subscribe({
        next: (response) => {
          if (response.success) {
            this.snackBar.open('Chapter deleted successfully', 'Close', { duration: 3000 });
            this.loadChapters();
          } else {
            this.snackBar.open(response.message, 'Close', { duration: 3000 });
          }
        },
        error: (error) => {
          this.snackBar.open('Error deleting chapter: ' + error.message, 'Close', {
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

  private getNextChapterNumber(): number {
    if (!this.dataSource.data.length) return 1;
    return Math.max(...this.dataSource.data.map(c => c.chapterNumber)) + 1;
  }
} 