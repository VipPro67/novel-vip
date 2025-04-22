import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule } from "@angular/router";

@Component({
  selector: "app-novel-card",
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="novel-card" [routerLink]="['/novel', novelId]">
      <div class="novel-cover">
        <img [src]="coverImage" [alt]="title" />
        <div class="novel-status" [class]="status.toLowerCase()">
          {{ status }}
        </div>
      </div>
      <div class="novel-info">
        <h3 class="novel-title">{{ title }}</h3>
        <p class="novel-author">{{ author }}</p>
        <div class="novel-stats">
          <span class="views"><i class="fas fa-eye"></i> {{ views }}</span>
          <span class="chapters"
            ><i class="fas fa-book"></i> {{ chapters }}</span
          >
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .novel-card {
        background: #fff;
        border-radius: 8px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        overflow: hidden;
        transition: transform 0.2s;
        cursor: pointer;

        &:hover {
          transform: translateY(-5px);
        }
      }

      .novel-cover {
        position: relative;
        padding-top: 140%;
        overflow: hidden;

        img {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .novel-status {
        position: absolute;
        top: 10px;
        right: 10px;
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: bold;
        color: white;

        &.ongoing {
          background-color: #4caf50;
        }

        &.completed {
          background-color: #2196f3;
        }

        &.dropped {
          background-color: #f44336;
        }
      }

      .novel-info {
        padding: 12px;

        .novel-title {
          margin: 0;
          font-size: 16px;
          font-weight: bold;
          color: #333;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .novel-author {
          margin: 4px 0;
          font-size: 14px;
          color: #666;
        }

        .novel-stats {
          display: flex;
          gap: 12px;
          font-size: 12px;
          color: #888;

          i {
            margin-right: 4px;
          }
        }
      }
    `,
  ],
})
export class NovelCardComponent {
  @Input() novelId!: string;
  @Input() title!: string;
  @Input() author!: string;
  @Input() coverImage!: string;
  @Input() status!: string;
  @Input() views!: number;
  @Input() chapters!: number;
}
