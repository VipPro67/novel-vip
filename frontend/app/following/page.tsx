"use client"

import { useEffect, useState } from "react"
import { BookmarkIcon, Loader2, Search } from "lucide-react"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Header } from "@/components/layout/header"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Pagination } from "@/components/ui/pagination"
import { useToast } from "@/hooks/use-toast"
import { usePagination } from "@/hooks/use-pagination"
import { api, type Novel } from "@/lib/api"
import Link from "next/link"
import { Badge } from "@/components/ui/badge"

export default function FollowingPage() {
  return (
    <AuthGuard>
      <FollowingContent />
    </AuthGuard>
  )
}

function FollowingContent() {
  const [novels, setNovels] = useState<Novel[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const { toast } = useToast()

  const { currentPage, totalPages, handlePageChange, updateTotalPages, getPaginationParams } = usePagination({
    initialPage: 0,
    initialSize: 12,
    initialSortBy: "createdAt",
    initialSortDir: "desc",
  })

  useEffect(() => {
    fetchFollowingNovels()
  }, [currentPage])

  const fetchFollowingNovels = async () => {
    setLoading(true)
    try {
      const paginationParams = getPaginationParams()
      const response = await api.getFavorites(paginationParams)

      if (response.success && response.data?.content) {
        setNovels(response.data.content)
        updateTotalPages(response.data.totalPages || 0)
      }
    } catch (error) {
      console.error("Failed to fetch following novels:", error)
      toast({
        title: "Error",
        description: "Failed to load your following novels",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const filteredNovels = novels.filter((novel) => novel.title.toLowerCase().includes(searchQuery.toLowerCase()))

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <main className="container py-8">
        <div className="max-w-6xl mx-auto space-y-8">
          <div>
            <h1 className="text-3xl font-bold">Following Novels</h1>
            <p className="text-muted-foreground">Novels you've bookmarked and are following</p>
          </div>

          <div className="flex items-center gap-4">
            <div className="relative flex-1 max-w-md">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search your following novels..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>

          {loading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : filteredNovels.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <BookmarkIcon className="h-12 w-12 text-muted-foreground mb-4" />
                <h3 className="text-lg font-semibold mb-2">No following novels</h3>
                <p className="text-muted-foreground text-center">
                  {searchQuery ? "No novels match your search" : "Start following novels to see them here"}
                </p>
              </CardContent>
            </Card>
          ) : (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {filteredNovels.map((novel) => (
                  <Link key={novel.id} href={`/novels/${novel.slug}`}>
                    <Card className="h-full hover:shadow-lg transition-shadow cursor-pointer">
                      <CardHeader className="p-0">
                        <div className="aspect-[2/3] relative overflow-hidden rounded-t-lg">
                          <img
                            src={novel.coverImage || "/placeholder.svg?height=400&width=300"}
                            alt={novel.title}
                            className="object-cover w-full h-full"
                          />
                        </div>
                      </CardHeader>
                      <CardContent className="p-4">
                        <CardTitle className="text-base line-clamp-2 mb-2">{novel.title}</CardTitle>
                        <CardDescription className="text-sm line-clamp-1 mb-2">{novel.author}</CardDescription>
                        <div className="flex items-center gap-2 flex-wrap">
                          <Badge variant="secondary" className="text-xs">
                            {novel.status}
                          </Badge>
                          {novel.totalChapters && (
                            <span className="text-xs text-muted-foreground">{novel.totalChapters} chapters</span>
                          )}
                        </div>
                      </CardContent>
                    </Card>
                  </Link>
                ))}
              </div>

              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
                showPageNumbers={true}
              />
            </>
          )}
        </div>
      </main>
    </div>
  )
}
