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
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { AdminService } from '../../services/admin.service';
import { PaymentDTO, PaymentStatsDTO } from '../../../dtos/payment.dto';
import { PaymentDialogComponent } from './payment-dialog/payment-dialog.component';

@Component({
  selector: 'app-payment-management',
  templateUrl: './payment-management.component.html',
  styleUrls: ['./payment-management.component.scss'],
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
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    PaymentDialogComponent
  ]
})
export class PaymentManagementComponent implements OnInit {
  displayedColumns: string[] = [
    'username',
    'amount',
    'status',
    'paymentMethod',
    'completedAt',
    'actions'
  ];
  dataSource: MatTableDataSource<PaymentDTO>;
  totalItems = 0;
  pageSize = 10;
  pageSizeOptions = [5, 10, 25, 50];
  stats: PaymentStatsDTO | null = null;

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
    this.loadPaymentStats();
    this.loadPayments();
  }

  loadPaymentStats(): void {
    this.adminService.getPaymentStats().subscribe({
      next: (response) => {
        if (response.success) {
          this.stats = response.data;
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error loading payment stats: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  loadPayments(page: number = 0): void {
    this.adminService.getPayments(page, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.dataSource.data = response.data.content;
          this.totalItems = response.data.totalElements;
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Error loading payments: ' + error.message, 'Close', {
          duration: 3000
        });
      }
    });
  }

  onPageChange(event: any): void {
    this.pageSize = event.pageSize;
    this.loadPayments(event.pageIndex);
  }

  viewPayment(payment: PaymentDTO): void {
    const dialogRef = this.dialog.open(PaymentDialogComponent, {
      data: payment,
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'cancelled') {
        this.cancelPayment(payment);
      }
    });
  }

  cancelPayment(payment: PaymentDTO): void {
    if (confirm('Are you sure you want to cancel this payment?')) {
      this.adminService.cancelPayment(payment.id).subscribe({
        next: (response) => {
          if (response.success) {
            this.snackBar.open('Payment cancelled successfully', 'Close', { duration: 3000 });
            this.loadPayments();
            this.loadPaymentStats();
          } else {
            this.snackBar.open(response.message, 'Close', { duration: 3000 });
          }
        },
        error: (error) => {
          this.snackBar.open('Error cancelling payment: ' + error.message, 'Close', {
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

  getStatusColor(status: string): string {
    switch (status.toUpperCase()) {
      case 'COMPLETED':
        return 'primary';
      case 'PENDING':
        return 'accent';
      case 'FAILED':
      case 'CANCELLED':
        return 'warn';
      default:
        return '';
    }
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString();
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }
} 