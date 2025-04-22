import { Component, Inject, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { NovelDTO, NovelCreateDTO, NovelUpdateDTO } from '../../../../dtos/novel.dto';

@Component({
  selector: 'app-novel-dialog',
  templateUrl: './novel-dialog.component.html',
  styleUrls: ['./novel-dialog.component.scss'],
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
    MatChipsModule,
    MatLabel
  ]
})
export class NovelDialogComponent {
  novelForm: FormGroup;
  genres: string[] = [];
  tags: string[] = [];
  categories: string[] = [];
  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  statuses = ['ONGOING', 'COMPLETED', 'HIATUS', 'DROPPED'] as const;
  isEditing = false;

  @ViewChild('genreInput') genreInput!: ElementRef<HTMLInputElement>;
  @ViewChild('tagInput') tagInput!: ElementRef<HTMLInputElement>;
  @ViewChild('categoryInput') categoryInput!: ElementRef<HTMLInputElement>;

  constructor(
    private dialogRef: MatDialogRef<NovelDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: NovelDTO | null,
    private fb: FormBuilder
  ) {
    this.novelForm = this.fb.group({
      title: [data?.title || '', [Validators.required, Validators.minLength(3)]],
      description: [data?.description || '', [Validators.required, Validators.minLength(10)]],
      status: [data?.status || 'ONGOING', Validators.required],
      categories: [data?.categories || []],
      genres: [data?.genres || []],
      tags: [data?.tags || []]
    });

    if (data) {
      this.categories = [...data.categories];
      this.genres = [...data.genres];
      this.tags = [...data.tags];
    }
  }

  addGenre(event: any): void {
    const value = (event.value || '').trim();
    if (value) {
      this.genres.push(value);
      this.novelForm.patchValue({ genres: this.genres });
    }
    event.chipInput!.clear();
  }

  removeGenre(genre: string): void {
    const index = this.genres.indexOf(genre);
    if (index >= 0) {
      this.genres.splice(index, 1);
      this.novelForm.patchValue({ genres: this.genres });
    }
  }

  addTag(event: any): void {
    const value = (event.value || '').trim();
    if (value) {
      this.tags.push(value);
      this.novelForm.patchValue({ tags: this.tags });
    }
    event.chipInput!.clear();
  }

  removeTag(tag: string): void {
    const index = this.tags.indexOf(tag);
    if (index >= 0) {
      this.tags.splice(index, 1);
      this.novelForm.patchValue({ tags: this.tags });
    }
  }

  addCategory(event: any): void {
    const value = (event.value || '').trim();
    if (value) {
      this.categories.push(value);
      this.novelForm.patchValue({ categories: this.categories });
    }
    event.chipInput!.clear();
  }

  removeCategory(category: string): void {
    const index = this.categories.indexOf(category);
    if (index >= 0) {
      this.categories.splice(index, 1);
      this.novelForm.patchValue({ categories: this.categories });
    }
  }

  onSubmit(): void {
    if (this.novelForm.valid) {
      const formValue = this.novelForm.value;
      formValue.categories = this.categories;
      formValue.genres = this.genres;
      formValue.tags = this.tags;

      // If we're editing, return NovelUpdateDTO, otherwise return NovelCreateDTO
      if (this.data) {
        const updateData: NovelUpdateDTO = {
          title: formValue.title !== this.data.title ? formValue.title : undefined,
          description: formValue.description !== this.data.description ? formValue.description : undefined,
          status: formValue.status !== this.data.status ? formValue.status : undefined,
          categories: JSON.stringify(formValue.categories) !== JSON.stringify(this.data.categories) ? formValue.categories : undefined,
          genres: JSON.stringify(formValue.genres) !== JSON.stringify(this.data.genres) ? formValue.genres : undefined,
          tags: JSON.stringify(formValue.tags) !== JSON.stringify(this.data.tags) ? formValue.tags : undefined
        };
        this.dialogRef.close(updateData);
      } else {
        const createData: NovelCreateDTO = {
          title: formValue.title,
          description: formValue.description,
          status: formValue.status,
          categories: formValue.categories,
          genres: formValue.genres,
          tags: formValue.tags
        };
        this.dialogRef.close(createData);
      }
    }
  }

  generateSlug(): void {
    const title = this.novelForm.get('title')?.value;
    if (title) {
      this.novelForm.patchValue({ slug: title.toLowerCase().replace(/ /g, '-') });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
} 
