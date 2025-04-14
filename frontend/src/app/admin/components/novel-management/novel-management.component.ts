import { Component, OnInit, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { NovelService } from "../../../services/novel.service";
import { Novel } from "../../../models/novel.model";
import { NovelDialogComponent } from "../novel-dialog/novel-dialog.component";
import { ChapterDialogComponent } from "../chapter-dialog/chapter-dialog.component";

@Component({
  selector: "app-novel-management",
  template: `
    <div class="mat-elevation-z8">
      <div class="header">
        <h2>Novel Management</h2>
        <button mat-raised-button color="primary" (click)="openCreateNovelDialog()">
          <mat-icon>add</mat-icon>
          Create Novel
        </button>
      </div>

      <table mat-table [dataSource]="dataSource" matSort>
        <ng-container matColumnDef="title">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Title</th>
          <td mat-cell *matCellDef="let novel">{{ novel.title }}</td>
        </ng-container>

        <ng-container matColumnDef="author">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Author</th>
          <td mat-cell *matCellDef="let novel">{{ novel.author }}</td>
        </ng-container>

        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
          <td mat-cell *matCellDef="let novel">{{ novel.status }}</td>
        </ng-container>

        <ng-container matColumnDef="chapters">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Chapters</th>
          <td mat-cell *matCellDef="let novel">{{ novel.chapters.length }}</td>
        </ng-container>

        <ng-container matColumnDef="views">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Views</th>
          <td mat-cell *matCellDef="let novel">{{ novel.views }}</td>
        </ng-container>

        <ng-container matColumnDef="rating">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Rating</th>
          <td mat-cell *matCellDef="let novel">{{ novel.rating }}</td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let novel">
            <button mat-icon-button color="primary" (click)="openEditNovelDialog(novel)">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button color="accent" (click)="openAddChapterDialog(novel)">
              <mat-icon>add_circle</mat-icon>
            </button>
            <button mat-icon-button color="warn" (click)="deleteNovel(novel)">
              <mat-icon>delete</mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
      </table>

      <mat-paginator [pageSizeOptions]="[5, 10, 25, 100]" aria-label="Select page of novels"></mat-paginator>
    </div>
  `,
  styles: [`
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
  `],
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule
  ]
})
export class NovelManagementComponent implements OnInit {
  displayedColumns: string[] = [
    "title",
    "author",
    "status",
    "chapters",
    "views",
    "rating",
    "actions"
  ];
  dataSource: MatTableDataSource<Novel>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private novelService: NovelService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.dataSource = new MatTableDataSource<Novel>([]);
  }

  ngOnInit(): void {
    this.loadNovels();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadNovels(): void {
    this.novelService.getNovels().subscribe({
      next: (response) => {
        if (response.success) {
          this.dataSource.data = response.data.content;
          this.paginator.length = response.data.totalElements;
        } else {
          this.snackBar.open("Error loading novels", "Close", { duration: 3000 });
        }
      },
      error: (error: Error) => {
        this.snackBar.open("Error loading novels", "Close", { duration: 3000 });
        console.error("Error loading novels:", error);
      }
    });
  }

  openCreateNovelDialog(): void {
    const dialogRef = this.dialog.open(NovelDialogComponent, {
      width: "500px",
      data: { novel: null }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.novelService.createNovel(result).subscribe({
          next: () => {
            this.loadNovels();
            this.snackBar.open("Novel created successfully", "Close", { duration: 3000 });
          },
          error: (error: Error) => {
            this.snackBar.open("Error creating novel", "Close", { duration: 3000 });
            console.error("Error creating novel:", error);
          }
        });
      }
    });
  }

  openEditNovelDialog(novel: Novel): void {
    const dialogRef = this.dialog.open(NovelDialogComponent, {
      width: "500px",
      data: { novel }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.novelService.updateNovel(result.id, result).subscribe({
          next: () => {
            this.loadNovels();
            this.snackBar.open("Novel updated successfully", "Close", { duration: 3000 });
          },
          error: (error: Error) => {
            this.snackBar.open("Error updating novel", "Close", { duration: 3000 });
            console.error("Error updating novel:", error);
          }
        });
      }
    });
  }

  openAddChapterDialog(novel: Novel): void {
    const dialogRef = this.dialog.open(ChapterDialogComponent, {
      width: "500px",
      data: { chapter: null, novelId: novel.id }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.novelService.createChapter(novel.id, result).subscribe({
          next: () => {
            this.loadNovels();
            this.snackBar.open("Chapter added successfully", "Close", { duration: 3000 });
          },
          error: (error: Error) => {
            this.snackBar.open("Error adding chapter", "Close", { duration: 3000 });
            console.error("Error adding chapter:", error);
          }
        });
      }
    });
  }

  deleteNovel(novel: Novel): void {
    if (confirm(`Are you sure you want to delete novel ${novel.title}?`)) {
      this.novelService.deleteNovel(novel.id).subscribe({
        next: () => {
          this.loadNovels();
          this.snackBar.open("Novel deleted successfully", "Close", { duration: 3000 });
        },
        error: (error: Error) => {
          this.snackBar.open("Error deleting novel", "Close", { duration: 3000 });
          console.error("Error deleting novel:", error);
        }
      });
    }
  }
}
