import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NovelService } from '../../services/novel.service';
import { ChapterService } from '../../services/chapter.service';
import { Novel, Chapter, PaginatedResponse } from '../../models/novel.model';

@Component({
  selector: 'app-novel-detail',
  templateUrl: './novel-detail.component.html',
  styleUrls: ['./novel-detail.component.css']
})
export class NovelDetailComponent implements OnInit {
  novel: Novel | null = null;
  chapters: Chapter[] = [];
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  selectedChapter: Chapter | null = null;

  constructor(
    private route: ActivatedRoute,
    private novelService: NovelService,
    private chapterService: ChapterService
  ) { }

  ngOnInit(): void {
    const novelId = Number(this.route.snapshot.paramMap.get('id'));
    if (novelId) {
      this.loadNovel(novelId);
      this.loadChapters(novelId);
    }
  }

  loadNovel(novelId: number): void {
    this.loading = true;
    this.novelService.getNovelById(novelId)
      .subscribe({
        next: (novel) => {
          this.novel = novel;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading novel:', error);
          this.loading = false;
        }
      });
  }

  loadChapters(novelId: number): void {
    this.loading = true;
    this.chapterService.getChaptersByNovel(novelId, this.currentPage, this.pageSize)
      .subscribe({
        next: (response: PaginatedResponse<Chapter>) => {
          this.chapters = response.content;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading chapters:', error);
          this.loading = false;
        }
      });
  }

  onPageChange(event: any): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    if (this.novel) {
      this.loadChapters(this.novel.id);
    }
  }

  selectChapter(chapter: Chapter): void {
    this.selectedChapter = chapter;
  }
} 