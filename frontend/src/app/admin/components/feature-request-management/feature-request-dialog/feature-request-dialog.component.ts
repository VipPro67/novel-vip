import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { FeatureRequestDTO, FeatureRequestStatus } from '../../../../dtos/feature-request.dto';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-feature-request-dialog',
  templateUrl: './feature-request-dialog.component.html',
  styleUrls: ['./feature-request-dialog.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
  ]
})
export class FeatureRequestDialogComponent {
  requestForm: FormGroup;
  statusOptions: FeatureRequestStatus[] = ['VOTING', 'PROCESSING', 'DONE', 'REJECTED'];

  constructor(
    private dialogRef: MatDialogRef<FeatureRequestDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: FeatureRequestDTO,
    private fb: FormBuilder
  ) {
    this.requestForm = this.fb.group({
      status: [data.status, Validators.required]
    });
  }

  onSubmit(): void {
    if (this.requestForm.valid) {
      this.dialogRef.close(this.requestForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
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