import { Component, OnInit } from "@angular/core";
import { Novel, NovelService } from "../../services/novel.service";
import { Router } from "@angular/router";
import { PageEvent, MatPaginatorModule } from "@angular/material/paginator";
import { TruncateWordsPipe } from "../../pipes/truncate-words.pipe"; // Import here
import { CommonModule } from "@angular/common"; // Import CommonModule for NgIf, NgFor
import { NgIf, NgFor } from "@angular/common"; // Import NgIf and NgFor

@Component({
  selector: "app-novel-list",
  templateUrl: "./novel-list.component.html",
  styleUrls: ["./novel-list.component.scss"],
  imports: [TruncateWordsPipe, MatPaginatorModule, CommonModule, NgIf, NgFor], // Add to imports array
  standalone: true, // If the component is standalone
})
export class NovelListComponent implements OnInit {
  novels: Novel[] = [];
  loading = false;
  currentPage = 0;
  pageSize = 12;
  totalNovels = 0;
  totalElements = 0;
  truncateWords = new TruncateWordsPipe();

  constructor(private novelService: NovelService, private router: Router) {}

  ngOnInit(): void {
    this.loadNovels();
  }

  loadNovels(): void {
    this.loading = true;
    this.novelService.getNovels(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.novels = response.data.content;
          this.totalNovels = response.data.totalElements;
        }
        this.loading = false;
      },
      error: (error) => {
        console.error("Error loading novels:", error);
        this.loading = false;
      },
    });
  }

  onPageChange(event: any): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadNovels();
  }

  navigateToNovel(novelId: string): void {
    // Implement navigation to novel detail page
    // this.router.navigate(['/novels', novelId]);
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src =
      "https://res.cloudinary.com/drpudphzv/image/upload/v1/novel/cmmk5yugoqtwzu5axdea";
  }
}
