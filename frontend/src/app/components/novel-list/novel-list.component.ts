import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { NovelService, Novel } from '../../services/novel.service';
import { TruncateWordsPipe } from '../../pipes/truncate-words.pipe';

@Component({
  selector: 'app-novel-list',
  templateUrl: './novel-list.component.html',
  styleUrls: ['./novel-list.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    TruncateWordsPipe
  ]
})
export class NovelListComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  
  novels: Novel[] = [];
  loading = false;
  currentPage = 0;
  pageSize = 12;
  totalElements = 0;
  truncateWords = new TruncateWordsPipe();
  
  searchForm: FormGroup;
  searchKeyword = '';
  private searchSubject = new Subject<string>();
  isSearching = false;

  constructor(
    private novelService: NovelService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.searchForm = this.fb.group({
      searchInput: ['']
    });
  }

  ngOnInit(): void {
    this.loadNovels();
    this.setupSearch();
  }

  setupSearch(): void {
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(keyword => {
        this.isSearching = true;
        this.loading = true;
        return this.novelService.searchNovels(keyword, this.currentPage, this.pageSize);
      })
    ).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.novels = response.data.content;
          this.totalElements = response.data.totalElements;
        }
        this.loading = false;
        this.isSearching = false;
      },
      error: (error) => {
        console.error('Error searching novels:', error);
        this.loading = false;
        this.isSearching = false;
      }
    });
  }

  onSearch(): void {
    const keyword = this.searchForm.get('searchInput')?.value || '';
    this.searchKeyword = keyword;
    this.currentPage = 0;
    
    if (keyword.trim() === '') {
      this.loadNovels();
    } else {
      this.searchSubject.next(keyword);
    }
  }

  clearSearch(): void {
    this.searchForm.patchValue({ searchInput: '' });
    this.searchKeyword = '';
    this.currentPage = 0;
    this.loadNovels();
  }

  loadNovels(): void {
    this.loading = true;
    this.novelService.getNovels(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.novels = response.data.content;
          this.totalElements = response.data.totalElements;
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading novels:', error);
        this.loading = false;
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    
    if (this.searchKeyword) {
      this.searchSubject.next(this.searchKeyword);
    } else {
      this.loadNovels();
    }
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = 'https://res.cloudinary.com/drpudphzv/image/upload/v1/novel/cmmk5yugoqtwzu5axdea';
  }

  viewNovel(novelId: string): void {
    this.router.navigate(['/novels', novelId]);
  }
}
