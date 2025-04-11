import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ReaderSettings, DEFAULT_READER_SETTINGS } from '../models/reader-settings.model';

@Injectable({
  providedIn: 'root'
})
export class ReaderSettingsService {
  private readonly STORAGE_KEY = 'reader_settings';
  private settingsSubject: BehaviorSubject<ReaderSettings>;

  constructor() {
    const savedSettings = this.loadSettings();
    this.settingsSubject = new BehaviorSubject<ReaderSettings>(savedSettings);
  }

  get settings(): Observable<ReaderSettings> {
    return this.settingsSubject.asObservable();
  }

  get currentSettings(): ReaderSettings {
    return this.settingsSubject.value;
  }

  updateSettings(settings: Partial<ReaderSettings>): void {
    const currentSettings = this.settingsSubject.value;
    const updatedSettings = { ...currentSettings, ...settings };
    this.settingsSubject.next(updatedSettings);
    this.saveSettings(updatedSettings);
  }

  resetSettings(): void {
    this.settingsSubject.next(DEFAULT_READER_SETTINGS);
    this.saveSettings(DEFAULT_READER_SETTINGS);
  }

  private loadSettings(): ReaderSettings {
    try {
      const savedSettings = localStorage.getItem(this.STORAGE_KEY);
      if (savedSettings) {
        return JSON.parse(savedSettings);
      }
    } catch (error) {
      console.error('Error loading reader settings:', error);
    }
    return DEFAULT_READER_SETTINGS;
  }

  private saveSettings(settings: ReaderSettings): void {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(settings));
    } catch (error) {
      console.error('Error saving reader settings:', error);
    }
  }
} 