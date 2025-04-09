import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpClient } from '@angular/common/http';
import { SanitizeHtmlPipe } from '../../pipes/sanitize-html.pipe';

interface ChapterContent {
  slug: string;
  title: string;
  chapterNumber: number;
  chapterTitle: string;
  content: string;
  createdAt: string;
}

@Component({
  selector: 'app-chapter-content',
  templateUrl: './chapter-content.component.html',
  styleUrls: ['./chapter-content.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    SanitizeHtmlPipe
  ]
})
export class ChapterContentComponent implements OnInit {
  @Input() jsonUrl: string = '';
  @Input() chapterNumber: number = 0;
  @Input() chapterTitle: string = '';
  
  content: ChapterContent | null = null;
  loading: boolean = false;
  error: string | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    if (this.jsonUrl) {
      this.loadChapterContent();
    }
  }

  loadChapterContent(): void {
    this.loading = true;
    this.error = null;
    
    this.http.get<ChapterContent>(this.jsonUrl).subscribe({
      next: (data) => {
        this.content = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Không thể tải nội dung chương. Vui lòng thử lại sau.';
        this.loading = false;
        console.error('Error loading chapter content:', err);
      }
    });
  }
} 