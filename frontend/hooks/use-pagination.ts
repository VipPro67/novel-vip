"use client"

import { useState, useCallback } from "react"

interface UsePaginationProps {
  initialPage?: number
  initialSize?: number
  initialSortBy?: string
  initialSortDir?: "asc" | "desc"
}

export function usePagination({
  initialPage = 0,
  initialSize = 10,
  initialSortBy = "id",
  initialSortDir = "asc",
}: UsePaginationProps = {}) {
  const [currentPage, setCurrentPage] = useState(initialPage)
  const [pageSize, setPageSize] = useState(initialSize)
  const [sortBy, setSortBy] = useState(initialSortBy)
  const [sortDir, setSortDir] = useState<"asc" | "desc">(initialSortDir)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const resetPage = useCallback(() => {
    setCurrentPage(0)
  }, [])

  const handlePageChange = useCallback((page: number) => {
    setCurrentPage(page)
  }, [])

  const handleSortChange = useCallback((field: string, direction?: "asc" | "desc") => {
    setSortBy(field)
    if (direction) {
      setSortDir(direction)
    } else {
      // Toggle direction if not specified
      setSortDir((prev) => (prev === "asc" ? "desc" : "asc"))
    }
    setCurrentPage(0) // Reset to first page when sorting changes
  }, [])

  const handlePageSizeChange = useCallback((size: number) => {
    setPageSize(size)
    setCurrentPage(0) // Reset to first page when page size changes
  }, [])

  const updateTotalPages = useCallback((total: number) => {
    setTotalPages(total)
  }, [])

  const updateTotalElements = useCallback((total: number) => {
    setTotalElements(total)
  }, [])

  const getPaginationParams = useCallback(
    () => ({
      page: currentPage,
      size: pageSize,
      sortBy,
      sortDir,
    }),
    [currentPage, pageSize, sortBy, sortDir],
  )

  return {
    // State
    currentPage,
    pageSize,
    sortBy,
    sortDir,
    totalPages,
    totalElements,

    // Actions
    handlePageChange,
    handleSortChange,
    handlePageSizeChange,
    resetPage,
    updateTotalPages,
    updateTotalElements,

    // Helpers
    getPaginationParams,
  }
}
