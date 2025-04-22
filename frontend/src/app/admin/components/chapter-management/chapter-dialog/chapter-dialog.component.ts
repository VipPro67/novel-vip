import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { ChapterListDTO, ChapterCreateDTO } from '../../../../dtos/chapter.dto';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-chapter-dialog',
  templateUrl: './chapter-dialog.component.html',
  styleUrls: ['./chapter-dialog.component.scss'],
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
export class ChapterDialogComponent {
  chapterForm: FormGroup;
  isEditing: boolean;

  constructor(
    private dialogRef: MatDialogRef<ChapterDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      novelId: string;
      chapter: ChapterCreateDTO | null;
      nextChapterNumber: number;
    },
    private fb: FormBuilder
  ) {
    this.isEditing = !!data.chapter;
    this.chapterForm = this.fb.group({
      chapterNumber: [
        this.isEditing ? data.chapter?.chapterNumber : data.nextChapterNumber,
        [Validators.required, Validators.min(1)]
      ],
      title: [
        this.isEditing ? data.chapter?.title : '',
        [Validators.required, Validators.minLength(1)]
      ],
      content: [
        this.isEditing ? data.chapter?.content : '',
        [Validators.required, Validators.minLength(10)]
      ],
      novelId: [data.novelId]
    });
  }

  onSubmit(): void {
    if (this.chapterForm.valid) {
      const chapterData: ChapterCreateDTO = this.chapterForm.value;
      this.dialogRef.close(chapterData);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
} 