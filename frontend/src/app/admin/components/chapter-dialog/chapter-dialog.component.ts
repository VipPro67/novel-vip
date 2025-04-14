import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ChapterDetail } from '../../../models/novel.model';

@Component({
  selector: 'app-chapter-dialog',
  template: `
    <h2 mat-dialog-title>{{ data.chapter ? 'Edit' : 'Add' }} Chapter</h2>
    <form [formGroup]="chapterForm" (ngSubmit)="onSubmit()">
      <mat-dialog-content>
        <mat-form-field appearance="fill">
          <mat-label>Chapter Number</mat-label>
          <input matInput type="number" formControlName="chapterNumber" required>
        </mat-form-field>

        <mat-form-field appearance="fill">
          <mat-label>Title</mat-label>
          <input matInput formControlName="title" required>
        </mat-form-field>

        <mat-form-field appearance="fill">
          <mat-label>Content</mat-label>
          <textarea matInput formControlName="content" required rows="10"></textarea>
        </mat-form-field>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button type="button" (click)="onCancel()">Cancel</button>
        <button mat-raised-button color="primary" type="submit" [disabled]="!chapterForm.valid">
          {{ data.chapter ? 'Update' : 'Add' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [`
    mat-form-field {
      width: 100%;
      margin-bottom: 16px;
    }
    mat-dialog-content {
      min-width: 400px;
    }
  `],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule
  ]
})
export class ChapterDialogComponent {
  chapterForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ChapterDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { chapter: ChapterDetail | null, novelId: string }
  ) {
    this.chapterForm = this.fb.group({
      id: [data.chapter?.id || ''],
      novelId: [data.novelId],
      chapterNumber: [data.chapter?.chapterNumber || '', [Validators.required, Validators.min(1)]],
      title: [data.chapter?.title || '', Validators.required],
      content: [data.chapter?.content || '', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.chapterForm.valid) {
      this.dialogRef.close(this.chapterForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
} 