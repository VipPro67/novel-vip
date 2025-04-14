import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Novel } from '../../../models/novel.model';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
@Component({
  selector: 'app-novel-dialog',
  template: `
    <h2 mat-dialog-title>{{ data.novel ? 'Edit' : 'Create' }} Novel</h2>
    <form [formGroup]="novelForm" (ngSubmit)="onSubmit()">
      <mat-dialog-content>
        <mat-form-field appearance="fill">
          <mat-label>Title</mat-label>
          <input matInput formControlName="title" required>
        </mat-form-field>

        <mat-form-field appearance="fill">
          <mat-label>Author</mat-label>
          <input matInput formControlName="author" required>
        </mat-form-field>

        <mat-form-field appearance="fill">
          <mat-label>Description</mat-label>
          <textarea matInput formControlName="description" required rows="4"></textarea>
        </mat-form-field>

        <mat-form-field appearance="fill">
          <mat-label>Cover Image URL</mat-label>
          <input matInput formControlName="coverImage">
        </mat-form-field>

        <mat-form-field appearance="fill">
          <mat-label>Status</mat-label>
          <mat-select formControlName="status" required>
            <mat-option value="draft">Draft</mat-option>
            <mat-option value="ongoing">Ongoing</mat-option>
            <mat-option value="completed">Completed</mat-option>
          </mat-select>
        </mat-form-field>

      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button type="button" (click)="onCancel()">Cancel</button>
        <button mat-raised-button color="primary" type="submit" [disabled]="!novelForm.valid">
          {{ data.novel ? 'Update' : 'Create' }}
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
    MatInputModule,
    MatSelectModule,
    MatAutocompleteModule
  ]
})
export class NovelDialogComponent {
  novelForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<NovelDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { novel: Novel | null }
  ) {
    this.novelForm = this.fb.group({
      id: [data.novel?.id || ''],
      title: [data.novel?.title || '', Validators.required],
      author: [data.novel?.author || '', Validators.required],
      description: [data.novel?.description || '', Validators.required],
      coverImage: [data.novel?.coverImage || ''],
      status: [data.novel?.status || 'draft', Validators.required],
      categories: [data.novel?.categories || [], Validators.required]
    });
  }

  onSubmit(): void {
    if (this.novelForm.valid) {
      this.dialogRef.close(this.novelForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
} 