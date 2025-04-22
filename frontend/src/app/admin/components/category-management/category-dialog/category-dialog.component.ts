import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { Category } from '../../../../dtos/category.dto';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-category-dialog',
  templateUrl: './category-dialog.component.html',
  styleUrls: ['./category-dialog.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule,
    MatCardModule,
  ]
})
export class CategoryDialogComponent {
  categoryForm: FormGroup;
  isEditing: boolean;

  constructor(
    private dialogRef: MatDialogRef<CategoryDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Category | null,
    private fb: FormBuilder
  ) {
    this.isEditing = !!data;
    this.categoryForm = this.fb.group({
      name: [data?.name || '', [Validators.required, Validators.minLength(2)]],
      slug: [data?.slug || '', [Validators.required, Validators.pattern('^[a-z0-9]+(?:-[a-z0-9]+)*$')]],
      description: [data?.description || '', [Validators.required, Validators.minLength(10)]]
    });

    // Auto-generate slug from name
    this.categoryForm.get('name')?.valueChanges.subscribe(name => {
      if (!this.isEditing) {
        const slug = name
          .toLowerCase()
          .replace(/[^a-z0-9]+/g, '-')
          .replace(/(^-|-$)/g, '');
        this.categoryForm.patchValue({ slug }, { emitEvent: false });
      }
    });
  }

  onSubmit(): void {
    if (this.categoryForm.valid) {
      const categoryData: Category = {
        ...this.data,
        ...this.categoryForm.value
      };
      this.dialogRef.close(categoryData);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
} 