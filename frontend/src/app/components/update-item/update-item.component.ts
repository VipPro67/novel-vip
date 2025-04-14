import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-update-item',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="update-item" [routerLink]="['/novel', novelId, 'chapter', chapterId]">
      <div class="novel-info">
        <h4 class="novel-title">{{ title }}</h4>
        <p class="chapter-title">Chapter {{ chapterNumber }}: {{ chapterTitle }}</p>
      </div>
      <div class="update-info">
        <span class="update-time">{{ updateTime | date:'short' }}</span>
        <span class="novel-status" [class]="status.toLowerCase()">{{ status }}</span>
      </div>
    </div>
  `,
  styles: [`
    .update-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
      cursor: pointer;
      transition: background-color 0.2s;

      &:hover {
        background-color: #f5f5f5;
      }
    }

    .novel-info {
      flex: 1;
      min-width: 0;

      .novel-title {
        margin: 0;
        font-size: 16px;
        font-weight: bold;
        color: #333;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .chapter-title {
        margin: 4px 0 0;
        font-size: 14px;
        color: #666;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .update-info {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 4px;
      margin-left: 16px;

      .update-time {
        font-size: 12px;
        color: #888;
      }

      .novel-status {
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: bold;
        color: white;

        &.ongoing {
          background-color: #4CAF50;
        }

        &.completed {
          background-color: #2196F3;
        }

        &.dropped {
          background-color: #f44336;
        }
      }
    }
  `]
})
export class UpdateItemComponent {
  @Input() novelId!: string;
  @Input() chapterId!: string;
  @Input() title!: string;
  @Input() chapterNumber!: number;
  @Input() chapterTitle!: string;
  @Input() updateTime!: Date;
  @Input() status!: string;
} 