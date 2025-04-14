import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NovelCardComponent } from '../novel-card/novel-card.component';
import { UpdateItemComponent } from '../update-item/update-item.component';
import { CategoryItemComponent } from '../category-item/category-item.component';
import { HomeService } from '../../services/home.service';
import { NovelListComponent } from '../novel-list/novel-list.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    NovelCardComponent,
    UpdateItemComponent,
    CategoryItemComponent,
    NovelListComponent,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="home-container">
    <!-- All Novels Section -->
      <section class="all-novels">
        <app-novel-list></app-novel-list>
      </section>
      <!-- Featured Novels Section -->
      <section class="featured-novels">
        <h2>Featured Novels</h2>
        <div class="novel-grid" *ngIf="!loadingFeatured">
          <app-novel-card
            *ngFor="let novel of featuredNovels"
            [novelId]="novel.id"
            [title]="novel.title"
            [author]="novel.author"
            [coverImage]="novel.coverImage"
            [status]="novel.status"
            [views]="novel.views"
            [chapters]="novel.chapters"
          ></app-novel-card>
        </div>
        <div class="loading-spinner" *ngIf="loadingFeatured">
          <mat-spinner diameter="40"></mat-spinner>
        </div>
      </section>

      <!-- Latest Updates Section -->
      <section class="latest-updates">
        <h2>Latest Updates</h2>
        <div class="updates-list" *ngIf="!loadingUpdates">
          <app-update-item
            *ngFor="let update of latestUpdates"
            [novelId]="update.novelId"
            [chapterId]="update.chapterId"
            [title]="update.title"
            [chapterNumber]="update.chapterNumber"
            [chapterTitle]="update.chapterTitle"
            [updateTime]="update.updateTime"
            [status]="update.status"
          ></app-update-item>
        </div>
        <div class="loading-spinner" *ngIf="loadingUpdates">
          <mat-spinner diameter="40"></mat-spinner>
        </div>
      </section>

      <!-- Popular Novels Section -->
      <section class="popular-novels">
        <h2>Popular Novels</h2>
        <div class="novel-grid" *ngIf="!loadingPopular">
          <app-novel-card
            *ngFor="let novel of popularNovels"
            [novelId]="novel.id"
            [title]="novel.title"
            [author]="novel.author"
            [coverImage]="novel.coverImage"
            [status]="novel.status"
            [views]="novel.views"
            [chapters]="novel.chapters"
          ></app-novel-card>
        </div>
        <div class="loading-spinner" *ngIf="loadingPopular">
          <mat-spinner diameter="40"></mat-spinner>
        </div>
      </section>

      <!-- Categories Section -->
      <section class="categories">
        <h2>Categories</h2>
        <div class="category-grid" *ngIf="!loadingCategories">
          <app-category-item
            *ngFor="let category of categories"
            [categoryId]="category.id"
            [name]="category.name"
            [icon]="category.icon"
            [novelCount]="category.novelCount"
          ></app-category-item>
        </div>
        <div class="loading-spinner" *ngIf="loadingCategories">
          <mat-spinner diameter="40"></mat-spinner>
        </div>
      </section>

      
    </div>
  `,
  styles: [`
    .home-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 20px;
    }

    section {
      margin-bottom: 40px;
    }

    h2 {
      font-size: 24px;
      margin-bottom: 20px;
      color: #333;
    }

    .novel-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 20px;
    }

    .updates-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .category-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 15px;
    }

    .loading-spinner {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 200px;
    }
  `]
})
export class HomeComponent implements OnInit {
  featuredNovels: any[] = [];
  latestUpdates: any[] = [];
  popularNovels: any[] = [];
  categories: any[] = [];

  loadingFeatured = false;
  loadingUpdates = false;
  loadingPopular = false;
  loadingCategories = false;

  constructor(private homeService: HomeService) {}

  ngOnInit(): void {
    this.loadFeaturedNovels();
    this.loadLatestUpdates();
    this.loadPopularNovels();
    this.loadCategories();
  }

  private loadFeaturedNovels(): void {
    this.loadingFeatured = true;
    this.homeService.getFeaturedNovels().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.featuredNovels = response.data.content;
        }
        this.loadingFeatured = false;
      },
      error: (error) => {
        console.error('Error loading featured novels:', error);
        this.loadingFeatured = false;
      }
    });
  }

  private loadLatestUpdates(): void {
    this.loadingUpdates = true;
    this.homeService.getLatestUpdates().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.latestUpdates = response.data.content;
        }
        this.loadingUpdates = false;
      },
      error: (error) => {
        console.error('Error loading latest updates:', error);
        this.loadingUpdates = false;
      }
    });
  }

  private loadPopularNovels(): void {
    this.loadingPopular = true;
    this.homeService.getPopularNovels().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.popularNovels = response.data.content;
        }
        this.loadingPopular = false;
      },
      error: (error) => {
        console.error('Error loading popular novels:', error);
        this.loadingPopular = false;
      }
    });
  }

  private loadCategories(): void {
    this.loadingCategories = true;
    this.homeService.getCategories().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.categories = response.data;
        }
        this.loadingCategories = false;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.loadingCategories = false;
      }
    });
  }
} 