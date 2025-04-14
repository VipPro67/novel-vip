import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { NovelService } from "../../services/novel.service";
import { ChapterService } from "../../services/chapter.service";
import { Novel, Chapter } from "../../models/novel.model";
import { ApiResponse } from "../../models/api-response.model";
import { PaginatedResponse } from "../../models/paginated-response.model";
import { MatPaginatorModule, PageEvent } from "@angular/material/paginator";
import { CommonModule } from "@angular/common";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatListModule } from "@angular/material/list";
import { MatBadgeModule } from "@angular/material/badge";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatIconModule } from "@angular/material/icon";

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
    MatProgressSpinnerModule,
    MatIconModule
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
  showFullDescription = false;
  selectedChapter: Chapter | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private novelService: NovelService,
    private chapterService: ChapterService
  ) {}

  ngOnInit(): void {
    const novelId = this.route.snapshot.paramMap.get("id");
    if (novelId) {
      this.loadNovel(novelId);
      this.loadChapters(novelId);
    }
  }

  loadNovel(novelId: string): void {
    this.loading = true;
    this.novelService.getNovelById(novelId).subscribe({
      next: (response: ApiResponse<Novel>) => {
        if (response.success && response.data) {
          this.novel = response.data;
        }
        this.loading = false;
      },
      error: (error) => {
        console.error("Error loading novel:", error);
        this.loading = false;
      },
    });
  }

  loadChapters(novelId: string): void {
    this.loading = true;
    this.chapterService
      .getChaptersByNovel(novelId, this.currentPage, this.pageSize)
      .subscribe({
        next: (response: ApiResponse<PaginatedResponse<Chapter>>) => {
          if (response.success && response.data) {
            this.chapters = response.data.content;
            this.totalPages = response.data.totalPages;
            this.totalElements = response.data.totalElements;
          }
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
      const prevChapter = this.chapters.find(
        (ch) => ch.chapterNumber === prevChapterNumber
      );
      if (prevChapter) {
        this.selectedChapter = prevChapter;
      } else {
        // Fetch from API if not in current page
        this.loadChapterByNovelIdAndNumber(this.novel.id, prevChapterNumber);
      }
    }
  }

  goToNextChapter(): void {
    if (
      this.selectedChapter &&
      this.novel &&
      this.selectedChapter.chapterNumber < this.totalElements
    ) {
      const nextChapterNumber = this.selectedChapter.chapterNumber + 1;
      const nextChapter = this.chapters.find(
        (ch) => ch.chapterNumber === nextChapterNumber
      );
      if (nextChapter) {
        this.selectedChapter = nextChapter;
      } else {
        // Fetch from API if not in current page
        this.loadChapterByNovelIdAndNumber(this.novel.id, nextChapterNumber);
      }
    }
  }

  private loadChapterByNovelIdAndNumber(
    novelId: string,
    chapterNumber: number
  ): void {
    this.loading = true;
    this.chapterService
      .getChapterByNovelIdAndNumber(novelId, chapterNumber)
      .subscribe({
        next: (response: ApiResponse<Chapter>) => {
          if (response.success && response.data) {
            this.selectedChapter = response.data;
          }
          this.loading = false;
        },
        error: (error) => {
          console.error("Error loading chapter:", error);
          this.loading = false;
        },
      });
  }

  readChapter(chapter: Chapter): void {
    if (this.novel) {
      this.router.navigate(['/novels', this.novel.id, 'chapters', chapter.chapterNumber]);
    }
  }

  toggleDescription(): void {
    this.showFullDescription = !this.showFullDescription;
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src =
      "https://res.cloudinary.com/drpudphzv/image/upload/v1/novel/cmmk5yugoqtwzu5axdea";
  }
}
