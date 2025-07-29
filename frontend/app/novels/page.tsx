"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { Search, Filter, SortAsc } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { NovelCard } from "@/components/novel/novel-card"
import { useApi } from "@/hooks/use-api"
import { usePagination } from "@/hooks/use-pagination"
import type { Novel, Category } from "@/lib/api"

export default function NovelsPage() {
  const [novels, setNovels] = useState<Novel[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedCategory, setSelectedCategory] = useState<string>("all") // Updated default value
  const [sortBy, setSortBy] = useState("latest")
  const [loading, setLoading] = useState(true)

  const { api } = useApi()
  const { currentPage, totalPages, goToPage, goToNextPage, goToPreviousPage } = usePagination()

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)
        const [novelsResponse, categoriesResponse] = await Promise.all([
          api.getNovels({
            page: currentPage,
            limit: 20,
            search: searchTerm,
            category: selectedCategory,
            sortBy,
          }),
          api.getCategories(),
        ])

        setNovels(novelsResponse.data)
        setCategories(categoriesResponse.data)
      } catch (error) {
        console.error("Failed to fetch data:", error)
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [currentPage, searchTerm, selectedCategory, sortBy, api])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    goToPage(1)
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-3 sm:gap-4 md:gap-6">
          {Array.from({ length: 12 }).map((_, i) => (
            <div key={i} className="animate-pulse">
              <div className="aspect-[3/4] bg-gray-200 rounded-lg mb-2"></div>
              <div className="h-4 bg-gray-200 rounded mb-1"></div>
              <div className="h-3 bg-gray-200 rounded w-2/3"></div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-6 sm:py-8">
      {/* Header */}
      <div className="mb-6 sm:mb-8">
        <h1 className="text-2xl sm:text-3xl font-bold mb-4">All Novels</h1>

        {/* Search and Filters */}
        <div className="flex flex-col sm:flex-row gap-4 mb-4">
          <form onSubmit={handleSearch} className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <Input
                type="text"
                placeholder="Search novels..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </form>

          <div className="flex gap-2 sm:gap-4">
            <Select value={selectedCategory} onValueChange={setSelectedCategory}>
              <SelectTrigger className="w-full sm:w-[180px]">
                <Filter className="h-4 w-4 mr-2" />
                <SelectValue placeholder="Category" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Categories</SelectItem>
                {categories.map((category) => (
                  <SelectItem key={category.id} value={category.id}>
                    {category.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Select value={sortBy} onValueChange={setSortBy}>
              <SelectTrigger className="w-full sm:w-[140px]">
                <SortAsc className="h-4 w-4 mr-2" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="latest">Latest</SelectItem>
                <SelectItem value="popular">Popular</SelectItem>
                <SelectItem value="rating">Rating</SelectItem>
                <SelectItem value="title">Title</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* Active Filters */}
        {(searchTerm || selectedCategory !== "all") && (
          <div className="flex flex-wrap gap-2 mb-4">
            {searchTerm && (
              <Badge variant="secondary" className="text-xs">
                Search: {searchTerm}
                <button onClick={() => setSearchTerm("")} className="ml-2 hover:text-red-500">
                  ×
                </button>
              </Badge>
            )}
            {selectedCategory !== "all" && (
              <Badge variant="secondary" className="text-xs">
                Category: {categories.find((c) => c.id === selectedCategory)?.name}
                <button onClick={() => setSelectedCategory("all")} className="ml-2 hover:text-red-500">
                  ×
                </button>
              </Badge>
            )}
          </div>
        )}
      </div>

      {/* Novels Grid - Responsive */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 2xl:grid-cols-7 gap-3 sm:gap-4 md:gap-6 mb-8">
        {novels.map((novel) => (
          <NovelCard key={novel.id} novel={novel} />
        ))}
      </div>

      {/* Empty State */}
      {novels.length === 0 && !loading && (
        <div className="text-center py-12">
          <p className="text-gray-500 mb-4">No novels found</p>
          <Button
            onClick={() => {
              setSearchTerm("")
              setSelectedCategory("all") // Updated default value
              goToPage(1)
            }}
          >
            Clear Filters
          </Button>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center gap-2 mt-8">
          <Button variant="outline" onClick={goToPreviousPage} disabled={currentPage === 1} size="sm">
            Previous
          </Button>

          <div className="flex gap-1">
            {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
              const page = i + 1
              return (
                <Button
                  key={page}
                  variant={currentPage === page ? "default" : "outline"}
                  onClick={() => goToPage(page)}
                  size="sm"
                  className="w-8 h-8 p-0"
                >
                  {page}
                </Button>
              )
            })}
          </div>

          <Button variant="outline" onClick={goToNextPage} disabled={currentPage === totalPages} size="sm">
            Next
          </Button>
        </div>
      )}
    </div>
  )
}
