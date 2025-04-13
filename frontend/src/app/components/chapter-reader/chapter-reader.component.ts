import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { NovelService } from '../../services/novel.service';
import { ReaderSettingsService } from '../../services/reader-settings.service';
import { ReaderSettings } from '../../models/reader-settings.model';
import { SanitizeHtmlPipe } from '../../pipes/sanitize-html.pipe';
import { ReaderSettingsComponent } from '../reader-settings/reader-settings.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chapter-reader',
  templateUrl: './chapter-reader.component.html',
  styleUrls: ['./chapter-reader.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    SanitizeHtmlPipe
  ]
})
export class ChapterReaderComponent implements OnInit, OnDestroy {
  novelId: string = '';
  chapterNumber: number = 0;
  chapter: any = null;
  loading: boolean = false;
  error: string | null = null;
  settings: ReaderSettings;
  private settingsSubscription: Subscription | undefined;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private novelService: NovelService,
    private readerSettingsService: ReaderSettingsService,
    private dialog: MatDialog
  ) {
    this.settings = this.readerSettingsService.currentSettings;
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.novelId = params['novelId'];
      this.chapterNumber = parseInt(params['chapterNumber']);
      this.loadChapter();
    });
    
    this.settingsSubscription = this.readerSettingsService.settings.subscribe(settings => {
      this.settings = settings;
    });
  }
  
  ngOnDestroy(): void {
    if (this.settingsSubscription) {
      this.settingsSubscription.unsubscribe();
    }
  }

  loadChapter(): void {
    this.loading = true;
    this.error = null;
    
    this.novelService.getChapterByNovelIdAndNumber(this.novelId, this.chapterNumber).subscribe({
      next: (response) => {
        if (response.success) {
          this.chapter = response.data;
        } else {
          this.error = response.message || 'Failed to load chapter';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading chapter. Please try again later.';
        this.loading = false;
        console.error('Error loading chapter:', err);
      }
    });
  }

  goToPreviousChapter(): void {
    if (this.chapterNumber > 1) {
      this.chapter = null; // Reset chapter to avoid showing old data
      this.router.navigate(['/novels', this.novelId, 'chapters', this.chapterNumber - 1]);
    }
  }

  goToNextChapter(): void {
    this.chapter = null; // Reset chapter to avoid showing old data
    this.router.navigate(['/novels', this.novelId, 'chapters', this.chapterNumber + 1]);
  }

  goToNovelDetail(): void {
    this.router.navigate(['/novels', this.novelId]);
  }
  
  openSettings(): void {
    this.dialog.open(ReaderSettingsComponent, {
      width: '500px',
      panelClass: 'reader-settings-dialog'
    });
  }
} 