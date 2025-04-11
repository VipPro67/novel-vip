import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSliderModule } from '@angular/material/slider';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatCardModule } from '@angular/material/card';
import { ReaderSettingsService } from '../../services/reader-settings.service';
import { ReaderSettings, FONT_FAMILIES, BACKGROUND_COLORS, TEXT_COLORS } from '../../models/reader-settings.model';

@Component({
  selector: 'app-reader-settings',
  templateUrl: './reader-settings.component.html',
  styleUrls: ['./reader-settings.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSliderModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatCardModule
  ]
})
export class ReaderSettingsComponent implements OnInit {
  settings: ReaderSettings;
  fontFamilies = FONT_FAMILIES;
  backgroundColors = BACKGROUND_COLORS;
  textColors = TEXT_COLORS;
  
  fontSizeOptions = [
    { value: 14, label: 'Small' },
    { value: 16, label: 'Medium' },
    { value: 18, label: 'Large' },
    { value: 20, label: 'X-Large' },
    { value: 22, label: 'XX-Large' }
  ];
  
  lineHeightOptions = [
    { value: 1.4, label: 'Compact' },
    { value: 1.6, label: 'Normal' },
    { value: 1.8, label: 'Comfortable' },
    { value: 2.0, label: 'Spacious' }
  ];
  
  letterSpacingOptions = [
    { value: -0.5, label: 'Tight' },
    { value: 0, label: 'Normal' },
    { value: 0.5, label: 'Wide' },
    { value: 1, label: 'Very Wide' }
  ];
  
  maxWidthOptions = [
    { value: 600, label: 'Narrow' },
    { value: 700, label: 'Medium' },
    { value: 800, label: 'Wide' },
    { value: 900, label: 'Very Wide' }
  ];

  constructor(private readerSettingsService: ReaderSettingsService) {
    this.settings = this.readerSettingsService.currentSettings;
  }

  ngOnInit(): void {
    this.readerSettingsService.settings.subscribe(settings => {
      this.settings = settings;
    });
  }

  updateSetting(key: keyof ReaderSettings, value: any): void {
    this.readerSettingsService.updateSettings({ [key]: value });
  }

  resetSettings(): void {
    this.readerSettingsService.resetSettings();
  }
} 