import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-category-item',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="category-item" [routerLink]="['/category', categoryId]">
      <div class="category-icon">
        <i [class]="icon"></i>
      </div>
      <div class="category-info">
        <h3 class="category-name">{{ name }}</h3>
        <span class="novel-count">{{ novelCount }} novels</span>
      </div>
    </div>
  `,
  styles: [`
    .category-item {
      display: flex;
      align-items: center;
      padding: 16px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: translateY(-2px);
      }
    }

    .category-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;

      i {
        font-size: 20px;
        color: #666;
      }
    }

    .category-info {
      flex: 1;

      .category-name {
        margin: 0;
        font-size: 16px;
        font-weight: bold;
        color: #333;
      }

      .novel-count {
        font-size: 12px;
        color: #888;
      }
    }
  `]
})
export class CategoryItemComponent {
  @Input() categoryId!: string;
  @Input() name!: string;
  @Input() icon!: string;
  @Input() novelCount!: number;
} 