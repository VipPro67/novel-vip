import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminService } from '../../services/admin.service';
import { SubscriptionDTO, SubscriptionPlanDTO } from '../../../dtos/subscription.dto';
import { SubscriptionDialogComponent } from './subscription-dialog/subscription-dialog.component';

@Component({
  selector: 'app-subscription-management',
  templateUrl: './subscription-management.component.html',
  styleUrls: ['./subscription-management.component.scss']
})
export class SubscriptionManagementComponent implements OnInit {
  displayedColumns: string[] = [
    'username',
    'planName',
    'status',
    'startDate',
    'endDate',
    'paymentMethod',
    'actions'
  ];
  dataSource: MatTableDataSource<SubscriptionDTO>;
  totalItems = 0;
  pageSize = 10;
  pageSizeOptions = [5, 10, 25, 50];
  subscriptionPlans: SubscriptionPlanDTO[] = [];

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
    this.loadSubscriptionPlans();
    this.loadSubscriptions();
  }

  loadSubscriptionPlans(): void {
    this.adminService.getSubscriptionPlans().subscribe({
      next: (response) => {
        if (response.success) {
          this.subscriptionPlans = response.data;
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error loading subscription plans: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  loadSubscriptions(page: number = 0): void {
    this.adminService.getSubscriptionHistory(page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.dataSource.data = response.data.content;
          this.totalItems = response.data.totalElements;
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error loading subscriptions: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  onPageChange(event: any): void {
    this.pageSize = event.pageSize;
    this.loadSubscriptions(event.pageIndex);
  }

  viewSubscription(subscription: SubscriptionDTO): void {
    const dialogRef = this.dialog.open(SubscriptionDialogComponent, {
      data: {
        subscription,
        plans: this.subscriptionPlans
      },
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Handle subscription update if needed
        this.loadSubscriptions();
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
    switch (status.toUpperCase()) {
      case 'ACTIVE':
        return 'primary';
      case 'CANCELLED':
        return 'warn';
      case 'EXPIRED':
        return 'accent';
      default:
        return '';
    }
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString();
  }
} 