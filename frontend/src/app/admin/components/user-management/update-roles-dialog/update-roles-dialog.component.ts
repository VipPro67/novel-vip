import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { UserDTO } from '../../../../dtos/user.dto';

@Component({
  selector: 'app-update-roles-dialog',
  templateUrl: './update-roles-dialog.component.html',
  styleUrls: ['./update-roles-dialog.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ]
})
export class UpdateRolesDialogComponent {
  roleForm: FormGroup;
  availableRoles = ['USER', 'ADMIN', 'MODERATOR'];

  constructor(
    private dialogRef: MatDialogRef<UpdateRolesDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user: UserDTO },
    private fb: FormBuilder
  ) {
    this.roleForm = this.fb.group({
      roles: [data.user.roles, [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.roleForm.valid) {
      this.dialogRef.close(this.roleForm.value.roles);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
} 