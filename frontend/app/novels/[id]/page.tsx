"use client"

import { useEffect, useState } from "react"
import { useParams, useRouter } from "next/navigation"
import Image from "next/image"
import Link from "next/link"
import { Heart, Star, Eye, BookOpen, MessageCircle, Bookmark, Play } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Textarea } from "@/components/ui/textarea"
import { Header } from "@/components/layout/header"
import { useAuth } from "@/components/providers/auth-provider"
import { useToast } from "@/hooks/use-toast"
import { api, type Novel, type Chapter, type Comment } from "@/lib/api"

export default function NovelDetailPage() {
  const params = useParams()
  const router = useRouter()
  const { isAuthenticated } = useAuth()
  const { toast } = useToast()
  const [novel, setNovel] = useState<Novel | null>(null)
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [comments, setComments] = useState<Comment[]>([])
  const [isFavorite, setIsFavorite] = useState(false)
  const [userRating, setUserRating] = useState(0)
  const [newComment, setNewComment] = useState("")
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (params.id) {
      fetchNovelData()
    }
  }, [params.id])

  const fetchNovelData = async () => {
    try {
      const [novelResponse, chaptersResponse, commentsResponse] = await Promise.all([
        api.getNovelById(params.id as string),
        api.getChaptersByNovel(params.id as string, { size: 50 }),
        api.getNovelComments(params.id as string, { size: 10 }),
      ])

      if (novelResponse.success) {
        setNovel(novelResponse.data)
      }
      if (chaptersResponse.success) {
        setChapters(chaptersResponse.data.content)
      }
      if (commentsResponse.success) {
        setComments(commentsResponse.data.content)
      }

      if (isAuthenticated) {
        // Check favorite status
        try {
          const favoriteResponse = await api.checkFavoriteStatus(params.id as string)
          if (favoriteResponse.success) {
            setIsFavorite(favoriteResponse.data)
          }
        } catch (error) {
          console.error("Failed to check favorite status:", error)
        }

        // Get user rating
        try {
          const ratingResponse = await api.getUserRating(params.id as string)
          if (ratingResponse.success) {
            setUserRating(ratingResponse.data.score)
          }
        } catch (error) {
          console.error("Failed to get user rating:", error)
        }
      }
    } catch (error) {
      console.error("Failed to fetch novel data:", error)
    } finally {
      setLoading(false)
    }
  }

  const handleFavoriteToggle = async () => {
    if (!isAuthenticated) {
      router.push("/login")
      return
    }

    try {
      if (isFavorite) {
        await api.removeFromFavorites(params.id as string)
        setIsFavorite(false)
        toast({ title: "Removed from favorites" })
      } else {
        await api.addToFavorites(params.id as string)
        setIsFavorite(true)
        toast({ title: "Added to favorites" })
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to update favorites",
        variant: "destructive",
      })
    }
  }

  const handleRating = async (rating: number) => {
    if (!isAuthenticated) {
      router.push("/login")
      return
    }

    try {
      await api.rateNovel(params.id as string, rating)
      setUserRating(rating)
      toast({ title: "Rating submitted" })
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to submit rating",
        variant: "destructive",
      })
    }
  }

  const handleCommentSubmit = async () => {
    if (!isAuthenticated) {
      router.push("/login")
      return
    }

    if (!newComment.trim()) return

    try {
      const response = await api.addComment({
        content: newComment,
        novelId: params.id as string,
      })

      if (response.success) {
        setComments([response.data, ...comments])
        setNewComment("")
        toast({ title: "Comment added" })
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to add comment",
        variant: "destructive",
      })
    }
  }

  const startReading = () => {
    if (chapters.length > 0) {
      router.push(`/novels/${params.id}/chapters/${chapters[0].chapterNumber}`)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <div className="container py-8">
          <div className="animate-pulse">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              <div className="lg:col-span-1">
                <div className="aspect-[3/4] bg-muted rounded-lg" />
              </div>
              <div className="lg:col-span-2 space-y-4">
                <div className="h-8 bg-muted rounded" />
                <div className="h-4 bg-muted rounded" />
                <div className="h-20 bg-muted rounded" />
              </div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  if (!novel) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <div className="container py-8">
          <div className="text-center">
            <h1 className="text-2xl font-bold">Novel not found</h1>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <main className="container py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Novel Cover and Actions */}
          <div className="lg:col-span-1">
            <Card>
              <CardContent className="p-6">
                <div className="aspect-[3/4] relative mb-4">
                  <Image
                    src={novel.coverImage || "/placeholder.svg?height=600&width=450"}
                    alt={novel.title}
                    fill
                    className="object-cover rounded-lg"
                  />
                </div>

                <div className="space-y-4">
                  <Button onClick={startReading} className="w-full" size="lg">
                    <Play className="mr-2 h-4 w-4" />
                    Start Reading
                  </Button>

                  <div className="grid grid-cols-2 gap-2">
                    <Button
                      variant={isFavorite ? "default" : "outline"}
                      onClick={handleFavoriteToggle}
                      className="w-full"
                    >
                      <Heart className={`mr-2 h-4 w-4 ${isFavorite ? "fill-current" : ""}`} />
                      {isFavorite ? "Favorited" : "Favorite"}
                    </Button>

                    <Button variant="outline" className="w-full bg-transparent">
                      <Bookmark className="mr-2 h-4 w-4" />
                      Bookmark
                    </Button>
                  </div>

                  {/* Rating */}
                  <div className="text-center">
                    <p className="text-sm text-muted-foreground mb-2">Rate this novel</p>
                    <div className="flex justify-center space-x-1">
                      {[1, 2, 3, 4, 5].map((rating) => (
                        <button key={rating} onClick={() => handleRating(rating)} className="p-1">
                          <Star
                            className={`h-5 w-5 ${
                              rating <= userRating ? "fill-yellow-400 text-yellow-400" : "text-gray-300"
                            }`}
                          />
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Novel Info */}
          <div className="lg:col-span-2">
            <div className="space-y-6">
              {/* Title and Meta */}
              <div>
                <h1 className="text-3xl font-bold mb-2">{novel.title}</h1>
                <p className="text-lg text-muted-foreground mb-4">by {novel.author}</p>

                <div className="flex flex-wrap items-center gap-4 text-sm text-muted-foreground mb-4">
                  <div className="flex items-center space-x-1">
                    <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                    <span>{novel.rating}/5</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <Eye className="h-4 w-4" />
                    <span>{novel.views.toLocaleString()} views</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <BookOpen className="h-4 w-4" />
                    <span>{novel.totalChapters} chapters</span>
                  </div>
                  <Badge variant="secondary">{novel.status}</Badge>
                </div>

                <div className="flex flex-wrap gap-2 mb-4">
                  {novel.categories.map((category) => (
                    <Badge key={category.id} variant="outline">
                      {category.name}
                    </Badge>
                  ))}
                </div>
              </div>

              {/* Description */}
              <Card>
                <CardHeader>
                  <CardTitle>Description</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground leading-relaxed">{novel.description}</p>
                </CardContent>
              </Card>

              {/* Tabs */}
              <Tabs defaultValue="chapters" className="w-full">
                <TabsList className="grid w-full grid-cols-2">
                  <TabsTrigger value="chapters">Chapters</TabsTrigger>
                  <TabsTrigger value="comments">Comments</TabsTrigger>
                </TabsList>

                <TabsContent value="chapters" className="space-y-4">
                  <Card>
                    <CardHeader>
                      <CardTitle>Chapters ({chapters.length})</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-2 max-h-96 overflow-y-auto">
                        {chapters.map((chapter) => (
                          <Link
                            key={chapter.id}
                            href={`/novels/${novel.id}/chapters/${chapter.chapterNumber}`}
                            className="block p-3 rounded-lg border hover:bg-muted transition-colors"
                          >
                            <div className="flex justify-between items-center">
                              <div>
                                <p className="font-medium">
                                  Chapter {chapter.chapterNumber}: {chapter.title}
                                </p>
                                <p className="text-sm text-muted-foreground">
                                  {new Date(chapter.updatedAt).toLocaleDateString()}
                                </p>
                              </div>
                            </div>
                          </Link>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                </TabsContent>

                <TabsContent value="comments" className="space-y-4">
                  {isAuthenticated && (
                    <Card>
                      <CardHeader>
                        <CardTitle>Add Comment</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <div className="space-y-4">
                          <Textarea
                            placeholder="Share your thoughts about this novel..."
                            value={newComment}
                            onChange={(e) => setNewComment(e.target.value)}
                          />
                          <Button onClick={handleCommentSubmit}>
                            <MessageCircle className="mr-2 h-4 w-4" />
                            Post Comment
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  )}

                  <Card>
                    <CardHeader>
                      <CardTitle>Comments ({comments.length})</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-4">
                        {comments.map((comment) => (
                          <div key={comment.id} className="border-b pb-4 last:border-b-0">
                            <div className="flex justify-between items-start mb-2">
                              <p className="font-medium">{comment.username}</p>
                              <p className="text-sm text-muted-foreground">
                                {new Date(comment.createdAt).toLocaleDateString()}
                              </p>
                            </div>
                            <p className="text-muted-foreground">{comment.content}</p>
                          </div>
                        ))}
                        {comments.length === 0 && (
                          <p className="text-center text-muted-foreground py-8">
                            No comments yet. Be the first to share your thoughts!
                          </p>
                        )}
                      </div>
                    </CardContent>
                  </Card>
                </TabsContent>
              </Tabs>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
