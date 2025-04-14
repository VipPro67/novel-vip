import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { User } from '../../../models/user.model';

@Component({
  selector: 'app-user-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.user ? 'Edit User' : 'Create User' }}</h2>
    <form [formGroup]="userForm" (ngSubmit)="onSubmit()">
      <mat-dialog-content>
        <mat-form-field appearance="fill" class="w-full">
          <mat-label>Username</mat-label>
          <input matInput formControlName="username" required>
          <mat-error *ngIf="userForm.get('username')?.hasError('required')">
            Username is required
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="fill" class="w-full">
          <mat-label>Email</mat-label>
          <input matInput formControlName="email" required type="email">
          <mat-error *ngIf="userForm.get('email')?.hasError('required')">
            Email is required
          </mat-error>
          <mat-error *ngIf="userForm.get('email')?.hasError('email')">
            Please enter a valid email
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="fill" class="w-full">
          <mat-label>Role</mat-label>
          <mat-select formControlName="role" required>
            <mat-option value="admin">Admin</mat-option>
            <mat-option value="user">User</mat-option>
          </mat-select>
          <mat-error *ngIf="userForm.get('role')?.hasError('required')">
            Role is required
          </mat-error>
        </mat-form-field>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button type="button" (click)="onCancel()">Cancel</button>
        <button mat-raised-button color="primary" type="submit" [disabled]="userForm.invalid">
          {{ data.user ? 'Update' : 'Create' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
      max-width: 500px;
    }
    mat-dialog-content {
      padding: 20px 0;
    }
    mat-form-field {
      width: 100%;
      margin-bottom: 16px;
    }
  `]
})
export class UserDialogComponent {
  userForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<UserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user?: User }
  ) {
    this.userForm = this.fb.group({
      username: [data.user?.username || '', [Validators.required]],
      email: [data.user?.email || '', [Validators.required, Validators.email]],
      role: [data.user?.roles || 'user', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      this.dialogRef.close(this.userForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
} 