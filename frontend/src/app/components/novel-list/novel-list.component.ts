import { Component, OnInit } from "@angular/core";
import { NovelService } from "../../services/novel.service";
import { Novel, PaginatedResponse } from "../../models/novel.model";
import { Router } from "@angular/router";
import { PageEvent, MatPaginatorModule } from "@angular/material/paginator";
import { TruncateWordsPipe } from "../../pipes/truncate-words.pipe"; // Import here
import { CommonModule } from "@angular/common"; // Import CommonModule for NgIf, NgFor
import { NgIf, NgFor } from "@angular/common"; // Import NgIf and NgFor

@Component({
  selector: "app-novel-list",
  templateUrl: "./novel-list.component.html",
  styleUrls: ["./novel-list.component.css"],
  imports: [TruncateWordsPipe, MatPaginatorModule, CommonModule, NgIf, NgFor], // Add to imports array
  standalone: true, // If the component is standalone
})
export class NovelListComponent implements OnInit {
  novels: Novel[] = [];
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  truncateWords = new TruncateWordsPipe();

  constructor(private novelService: NovelService, private router: Router) {}

  ngOnInit(): void {
    this.loadNovels();
  }

  loadNovels(): void {
    this.loading = true;
    this.novelService.getAllNovels(this.currentPage, this.pageSize).subscribe({
      next: (response: PaginatedResponse<Novel>) => {
        this.novels = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (error) => {
        console.error("Error loading novels:", error);
        this.loading = false;
      },
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadNovels();
  }

  viewNovel(novelId: number): void {
    this.router.navigate(["/novel", novelId]);
  }
  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src =
      "https://res.cloudinary.com/drpudphzv/image/upload/v1/novel/cmmk5yugoqtwzu5axdea";
  }
}
