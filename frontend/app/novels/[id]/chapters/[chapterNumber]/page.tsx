"use client"

import { useState, useEffect } from "react"
import { Client } from "@stomp/stompjs"
import { useParams, useRouter } from "next/navigation"
import { ChevronLeft, ChevronRight, Bookmark, Settings, MessageCircle, Loader2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Textarea } from "@/components/ui/textarea"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { useToast } from "@/hooks/use-toast"
import { api, type ChapterDetail, type Comment } from "@/lib/api"

interface ChapterContent {
  title: string
  content: string
  wordCount: number
  readingTime: number
}

export default function ChapterPage() {
  const params = useParams()
  const router = useRouter()
  const { toast } = useToast()

  const novelId = params.id as string
  const chapterNumber = Number.parseInt(params.chapterNumber as string)

  const [chapter, setChapter] = useState<ChapterDetail | null>(null)
  const [chapterContent, setChapterContent] = useState<ChapterContent | null>(null)
  const [loading, setLoading] = useState(true)
  const [contentLoading, setContentLoading] = useState(false)

  // Comment states
  const [showComments, setShowComments] = useState(false)
  const [comments, setComments] = useState<Comment[]>([])
  const [commentsLoading, setCommentsLoading] = useState(false)
  const [commentsLoaded, setCommentsLoaded] = useState(false)
  const [totalComments, setTotalComments] = useState(0)
  const [newComment, setNewComment] = useState("")
  const [submittingComment, setSubmittingComment] = useState(false)
  const [replyingTo, setReplyingTo] = useState<string | null>(null)
  const [replyContent, setReplyContent] = useState("")
  const [editingComment, setEditingComment] = useState<string | null>(null)
  const [editContent, setEditContent] = useState("")

  // Reset comment state when chapter changes
  const resetCommentState = () => {
    setShowComments(false)
    setComments([])
    setCommentsLoaded(false)
    setTotalComments(0)
    setNewComment("")
    setReplyingTo(null)
    setReplyContent("")
    setEditingComment(null)
    setEditContent("")
  }

  useEffect(() => {
    resetCommentState()
    fetchChapter()
  }, [novelId, chapterNumber])

const fetchChapter = async () => {
    setLoading(true)
    setContentLoading(true)
    try {
      const response = await api.getChapterByNumber(novelId, chapterNumber)
      if (response.success) {
        setChapter(response.data)
        await fetchChapterContent(response.data)
      } else {
        toast({
          title: "Error",
          description: "Failed to load chapter",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Failed to fetch chapter:", error)
      toast({
        title: "Error",
        description: "Failed to load chapter",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const fetchChapterContent = async (chapterData: ChapterDetail) => {
    try {
      // First get the JSON metadata
      const metadataResponse = await api.getChapterJsonMetadata(chapterData.id)

      if (metadataResponse.success && metadataResponse.data.fileUrl) {
        // Then fetch the actual content from the file URL
        const contentResponse = await fetch(metadataResponse.data.fileUrl)
        if (contentResponse.ok) {
          const content = await contentResponse.json()
          setChapterContent(content)
        } else {
          toast({
            title: "Error",
            description: "Failed to load chapter content",
            variant: "destructive",
          })
        }
      } else {
        // Fallback to jsonUrl if available
        if (chapterData.jsonUrl) {
          const response = await fetch(chapterData.jsonUrl)
          if (response.ok) {
            const content = await response.json()
            setChapterContent(content)
          }
        }
      }
    } catch (error) {
      console.error("Failed to fetch chapter content:", error)
      toast({
        title: "Error",
        description: "Failed to load chapter content",
        variant: "destructive",
      })
    } finally {
      setContentLoading(false)
    }
  }

  useEffect(() => {
    if (!chapter) return
    const client = new Client({
      brokerURL: process.env.NEXT_PUBLIC_WS_URL || "ws://localhost:8081/ws",
    })

    client.onConnect = () => {
      client.subscribe(`/topic/chapter.${chapter.id}`, (message) => {
        const incoming: Comment = JSON.parse(message.body)
        setComments((prev) => [incoming, ...prev])
        setTotalComments((prev) => prev + 1)
      })
    }

    client.activate()
    return () => {
      client.deactivate()
    }
  }, [chapter])

  const loadComments = async () => {
    if (!chapter || commentsLoaded) return

    setCommentsLoading(true)
    try {
      const response = await api.getChapterComments(chapter.id, {
        page: 0,
        size: 50,
        sortBy: "createdAt",
        sortDir: "desc",
      })

      if (response.success) {
        setComments(response.data.content || [])
        setTotalComments(response.data.totalElements || 0)
        setCommentsLoaded(true)
      }
    } catch (error) {
      console.error("Failed to load comments:", error)
      toast({
        title: "Error",
        description: "Failed to load comments",
        variant: "destructive",
      })
    } finally {
      setCommentsLoading(false)
    }
  }

  const handleShowComments = () => {
    if (!showComments && !commentsLoaded) {
      loadComments()
    }
    setShowComments(!showComments)
  }

  const handleAddComment = async () => {
    if (!newComment.trim() || !chapter) return

    setSubmittingComment(true)
    try {
      const response = await api.addComment({
        content: newComment.trim(),
        chapterId: chapter.id,
      })

      if (response.success) {
        setComments((prev) => [response.data, ...prev])
        setTotalComments((prev) => prev + 1)
        setNewComment("")
        toast({
          title: "Success",
          description: "Comment added successfully",
        })
      }
    } catch (error) {
      console.error("Failed to add comment:", error)
      toast({
        title: "Error",
        description: "Failed to add comment",
        variant: "destructive",
      })
    } finally {
      setSubmittingComment(false)
    }
  }

  const handleReply = async (parentId: string) => {
    if (!replyContent.trim() || !chapter) return

    setSubmittingComment(true)
    try {
      const response = await api.addComment({
        content: replyContent.trim(),
        chapterId: chapter.id,
        parentId,
      })

      if (response.success) {
        // Add reply to the parent comment
        setComments((prev) =>
          prev.map((comment) => {
            if (comment.id === parentId) {
              return {
                ...comment,
                replies: [...(comment.replies || []), response.data],
              }
            }
            return comment
          }),
        )
        setTotalComments((prev) => prev + 1)
        setReplyContent("")
        setReplyingTo(null)
        toast({
          title: "Success",
          description: "Reply added successfully",
        })
      }
    } catch (error) {
      console.error("Failed to add reply:", error)
      toast({
        title: "Error",
        description: "Failed to add reply",
        variant: "destructive",
      })
    } finally {
      setSubmittingComment(false)
    }
  }

  const handleEditComment = async (commentId: string) => {
    if (!editContent.trim()) return

    try {
      const response = await api.updateComment(commentId, {
        content: editContent.trim(),
      })

      if (response.success) {
        // Update comment in the list
        const updateCommentInList = (comments: Comment[]): Comment[] => {
          return comments.map((comment) => {
            if (comment.id === commentId) {
              return { ...comment, content: editContent.trim(), edited: true }
            }
            if (comment.replies) {
              return { ...comment, replies: updateCommentInList(comment.replies) }
            }
            return comment
          })
        }

        setComments((prev) => updateCommentInList(prev))
        setEditingComment(null)
        setEditContent("")
        toast({
          title: "Success",
          description: "Comment updated successfully",
        })
      }
    } catch (error) {
      console.error("Failed to update comment:", error)
      toast({
        title: "Error",
        description: "Failed to update comment",
        variant: "destructive",
      })
    }
  }

  const handleDeleteComment = async (commentId: string) => {
    try {
      const response = await api.deleteComment(commentId)

      if (response.success) {
        // Remove comment from the list
        const removeCommentFromList = (comments: Comment[]): Comment[] => {
          return comments.filter((comment) => {
            if (comment.id === commentId) {
              return false
            }
            if (comment.replies) {
              comment.replies = removeCommentFromList(comment.replies)
            }
            return true
          })
        }

        setComments((prev) => removeCommentFromList(prev))
        setTotalComments((prev) => prev - 1)
        toast({
          title: "Success",
          description: "Comment deleted successfully",
        })
      }
    } catch (error) {
      console.error("Failed to delete comment:", error)
      toast({
        title: "Error",
        description: "Failed to delete comment",
        variant: "destructive",
      })
    }
  }

  const renderComment = (comment: Comment, depth = 0) => {
    const isEditing = editingComment === comment.id
    const isReplying = replyingTo === comment.id

    return (
      <div key={comment.id} className={`${depth > 0 ? "ml-8 border-l-2 border-muted pl-4" : ""}`}>
        <div className="flex space-x-3 mb-4">
          <Avatar className="h-8 w-8">
            <AvatarImage src={`/placeholder-user.jpg`} />
            <AvatarFallback>{comment.username.charAt(0).toUpperCase()}</AvatarFallback>
          </Avatar>
          <div className="flex-1 space-y-2">
            <div className="flex items-center space-x-2">
              <span className="font-medium text-sm">{comment.username}</span>
              <span className="text-xs text-muted-foreground">{new Date(comment.createdAt).toLocaleDateString()}</span>
              {comment.edited && (
                <Badge variant="secondary" className="text-xs">
                  Edited
                </Badge>
              )}
            </div>

            {isEditing ? (
              <div className="space-y-2">
                <Textarea
                  value={editContent}
                  onChange={(e) => setEditContent(e.target.value)}
                  placeholder="Edit your comment..."
                  className="min-h-[80px]"
                />
                <div className="flex space-x-2">
                  <Button size="sm" onClick={() => handleEditComment(comment.id)}>
                    Save
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => {
                      setEditingComment(null)
                      setEditContent("")
                    }}
                  >
                    Cancel
                  </Button>
                </div>
              </div>
            ) : (
              <>
                <p className="text-sm">{comment.content}</p>
                <div className="flex space-x-2">
                  <Button size="sm" variant="ghost" onClick={() => setReplyingTo(isReplying ? null : comment.id)}>
                    Reply
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => {
                      setEditingComment(comment.id)
                      setEditContent(comment.content)
                    }}
                  >
                    Edit
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => handleDeleteComment(comment.id)}
                    className="text-destructive hover:text-destructive"
                  >
                    Delete
                  </Button>
                </div>
              </>
            )}

            {isReplying && (
              <div className="space-y-2 mt-2">
                <Textarea
                  value={replyContent}
                  onChange={(e) => setReplyContent(e.target.value)}
                  placeholder="Write a reply..."
                  className="min-h-[80px]"
                />
                <div className="flex space-x-2">
                  <Button size="sm" onClick={() => handleReply(comment.id)} disabled={submittingComment}>
                    {submittingComment ? (
                      <>
                        <Loader2 className="h-4 w-4 animate-spin mr-2" />
                        Posting...
                      </>
                    ) : (
                      "Post Reply"
                    )}
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => {
                      setReplyingTo(null)
                      setReplyContent("")
                    }}
                  >
                    Cancel
                  </Button>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Render replies */}
        {comment.replies && comment.replies.length > 0 && (
          <div className="space-y-4">{comment.replies.map((reply) => renderComment(reply, depth + 1))}</div>
        )}
      </div>
    )
  }

  const navigateChapter = (direction: "prev" | "next") => {
    const newChapterNumber = direction === "prev" ? chapterNumber - 1 : chapterNumber + 1
    if (newChapterNumber > 0) {
      router.push(`/novels/${novelId}/chapters/${newChapterNumber}`)
    }
  }

  // Function to sanitize and render HTML content safely
  const renderHtmlContent = (htmlContent: string) => {
    // Basic HTML sanitization - remove potentially dangerous tags and attributes
    const sanitizedContent = htmlContent
      .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, "") // Remove script tags
      .replace(/<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi, "") // Remove iframe tags
      .replace(/on\w+="[^"]*"/gi, "") // Remove event handlers
      .replace(/javascript:/gi, "") // Remove javascript: URLs

    return { __html: sanitizedContent }
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <div className="animate-pulse space-y-4">
            <div className="h-8 bg-muted rounded w-3/4"></div>
            <div className="h-4 bg-muted rounded w-1/2"></div>
            <div className="space-y-2">
              {Array.from({ length: 10 }).map((_, i) => (
                <div key={i} className="h-4 bg-muted rounded"></div>
              ))}
            </div>
          </div>
        </div>
      </div>
    )
  }

  if (!chapter) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto text-center">
          <h1 className="text-2xl font-bold mb-4">Chapter not found</h1>
          <Button onClick={() => router.back()}>Go Back</Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Sticky Header */}
      <div className="sticky top-0 z-10 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 border-b">
        <div className="container mx-auto px-4 py-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <Button variant="ghost" size="sm" onClick={() => router.back()}>
                <ChevronLeft className="h-4 w-4 mr-1" />
                Back
              </Button>
              <div>
                <h1 className="font-semibold text-lg truncate max-w-md">{chapter.title}</h1>
                <p className="text-sm text-muted-foreground">Chapter {chapter.chapterNumber}</p>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <Button variant="ghost" size="sm">
                <Bookmark className="h-4 w-4" />
              </Button>
              <Button variant="ghost" size="sm">
                <Settings className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Chapter Content */}
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <Card>
            <CardContent className="p-8">
              {contentLoading ? (
                <div className="animate-pulse space-y-4">
                  {Array.from({ length: 15 }).map((_, i) => (
                    <div key={i} className="h-4 bg-muted rounded"></div>
                  ))}
                </div>
              ) : chapterContent ? (
                <div className="prose prose-lg dark:prose-invert max-w-none">
                  <h1 className="text-3xl font-bold mb-6">{chapterContent.title}</h1>
                  {/* Render HTML content safely */}
                  <div
                    className="chapter-content leading-relaxed text-base"
                    dangerouslySetInnerHTML={renderHtmlContent(chapterContent.content)}
                    style={{
                      lineHeight: "1.8",
                      fontSize: "16px",
                      color: "inherit",
                    }}
                  />
                  {chapterContent.wordCount && (
                    <div className="mt-8 pt-4 border-t text-sm text-muted-foreground">
                      <p>Word count: {chapterContent.wordCount.toLocaleString()}</p>
                      {chapterContent.readingTime && (
                        <p>Estimated reading time: {chapterContent.readingTime} minutes</p>
                      )}
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-8">
                  <p className="text-muted-foreground">Chapter content not available</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Chapter Navigation */}
          <div className="flex justify-between items-center mt-8">
            <Button variant="outline" onClick={() => navigateChapter("prev")} disabled={chapterNumber <= 1}>
              <ChevronLeft className="h-4 w-4 mr-2" />
              Previous Chapter
            </Button>
            <Button variant="outline" onClick={() => navigateChapter("next")}>
              Next Chapter
              <ChevronRight className="h-4 w-4 ml-2" />
            </Button>
          </div>

          {/* Comments Section */}
          <div className="mt-8 text-center">
            <Button variant="outline" size="lg" onClick={handleShowComments} className="mb-6 bg-transparent">
              <MessageCircle className="h-4 w-4 mr-2" />
              {showComments ? "Hide Comments" : "Show Comments"}
              {totalComments > 0 && (
                <Badge variant="secondary" className="ml-2">
                  {totalComments}
                </Badge>
              )}
            </Button>

            {showComments && (
              <Card>
                <CardContent className="p-6">
                  <div className="text-left space-y-6">
                    {/* Add Comment Form */}
                    <div className="space-y-4">
                      <h3 className="text-lg font-semibold">Add a Comment</h3>
                      <Textarea
                        value={newComment}
                        onChange={(e) => setNewComment(e.target.value)}
                        placeholder="Share your thoughts about this chapter..."
                        className="min-h-[100px]"
                      />
                      <Button onClick={handleAddComment} disabled={submittingComment || !newComment.trim()}>
                        {submittingComment ? (
                          <>
                            <Loader2 className="h-4 w-4 animate-spin mr-2" />
                            Posting...
                          </>
                        ) : (
                          "Post Comment"
                        )}
                      </Button>
                    </div>

                    {/* Comments List */}
                    <div className="space-y-6">
                      {commentsLoading ? (
                        <div className="space-y-4">
                          {Array.from({ length: 3 }).map((_, i) => (
                            <div key={i} className="animate-pulse flex space-x-3">
                              <div className="h-8 w-8 bg-muted rounded-full"></div>
                              <div className="flex-1 space-y-2">
                                <div className="h-4 bg-muted rounded w-1/4"></div>
                                <div className="h-4 bg-muted rounded w-3/4"></div>
                                <div className="h-4 bg-muted rounded w-1/2"></div>
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : comments.length === 0 ? (
                        <div className="text-center py-8">
                          <MessageCircle className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                          <p className="text-muted-foreground">No comments yet. Be the first to share your thoughts!</p>
                        </div>
                      ) : (
                        <div className="space-y-6">{comments.map((comment) => renderComment(comment))}</div>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            )}
          </div>
        </div>
      </div>

      {/* Custom CSS for chapter content */}
      <style jsx>{`
        .chapter-content p {
          margin-bottom: 1.2em;
          text-align: justify;
        }
        
        .chapter-content div {
          margin-bottom: 1em;
        }
        
        .chapter-content strong,
        .chapter-content b {
          font-weight: 600;
        }
        
        .chapter-content em,
        .chapter-content i {
          font-style: italic;
        }
        
        .chapter-content u {
          text-decoration: underline;
        }
        
        .chapter-content br {
          line-height: 1.5;
        }
        
        /* Handle dialogue and special formatting */
        .chapter-content p:has(strong) {
          margin-top: 1.5em;
        }
        
        /* Ensure proper spacing for Vietnamese text */
        .chapter-content {
          word-spacing: 0.1em;
          letter-spacing: 0.02em;
        }
      `}</style>
    </div>
  )
}
