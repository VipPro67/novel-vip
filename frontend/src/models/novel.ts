import type { Category, Genre, Tag } from "./category"
import type { FileMetadata } from "./file"

export interface Novel {
  id: string
  title: string
  description: string
  slug: string
  author: string
  imageUrl: string
  status: string
  categories: Category[] | []
  genres: Genre[] | []
  tags: Tag[] | []
  totalChapters: number
  totalViews: number
  monthlyViews: number
  dailyViews: number
  rating: number
  updatedAt: string
  coverImage?: FileMetadata
}
