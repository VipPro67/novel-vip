export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  statusCode: number
}

export interface PageResponse<T> {
  totalPages: number
  totalElements: number
  pageNumber: number
  pageSize: number
  content: T[]
}
