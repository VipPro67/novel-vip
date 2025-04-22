export interface Category {
  id: string;
  name: string;
  slug: string;
  description: string;
}

export interface PageResponseCategoryDTO {
  totalPages: number;
  totalElements: number;
  pageNumber: number;
  pageSize: number;
  content: Category[];
} 