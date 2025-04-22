import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminService } from '../../services/admin.service';
import { FeatureRequestDTO, FeatureRequestStatus } from '../../../dtos/feature-request.dto';
import { FeatureRequestDialogComponent } from './feature-request-dialog/feature-request-dialog.component';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
@Component({
  selector: 'app-feature-request-management',
  templateUrl: './feature-request-management.component.html',
  styleUrls: ['./feature-request-management.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatLabel,
    FormsModule
  ]
})
export class FeatureRequestManagementComponent implements OnInit {
  displayedColumns: string[] = [
    'title',
    'username',
    'status',
    'voteCount',
    'actions'
  ];
  dataSource: MatTableDataSource<FeatureRequestDTO>;
  totalItems = 0;
  pageSize = 10;
  pageSizeOptions = [5, 10, 25, 50];
  statusFilter: FeatureRequestStatus = 'VOTING';
  statusOptions: FeatureRequestStatus[] = ['VOTING', 'PROCESSING', 'DONE', 'REJECTED'];

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
    this.loadFeatureRequests();
  }

  loadFeatureRequests(page: number = 0): void {
    if (this.statusFilter) {
      this.loadFeatureRequestsByStatus(page);
    } else {
      this.loadAllFeatureRequests(page);
    }
  }

  loadAllFeatureRequests(page: number): void {
    this.adminService.getFeatureRequests(page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.dataSource.data = response.data.content;
          this.totalItems = response.data.totalElements;
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error loading feature requests: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  loadFeatureRequestsByStatus(page: number): void {
    this.adminService.getFeatureRequestsByStatus(this.statusFilter, page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.dataSource.data = response.data.content;
          this.totalItems = response.data.totalElements;
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error loading feature requests: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  onPageChange(event: any): void {
    this.pageSize = event.pageSize;
    this.loadFeatureRequests(event.pageIndex);
  }

  onStatusFilterChange(): void {
    this.loadFeatureRequests();
  }

  viewFeatureRequest(request: FeatureRequestDTO): void {
    const dialogRef = this.dialog.open(FeatureRequestDialogComponent, {
      data: request,
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.updateFeatureRequestStatus(request.id, result.status).subscribe({
          next: (response) => {
            if (response.success) {
              this.snackBar.open('Feature request status updated successfully', 'Close', { duration: 3000 });
              this.loadFeatureRequests();
            } else {
              this.snackBar.open(response.message, 'Close', { duration: 3000 });
            }
          },
          error: (error) => {
            this.snackBar.open('Error updating feature request status: ' + error.message, 'Close', {
              duration: 3000
            });
          }
        });
      }
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  getStatusColor(status: FeatureRequestStatus): string {
    switch (status) {
      case 'VOTING':
        return 'accent';
      case 'PROCESSING':
        return 'primary';
      case 'DONE':
        return 'primary';
      case 'REJECTED':
        return 'warn';
      default:
        return '';
    }
  }
} 