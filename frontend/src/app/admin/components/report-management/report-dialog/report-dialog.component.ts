import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { DatePipe } from '@angular/common';
import { ReportDTO } from '../../../../dtos/report.dto';

@Component({
  selector: 'app-report-dialog',
  templateUrl: './report-dialog.component.html',
  styleUrls: ['./report-dialog.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    DatePipe
  ]
})
export class ReportDialogComponent {
  reportForm: FormGroup;
  statusOptions = ['PENDING', 'RESOLVED', 'REJECTED'];

  constructor(
    private dialogRef: MatDialogRef<ReportDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ReportDTO,
    private fb: FormBuilder
  ) {
    this.reportForm = this.fb.group({
      status: [data.status, Validators.required],
      adminResponse: [data.adminResponse || '', [Validators.required, Validators.minLength(10)]]
    });
  }

  onSubmit(): void {
    if (this.reportForm.valid) {
      this.dialogRef.close(this.reportForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  getReportedContent(): string {
    if (this.data.novelTitle) {
      return `Novel: ${this.data.novelTitle}`;
    } else if (this.data.chapterTitle) {
      return `Chapter: ${this.data.chapterTitle}`;
    } else if (this.data.commentId) {
      return `Comment ID: ${this.data.commentId}`;
    }
    return 'Unknown content';
  }
}