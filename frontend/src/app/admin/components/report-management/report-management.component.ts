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
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AdminService } from '../../services/admin.service';
import { ReportDTO, ReportUpdateDTO } from '../../../dtos/report.dto';
import { ReportDialogComponent } from './report-dialog/report-dialog.component';

@Component({
  selector: 'app-report-management',
  templateUrl: './report-management.component.html',
  styleUrls: ['./report-management.component.scss'],
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
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatButtonModule,
    ReportDialogComponent
  ]
})
export class ReportManagementComponent implements OnInit {
  displayedColumns: string[] = [
    'reporterUsername',
    'reason',
    'status',
    'createdAt',
    'resolvedAt',
    'actions'
  ];
  dataSource: MatTableDataSource<ReportDTO>;
  totalItems = 0;
  pageSize = 10;
  pageSizeOptions = [5, 10, 25, 50];
  statusFilter: string = 'PENDING';

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
    this.loadReports();
  }

  loadReports(page: number = 0): void {
    if (this.statusFilter === 'PENDING') {
      this.loadPendingReports(page);
    } else {
      this.loadAllReports(page);
    }
  }

  loadAllReports(page: number): void {
    this.adminService.getReports(page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.dataSource.data = response.data.content;
          this.totalItems = response.data.totalElements;
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error loading reports: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  loadPendingReports(page: number): void {
    this.adminService.getPendingReports(page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.dataSource.data = response.data.content;
          this.totalItems = response.data.totalElements;
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error loading pending reports: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  onPageChange(event: any): void {
    this.pageSize = event.pageSize;
    this.loadReports(event.pageIndex);
  }

  onStatusFilterChange(): void {
    this.loadReports();
  }

  viewReport(report: ReportDTO): void {
    const dialogRef = this.dialog.open(ReportDialogComponent, {
      data: report,
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const updateData: ReportUpdateDTO = {
          status: result.status,
          adminResponse: result.adminResponse
        };

        this.adminService.updateReportStatus(report.id, updateData).subscribe({
          next: (response) => {
            if (response.success) {
              this.snackBar.open('Report status updated successfully', 'Close', { duration: 3000 });
              this.loadReports();
            } else {
              this.snackBar.open(response.message, 'Close', { duration: 3000 });
            }
          },
          error: (error) => {
            this.snackBar.open('Error updating report status: ' + error.message, 'Close', {
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

  getStatusColor(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'accent';
      case 'RESOLVED':
        return 'primary';
      case 'REJECTED':
        return 'warn';
      default:
        return '';
    }
  }
} 