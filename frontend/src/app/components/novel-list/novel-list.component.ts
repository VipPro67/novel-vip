import { Component, OnInit } from '@angular/core';
import { NovelService } from '../../services/novel.service';
import { Novel, PaginatedResponse } from '../../models/novel.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-novel-list',
  templateUrl: './novel-list.component.html',
  styleUrls: ['./novel-list.component.css']
})
export class NovelListComponent implements OnInit {
  novels: Novel[] = [];
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  loading = false;

  constructor(
    private novelService: NovelService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadNovels();
  }

  loadNovels(): void {
    this.loading = true;
    this.novelService.getAllNovels(this.currentPage, this.pageSize)
      .subscribe({
        next: (response: PaginatedResponse<Novel>) => {
          this.novels = response.content;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading novels:', error);
          this.loading = false;
        }
      });
  }

  onPageChange(event: any): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadNovels();
  }

  viewNovel(novelId: number): void {
    this.router.navigate(['/novel', novelId]);
  }
} 