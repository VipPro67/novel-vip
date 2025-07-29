"use client"

import type React from "react"

import { useEffect, useState } from "react"
import { useSearchParams } from "next/navigation"
import { Search, Grid, List, Filter, X } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Header } from "@/components/layout/header"
import { NovelCard } from "@/components/novel/novel-card"
import { api, type Novel } from "@/lib/api"
import { Pagination } from "@/components/ui/pagination"

export default function SearchPage() {
  const [novels, setNovels] = useState<Novel[]>([])
  const [loading, setLoading] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [sortBy, setSortBy] = useState("updatedAt")
  const [sortDir, setSortDir] = useState("desc")
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid")
  const [searchQuery, setSearchQuery] = useState("")
  const [activeFilters, setActiveFilters] = useState<string[]>([])

  const searchParams = useSearchParams()
  const initialQuery = searchParams.get("q") || ""

  useEffect(() => {
    if (initialQuery) {
      setSearchQuery(initialQuery)
      performSearch(initialQuery)
    }
  }, [initialQuery, currentPage, sortBy, sortDir])

  const performSearch = async (query: string) => {
    if (!query.trim()) return

    setLoading(true)
    try {
      const response = await api.searchNovels(query, {
        page: currentPage,
        size: 20,
        sortBy,
        sortDir,
      })

      if (response.success) {
        setNovels(response.data.content)
        setTotalPages(response.data.totalPages)
      }
    } catch (error) {
      console.error("Failed to search novels:", error)
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setCurrentPage(0)
    performSearch(searchQuery)
  }

  const handleSortChange = (value: string) => {
    const [field, direction] = value.split("-")
    setSortBy(field)
    setSortDir(direction)
    setCurrentPage(0)
  }

  const addFilter = (filter: string) => {
    if (!activeFilters.includes(filter)) {
      setActiveFilters([...activeFilters, filter])
    }
  }

  const removeFilter = (filter: string) => {
    setActiveFilters(activeFilters.filter((f) => f !== filter))
  }

  const clearAllFilters = () => {
    setActiveFilters([])
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <main className="container py-8">
        <div className="flex flex-col space-y-6">
          {/* Search Header */}
          <div className="flex flex-col space-y-4">
            <div className="flex items-center space-x-3">
              <Search className="h-8 w-8 text-primary" />
              <div>
                <h1 className="text-3xl font-bold">Search Novels</h1>
                <p className="text-muted-foreground">
                  {initialQuery ? `Results for "${initialQuery}"` : "Find your next favorite story"}
                </p>
              </div>
            </div>

            {/* Search Form */}
            <form onSubmit={handleSearch} className="flex gap-2">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search by title, author, or keywords..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
              <Button type="submit" disabled={loading}>
                {loading ? "Searching..." : "Search"}
              </Button>
            </form>
          </div>

          {/* Filters and Controls */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div className="flex flex-wrap items-center gap-2">
              <Button variant="outline" size="sm" onClick={() => addFilter("completed")}>
                <Filter className="h-4 w-4 mr-1" />
                Completed
              </Button>
              <Button variant="outline" size="sm" onClick={() => addFilter("ongoing")}>
                <Filter className="h-4 w-4 mr-1" />
                Ongoing
              </Button>
              <Button variant="outline" size="sm" onClick={() => addFilter("high-rated")}>
                <Filter className="h-4 w-4 mr-1" />
                High Rated
              </Button>

              {activeFilters.length > 0 && (
                <Button variant="ghost" size="sm" onClick={clearAllFilters} className="text-muted-foreground">
                  Clear All
                </Button>
              )}
            </div>

            <div className="flex items-center space-x-2">
              <Select value={`${sortBy}-${sortDir}`} onValueChange={handleSortChange}>
                <SelectTrigger className="w-48">
                  <SelectValue placeholder="Sort by" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="updatedAt-desc">Latest Updated</SelectItem>
                  <SelectItem value="createdAt-desc">Newest</SelectItem>
                  <SelectItem value="rating-desc">Highest Rated</SelectItem>
                  <SelectItem value="views-desc">Most Popular</SelectItem>
                  <SelectItem value="title-asc">Title A-Z</SelectItem>
                  <SelectItem value="title-desc">Title Z-A</SelectItem>
                </SelectContent>
              </Select>

              <div className="flex border rounded-md">
                <Button
                  variant={viewMode === "grid" ? "default" : "ghost"}
                  size="icon"
                  onClick={() => setViewMode("grid")}
                >
                  <Grid className="h-4 w-4" />
                </Button>
                <Button
                  variant={viewMode === "list" ? "default" : "ghost"}
                  size="icon"
                  onClick={() => setViewMode("list")}
                >
                  <List className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>

          {/* Active Filters */}
          {activeFilters.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {activeFilters.map((filter) => (
                <Badge key={filter} variant="secondary" className="flex items-center gap-1">
                  {filter}
                  <X className="h-3 w-3 cursor-pointer" onClick={() => removeFilter(filter)} />
                </Badge>
              ))}
            </div>
          )}

          {/* Results */}
          {loading ? (
            <div
              className={`grid gap-6 ${
                viewMode === "grid"
                  ? "grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5"
                  : "grid-cols-1"
              }`}
            >
              {Array.from({ length: 20 }).map((_, i) => (
                <Card key={i} className="animate-pulse">
                  <div className="aspect-[3/4] bg-muted" />
                  <CardContent className="p-4">
                    <div className="h-4 bg-muted rounded mb-2" />
                    <div className="h-3 bg-muted rounded mb-2" />
                    <div className="h-3 bg-muted rounded" />
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : !initialQuery && !searchQuery ? (
            <div className="text-center py-12">
              <Search className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground">Enter a search term to find novels</p>
            </div>
          ) : novels.length === 0 ? (
            <div className="text-center py-12">
              <Search className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground">No novels found for "{initialQuery || searchQuery}"</p>
              <p className="text-sm text-muted-foreground mt-2">Try different keywords or check your spelling</p>
            </div>
          ) : (
            <>
              <div className="flex items-center justify-between">
                <p className="text-sm text-muted-foreground">Found {novels.length} novels</p>
              </div>

              <div
                className={`grid gap-6 ${
                  viewMode === "grid"
                    ? "grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5"
                    : "grid-cols-1"
                }`}
              >
                {novels.map((novel) => (
                  <NovelCard key={novel.id} novel={novel} />
                ))}
              </div>
            </>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
              showPageNumbers={true}
              className="mt-8"
            />
          )}
        </div>
      </main>
    </div>
  )
}
