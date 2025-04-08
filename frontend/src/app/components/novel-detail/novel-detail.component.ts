import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { NovelService } from "../../services/novel.service";
import { ChapterService } from "../../services/chapter.service";
import { SanitizeHtmlPipe } from "../../pipes/sanitize-html.pipe";
import { Novel, Chapter, PaginatedResponse } from "../../models/novel.model";
import { MatPaginatorModule, PageEvent } from "@angular/material/paginator";
import { CommonModule } from "@angular/common";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatListModule } from "@angular/material/list";
import { MatBadgeModule } from "@angular/material/badge";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
@Component({
  selector: "app-novel-detail",
  templateUrl: "./novel-detail.component.html",
  styleUrls: ["./novel-detail.component.css"],
  standalone: true,
  imports: [
    CommonModule,
    MatPaginatorModule,
    MatCardModule,
    MatButtonModule,
    MatListModule,
    MatBadgeModule,
    SanitizeHtmlPipe,
    MatProgressSpinnerModule,
  ],
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
  ) {}

  ngOnInit(): void {
    const novelId = Number(this.route.snapshot.paramMap.get("id"));
    if (novelId) {
      this.loadNovel(novelId);
      this.loadChapters(novelId);
    }
  }

  loadNovel(novelId: number): void {
    this.loading = true;
    this.novelService.getNovelById(novelId).subscribe({
      next: (novel) => {
        this.novel = novel;
        this.loading = false;
      },
      error: (error) => {
        console.error("Error loading novel:", error);
        this.loading = false;
      },
    });
  }

  loadChapters(novelId: number): void {
    this.loading = true;
    this.chapterService
      .getChaptersByNovel(novelId, this.currentPage, this.pageSize)
      .subscribe({
        next: (response: PaginatedResponse<Chapter>) => {
          this.chapters = response.content;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: (error) => {
          console.error("Error loading chapters:", error);
          this.loading = false;
        },
      });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    if (this.novel) {
      this.loadChapters(this.novel.id);
    }
  }

  selectChapter(chapter: Chapter): void {
    this.selectedChapter = chapter;
  }

  goToPreviousChapter(): void {
    if (
      this.selectedChapter &&
      this.novel &&
      this.selectedChapter.chapterNumber > 1
    ) {
      const prevChapterNumber = this.selectedChapter.chapterNumber - 1;
      const prevChapter = this.novel.chapters.find(
        (ch) => ch.chapterNumber === prevChapterNumber
      );
      if (prevChapter) {
        this.selectedChapter = prevChapter;
      } else {
        // Fetch from API if not in current page
        this.loadChapterByNumber(this.novel.id, prevChapterNumber);
      }
    }
  }

  goToNextChapter(): void {
    if (
      this.selectedChapter &&
      this.novel &&
      this.selectedChapter.chapterNumber < this.novel.totalChapters
    ) {
      const nextChapterNumber = this.selectedChapter.chapterNumber + 1;
      const nextChapter = this.novel.chapters.find(
        (ch) => ch.chapterNumber === nextChapterNumber
      );
      if (nextChapter) {
        this.selectedChapter = nextChapter;
      } else {
        // Fetch from API if not in current page
        this.loadChapterByNumber(this.novel.id, nextChapterNumber);
      }
    }
  }

  private loadChapterByNumber(novelId: number, chapterNumber: number): void {
    this.loading = true;
    this.chapterService.getChapterByNumber(novelId, chapterNumber).subscribe({
      next: (chapter) => {
        this.selectedChapter = chapter;
        this.loading = false;
      },
      error: (error) => {
        console.error("Error loading chapter:", error);
        this.loading = false;
      },
    });
  }
  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src =
      "https://res.cloudinary.com/drpudphzv/image/upload/v1/novel/cmmk5yugoqtwzu5axdea";
  }
}
