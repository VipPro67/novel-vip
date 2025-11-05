"use client"

import { useState, useEffect, useCallback, useMemo, useRef } from "react"
import type { CSSProperties } from "react"
import { Client } from "@stomp/stompjs"
import Link from "next/link"
import { useParams, useRouter } from "next/navigation"
import {
  ChevronLeft,
  ChevronRight,
  MessageCircle,
  Loader2,
  MoreVertical,
  Edit,
  Trash2,
  Reply,
  ChevronDown,
  ChevronUp,
  Send,
  RefreshCw,
  AlertCircle,
  Volume2,
  Flag,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Textarea } from "@/components/ui/textarea"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { useToast } from "@/hooks/use-toast"
import { useAuth } from "@/components/providers/auth-provider"
import { useReaderSettings } from "@/components/providers/reader-settings-provider"
import { api, type ChapterDetail, type Comment, type ReaderSettings } from "@/lib/api"
import { formatRelativeTime } from "@/lib/utils"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog"
import ChapterNavigation from "@/components/chapter-navigation"
import { ReportDialog } from "@/components/report/report-dialog"
import { Header } from "@/components/layout/header"
import Link from "next/link"

const READER_SETTINGS_STORAGE_KEY = "readerSettings"

interface ChapterContent {
  title: string
  content: string
  wordCount: number
  readingTime: number
}

interface CommentWithReplies extends Comment {
  replies: CommentWithReplies[]
  showReplies?: boolean
}

type ErrorType = "chapter-not-found" | "content-fetch-error" | "content-not-available" | null

export default function ChapterPage() {
  const params = useParams()
  const router = useRouter()
  const { toast } = useToast()
  const { isAuthenticated, loading: authLoading } = useAuth()
  const { settings: readerSettings } = useReaderSettings()
  const [cachedReaderSettings, setCachedReaderSettings] = useState<ReaderSettings | null>(null)
  const audioRef = useRef<HTMLAudioElement | null>(null)
  const wsConnectedRef = useRef(false)
  const fetchingChapterRef = useRef(false)
  const updatingProgressRef = useRef(false)

  useEffect(() => {
    if (readerSettings) {
      setCachedReaderSettings(readerSettings)
      return
    }

    if (typeof window === "undefined") {
      return
    }

    try {
      const raw = window.localStorage.getItem(READER_SETTINGS_STORAGE_KEY)
      if (!raw) {
        setCachedReaderSettings(null)
        return
      }

      const storedSettings = JSON.parse(raw) as ReaderSettings
      setCachedReaderSettings(storedSettings)
    } catch (error) {
      console.error("Failed to load reader settings from local storage", error)
      setCachedReaderSettings(null)
    }
  }, [readerSettings])

  useEffect(() => {
    if (typeof window === "undefined" || readerSettings) {
      return
    }

    const handleStorage = (event: StorageEvent) => {
      if (event.key !== READER_SETTINGS_STORAGE_KEY) {
        return
      }

      if (!event.newValue) {
        setCachedReaderSettings(null)
        return
      }

      try {
        const updatedSettings = JSON.parse(event.newValue) as ReaderSettings
        setCachedReaderSettings(updatedSettings)
      } catch (error) {
        console.error("Failed to parse updated reader settings from storage event", error)
      }
    }

    window.addEventListener("storage", handleStorage)

    return () => {
      window.removeEventListener("storage", handleStorage)
    }
  }, [readerSettings])

  const slug = params.slug as string | undefined
  const chapterNumber = Number.parseInt(params.chapterNumber as string)

  const [chapter, setChapter] = useState<ChapterDetail | null>(null)
  const [chapterContent, setChapterContent] = useState<ChapterContent | null>(null)
  const [loading, setLoading] = useState(true)
  const [contentLoading, setContentLoading] = useState(false)
  const [error, setError] = useState<ErrorType>(null)
  const [errorMessage, setErrorMessage] = useState<string>("")
  const [audioUrl, setAudioUrl] = useState<string | null>(null)
  const [audioLoading, setAudioLoading] = useState(false)
  const [audioGenerating, setAudioGenerating] = useState(false)
  const [audioError, setAudioError] = useState<string | null>(null)

  const effectiveReaderSettings = readerSettings ?? cachedReaderSettings

  const cardContentStyle = useMemo<CSSProperties>(() => {
    if (!effectiveReaderSettings) {
      return {}
    }

    return {
      padding: `${effectiveReaderSettings.marginSize ?? 20}px`,
      backgroundColor: effectiveReaderSettings.backgroundColor ?? undefined,
      color: effectiveReaderSettings.textColor ?? undefined,
    }
  }, [effectiveReaderSettings])

  const chapterTypographyStyle = useMemo<CSSProperties & Record<string, string | number>>(() => {
    const style: CSSProperties & Record<string, string | number> = {
      lineHeight: effectiveReaderSettings?.lineHeight ?? 1.8,
      fontSize: `${effectiveReaderSettings?.fontSize ?? 16}px`,
      color: effectiveReaderSettings?.textColor ?? "inherit",
      fontFamily: effectiveReaderSettings?.fontFamily ?? "inherit",
    }

    style["--paragraph-spacing"] = `${effectiveReaderSettings?.paragraphSpacing ?? 10}px`
    return style
  }, [effectiveReaderSettings])
  useEffect(() => {
    if (audioRef.current && effectiveReaderSettings?.audioSpeed) {
      audioRef.current.playbackRate = effectiveReaderSettings.audioSpeed
    }
  }, [audioUrl, effectiveReaderSettings?.audioSpeed])

  useEffect(() => {
    const audioElement = audioRef.current
    if (!audioElement || !audioUrl) {
      return
    }

    if (!effectiveReaderSettings?.audioEnabled) {
      return
    }

    const shouldAutoPlay = Boolean(effectiveReaderSettings?.audioAutoNextChapter)
    if (!shouldAutoPlay || !audioElement.paused) {
      return
    }

    const attemptPlay = async () => {
      try {
        await audioElement.play()
      } catch (error) {
        console.warn("Auto play audio failed", error)
      }
    }

    if (audioElement.readyState >= 2) {
      void attemptPlay()
      return
    }

    const handleCanPlay = () => {
      void attemptPlay()
    }

    audioElement.addEventListener("canplaythrough", handleCanPlay, { once: true })

    return () => {
      audioElement.removeEventListener("canplaythrough", handleCanPlay)
    }
  }, [audioUrl, effectiveReaderSettings?.audioAutoNextChapter, effectiveReaderSettings?.audioEnabled])

  // Comment states
  const [showComments, setShowComments] = useState(false)
  const [comments, setComments] = useState<CommentWithReplies[]>([])
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
    if (!slug || isNaN(chapterNumber) || chapterNumber <= 0) {
      return
    }
    resetCommentState()
    fetchChapter()
  }, [slug, chapterNumber])

  const fetchChapter = async () => {
    if (fetchingChapterRef.current) return
    fetchingChapterRef.current = true

    setLoading(true)
    setContentLoading(true)
    setAudioUrl(null)
    setAudioError(null)
    setAudioLoading(false)
    setError(null)
    setErrorMessage("")
    try {
      if (!slug) {
        setLoading(false)
        return
      }

      const response = await api.getChapterByNumber2(slug, chapterNumber)
      if (response.success) {
        const chapterData = response.data
        setChapter(chapterData)
        await fetchChapterContent(chapterData)
        setAudioUrl(chapterData.audioUrl ?? null)
      } else {
        setError("chapter-not-found")
        setErrorMessage(response.message || "Failed to load chapter")
        setContentLoading(false)
        toast({
          title: "Error",
          description: response.message || "Failed to load chapter",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Failed to fetch chapter:", error)
      setError("chapter-not-found")
      setErrorMessage(error instanceof Error ? error.message : "Failed to load chapter")
      toast({
        title: "Error",
        description: "Failed to load chapter. Please try again.",
        variant: "destructive",
      })
      setContentLoading(false)
    } finally {
      setLoading(false)
      fetchingChapterRef.current = false
    }
  }

  const fetchChapterContent = async (chapterData: ChapterDetail) => {
    try {
      if (!chapterData.jsonUrl) {
        setError("content-not-available")
        setErrorMessage("This chapter does not have any content yet.")
        setChapterContent(null)
        return
      }

      const response = await fetch(chapterData.jsonUrl)
      if (!response.ok) {
        throw new Error(`Failed to fetch chapter content: ${response.statusText}`)
      }

      const content = await response.json()
      setChapterContent(content)
      setError(null)
      setErrorMessage("")
    } catch (error) {
      console.error("Failed to fetch chapter content:", error)
      setError("content-fetch-error")
      setErrorMessage(error instanceof Error ? error.message : "Failed to load chapter content")
      setChapterContent(null)
      toast({
        title: "Error",
        description: "Failed to load chapter content. Please try again.",
        variant: "destructive",
      })
    } finally {
      setContentLoading(false)
    }
  }

  const handleGenerateAudio = useCallback(async () => {
    if (!chapter?.id) {
      return
    }

    setAudioLoading(true)
    setAudioError(null)
    try {
      const response = await api.getChapterAudio(chapter.id)
      if (response.success) {
        setAudioError(null)
        setAudioLoading(false)
        setAudioGenerating(true)
      } else {
        setAudioError("Audio is not available yet. Please try again shortly.")
      }
    } catch (error) {
      console.error("Failed to generate audio:", error)
      setAudioError("Failed to generate audio. Please try again.")
    } finally {
      setAudioLoading(false)
    }
  }, [chapter?.id])

  useEffect(() => {
    if (!chapter) {
      setAudioUrl(null)
      setAudioError(null)
      setAudioLoading(false)
      return
    }

    if (chapter.audioUrl) {
      setAudioUrl(chapter.audioUrl)
      setAudioError(null)
    } else {
      setAudioUrl(null)
    }
  }, [chapter])

  useEffect(() => {
    if (!chapter || wsConnectedRef.current) return

    const client = new Client({
      brokerURL: process.env.NEXT_PUBLIC_WS_URL || "ws://localhost:8081/ws",
    })

    client.onConnect = () => {
      wsConnectedRef.current = true
      client.subscribe(`/topic/chapter.${chapter.id}`, (message) => {
        const incoming: Comment = JSON.parse(message.body)
        setComments((prev) => [incoming, ...prev])
        setTotalComments((prev) => prev + 1)
      })
    }

    client.onDisconnect = () => {
      wsConnectedRef.current = false
    }

    client.activate()

    return () => {
      wsConnectedRef.current = false
      if (client.active) {
        client.deactivate()
      }
    }
  }, [chapter?.id])

  const organizeComments = (comments: Comment[]): CommentWithReplies[] => {
    const commentMap = new Map<string, CommentWithReplies>()
    const rootComments: CommentWithReplies[] = []

    comments.forEach((comment) => {
      commentMap.set(comment.id, {
        ...comment,
        replies: [],
        showReplies: true,
      })
    })

    comments.forEach((comment) => {
      const commentWithReplies = commentMap.get(comment.id)!
      if (comment.parentId) {
        const parent = commentMap.get(comment.parentId)
        if (parent) {
          parent.replies.push(commentWithReplies)
        }
      } else {
        rootComments.push(commentWithReplies)
      }
    })

    return rootComments
  }

  const loadComments = async () => {
    if (!chapter || commentsLoaded) return

    setCommentsLoading(true)
    try {
      const response = await api.getChapterComments(chapter.id, {
        page: 0,
        size: 100,
        sortBy: "createdAt",
        sortDir: "asc",
      })

      if (response.success) {
        const organizedComments = organizeComments(response.data.content || [])
        setComments(organizedComments)
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
    setShowComments(!showComments)
    if (!commentsLoaded && !showComments) {
      loadComments()
    }
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
        const newCommentWithReplies: CommentWithReplies = {
          ...response.data,
          replies: [],
          showReplies: true,
        }
        setComments([newCommentWithReplies, ...comments])
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

  const handleAddReply = async (parentId: string) => {
    if (!replyContent.trim() || !chapter) return

    setSubmittingComment(true)
    try {
      const response = await api.addComment({
        content: replyContent.trim(),
        chapterId: chapter.id,
        parentId,
      })

      if (response.success) {
        const newReply: CommentWithReplies = {
          ...response.data,
          replies: [],
          showReplies: true,
        }

        const updateComments = (comments: CommentWithReplies[]): CommentWithReplies[] => {
          return comments.map((comment) => {
            if (comment.id === parentId) {
              return {
                ...comment,
                replies: [...comment.replies, newReply],
                showReplies: true,
              }
            }
            if (comment.replies.length > 0) {
              return {
                ...comment,
                replies: updateComments(comment.replies),
              }
            }
            return comment
          })
        }

        setComments(updateComments(comments))
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

  const handleEditComment = (comment: CommentWithReplies) => {
    setEditingComment(comment.id)
    setEditContent(comment.content)
  }

  const handleSaveEdit = async (commentId: string) => {
    if (!editContent.trim()) return

    try {
      const response = await api.updateComment(commentId, {
        content: editContent.trim(),
      })

      if (response.success) {
        const updateComments = (comments: CommentWithReplies[]): CommentWithReplies[] => {
          return comments.map((comment) => {
            if (comment.id === commentId) {
              return { ...comment, ...response.data, edited: true }
            }
            if (comment.replies.length > 0) {
              return {
                ...comment,
                replies: updateComments(comment.replies),
              }
            }
            return comment
          })
        }

        setComments(updateComments(comments))
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
        const removeComment = (comments: CommentWithReplies[]): CommentWithReplies[] => {
          return comments.filter((comment) => {
            if (comment.id === commentId) {
              return false
            }
            if (comment.replies.length > 0) {
              comment.replies = removeComment(comment.replies)
            }
            return true
          })
        }

        setComments(removeComment(comments))
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

  const toggleReplies = (commentId: string) => {
    const updateComments = (comments: CommentWithReplies[]): CommentWithReplies[] => {
      return comments.map((comment) => {
        if (comment.id === commentId) {
          return { ...comment, showReplies: !comment.showReplies }
        }
        if (comment.replies.length > 0) {
          return {
            ...comment,
            replies: updateComments(comment.replies),
          }
        }
        return comment
      })
    }

    setComments(updateComments(comments))
  }

  const formatRelativeDate = (dateString: string) => {
    const value = formatRelativeTime(dateString)
    return value || "just now"
  }

  const renderComment = (comment: CommentWithReplies, depth = 0) => {
    const maxDepth = 3
    const isMaxDepth = depth >= maxDepth

    return (
      <div key={comment.id} className={`space-y-3 ${depth > 0 ? "ml-6 pl-4 border-l-2 border-muted" : ""}`}>
        <div className="space-y-2">
          <div className="flex items-start justify-between">
            <div className="flex items-center space-x-2">
              <Avatar className="h-8 w-8">
                <AvatarFallback>{comment.username.charAt(0).toUpperCase()}</AvatarFallback>
              </Avatar>
              <div className="flex items-center space-x-2">
                <span className="font-medium text-sm">{comment.username}</span>
                {comment.edited && (
                  <Badge variant="secondary" className="text-xs">
                    edited
                  </Badge>
                )}
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <span className="text-xs text-muted-foreground">{formatRelativeDate(comment.updatedAt)}</span>
              {/* Assume all users can edit/delete for demo, add owner check if needed */}
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="sm">
                    <MoreVertical className="h-4 w-4" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem onClick={() => handleEditComment(comment)}>
                    <Edit className="mr-2 h-4 w-4" />
                    Edit
                  </DropdownMenuItem>
                  <ReportDialog
                    reportType="COMMENT"
                    targetId={comment.id}
                    targetTitle={`Comment by ${comment.username}`}
                    trigger={
                      <DropdownMenuItem onSelect={(e) => e.preventDefault()}>
                        <Flag className="mr-2 h-4 w-4" />
                        Report
                      </DropdownMenuItem>
                    }
                  />
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <DropdownMenuItem onSelect={(e) => e.preventDefault()}>
                        <Trash2 className="mr-2 h-4 w-4" />
                        Delete
                      </DropdownMenuItem>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Delete Comment</AlertDialogTitle>
                        <AlertDialogDescription>
                          Are you sure you want to delete this comment? This action cannot be undone.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                          onClick={() => handleDeleteComment(comment.id)}
                          className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                        >
                          Delete
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </div>

          {editingComment === comment.id ? (
            <div className="space-y-2">
              <Textarea value={editContent} onChange={(e) => setEditContent(e.target.value)} className="min-h-[80px]" />
              <div className="flex justify-end space-x-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setEditingComment(null)
                    setEditContent("")
                  }}
                >
                  Cancel
                </Button>
                <Button size="sm" onClick={() => handleSaveEdit(comment.id)} disabled={!editContent.trim()}>
                  Save
                </Button>
              </div>
            </div>
          ) : (
            <>
              <div className="bg-muted/50 rounded-lg p-3">
                <p className="text-sm whitespace-pre-wrap">{comment.content}</p>
              </div>

              {/* Reply button and controls */}
              <div className="flex items-center space-x-2">
                {!isMaxDepth && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setReplyingTo(replyingTo === comment.id ? null : comment.id)}
                    className="text-xs"
                  >
                    <Reply className="mr-1 h-3 w-3" />
                    Reply
                  </Button>
                )}

                {comment.replies.length > 0 && (
                  <Button variant="ghost" size="sm" onClick={() => toggleReplies(comment.id)} className="text-xs">
                    {comment.showReplies ? (
                      <>
                        <ChevronUp className="mr-1 h-3 w-3" />
                        Hide {comment.replies.length} {comment.replies.length === 1 ? "reply" : "replies"}
                      </>
                    ) : (
                      <>
                        <ChevronDown className="mr-1 h-3 w-3" />
                        Show {comment.replies.length} {comment.replies.length === 1 ? "reply" : "replies"}
                      </>
                    )}
                  </Button>
                )}
              </div>

              {/* Reply form */}
              {replyingTo === comment.id && (
                <div className="space-y-2 mt-3">
                  <Textarea
                    placeholder={`Reply to ${comment.username}...`}
                    value={replyContent}
                    onChange={(e) => setReplyContent(e.target.value)}
                    className="min-h-[80px]"
                  />
                  <div className="flex justify-end space-x-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        setReplyingTo(null)
                        setReplyContent("")
                      }}
                    >
                      Cancel
                    </Button>
                    <Button
                      size="sm"
                      onClick={() => handleAddReply(comment.id)}
                      disabled={!replyContent.trim() || submittingComment}
                    >
                      {submittingComment ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Posting...
                        </>
                      ) : (
                        <>
                          <Send className="mr-2 h-4 w-4" />
                          Reply
                        </>
                      )}
                    </Button>
                  </div>
                </div>
              )}
            </>
          )}
        </div>

        {/* Render replies */}
        {comment.showReplies && comment.replies.length > 0 && (
          <div className="space-y-3">{comment.replies.map((reply) => renderComment(reply, depth + 1))}</div>
        )}
      </div>
    )
  }

  const navigateChapter = (direction: "prev" | "next") => {
    const newChapterNumber = direction === "prev" ? chapterNumber - 1 : chapterNumber + 1
    if (newChapterNumber > 0 && slug) {
      router.push(`/novels/${slug}/chapters/${newChapterNumber}`)
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

  const renderAudioSection = () => {
    if (!chapter) {
      return null
    }

    if (effectiveReaderSettings && !effectiveReaderSettings.audioEnabled) {
      return null
    }

    const canGenerateAudio = !authLoading && isAuthenticated

    if (audioGenerating) {
      return (
        <div className="flex items-center space-x-3 rounded-lg border border-muted p-4">
          <Loader2 className="h-5 w-5 animate-spin text-primary" />
          <span className="text-sm text-muted-foreground">Generating audio it can take 3-5 minutes, you will get notification while it ready</span>
        </div>
      )
    }

    if (audioLoading) {
      return (
        <div className="flex items-center space-x-3 rounded-lg border border-muted p-4">
          <Loader2 className="h-5 w-5 animate-spin text-primary" />
          <span className="text-sm text-muted-foreground">Preparing audio...</span>
        </div>
      )
    }

    if (audioUrl) {
      return (
        <div className="rounded-lg border border-muted p-2">
          <audio
            ref={(element) => {
              audioRef.current = element
              if (element && effectiveReaderSettings?.audioSpeed) {
                element.playbackRate = effectiveReaderSettings.audioSpeed
              }
              if (element && effectiveReaderSettings?.audioAutoNextChapter) {
                element.autoplay = true
              }
            }}
            controls
            preload="none"
            className="w-full"
            onEnded={() => {
              if (effectiveReaderSettings?.audioAutoNextChapter) {
                navigateChapter("next")
              }
            }}
          >
            <source src={audioUrl} type="audio/mpeg" />
            Your browser does not support the audio element.
          </audio>
        </div>
      )
    }

    if (audioError) {
      return (
        <div className="flex flex-col gap-3 rounded-lg border border-destructive/30 bg-destructive/10 p-4 md:flex-row md:items-center md:justify-between">
          <div className="flex items-start space-x-3">
            <AlertCircle className="mt-0.5 h-5 w-5 text-destructive" />
            <div>
              <p className="text-sm font-medium text-destructive">Audio unavailable</p>
              <p className="text-xs text-muted-foreground">{audioError}</p>
            </div>
          </div>
          {canGenerateAudio ? (
            <Button
              size="sm"
              variant="outline"
              onClick={handleGenerateAudio}
              className="self-start md:self-auto bg-transparent"
            >
              <RefreshCw className="mr-2 h-4 w-4" />
              Try again
            </Button>
          ) : (
            !authLoading && (
              <p className="text-xs text-muted-foreground">Sign in to generate an audio version for this chapter.</p>
            )
          )}
        </div>
      )
    }

    return (
      <div className="flex flex-col gap-3 rounded-lg border border-muted p-4 md:flex-row md:items-center md:justify-between">
        <div className="flex items-start space-x-3">
          <AlertCircle className="mt-0.5 h-5 w-5 text-muted-foreground" />
          <div>
            <p className="text-sm font-medium">Audio not available yet</p>
            <p className="text-xs text-muted-foreground">
              {authLoading
                ? "Checking audio availability..."
                : canGenerateAudio
                  ? "Generate an audio version for this chapter."
                  : "Sign in to generate an audio version for this chapter."}
            </p>
          </div>
        </div>
        {canGenerateAudio && (
          <Button
            size="sm"
            variant="outline"
            onClick={handleGenerateAudio}
            className="self-start md:self-auto bg-transparent"
          >
            <RefreshCw className="mr-2 h-4 w-4" />
            Generate audio
          </Button>
        )}
      </div>
    )
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

  if (!chapter || error === "chapter-not-found") {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <Card>
            <CardContent className="p-8">
              <div className="text-center space-y-4">
                <AlertCircle className="h-16 w-16 text-destructive mx-auto" />
                <h1 className="text-2xl font-bold">Chapter Not Found</h1>
                <p className="text-muted-foreground">
                  {errorMessage || "The chapter you're looking for could not be found."}
                </p>
                <div className="flex justify-center gap-3">
                  <Button onClick={() => router.back()} variant="outline">
                    <ChevronLeft className="h-4 w-4 mr-2" />
                    Go Back
                  </Button>
                  <Button onClick={fetchChapter}>
                    <RefreshCw className="h-4 w-4 mr-2" />
                    Try Again
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
        {/* Chapter Content */}
        <div className="container mx-auto px-4 py-4">
          <div className="max-w-6xl mx-auto">
            {/* Breadcrumb: Novels > slug > Chapter number */}
            <div className="mb-4 text-sm text-muted-foreground">
              <nav aria-label="Breadcrumb" className="flex items-center space-x-2">
                <Link href="/novels" className="text-sm text-primary hover:underline">
                  Novels
                </Link>
                <ChevronRight className="h-4 w-4 text-muted-foreground" />
                {slug ? (
                  <Link href={`/novels/${slug}`} className="text-sm hover:underline">
                    {slug}
                  </Link>
                ) : (
                  <span className="text-sm text-muted-foreground">{slug ?? "..."}</span>
                )}
                <ChevronRight className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm font-medium">Chapter {chapterNumber}</span>
              </nav>
            </div>

            <Card>
              <CardContent className="p-8" style={cardContentStyle}>
                <div className="space-y-6">
                  <div className="mb-8">
                    <h1 className="text-3xl font-bold mb-2">{chapter.title}</h1>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <span>Chapter {chapterNumber}</span>
                      {chapterContent?.wordCount && (
                        <>
                          <span>•</span>
                          <span>{chapterContent.wordCount.toLocaleString()} words</span>
                        </>
                      )}
                      {chapterContent?.readingTime && (
                        <>
                          <span>•</span>
                          <span>{chapterContent.readingTime} min read</span>
                        </>
                      )}
                    </div>
                  </div>

            <Card>
              <CardContent className="p-8" style={cardContentStyle}>
                <div className="space-y-6">
                  {renderAudioSection()}
                  {contentLoading ? (
                    <div className="animate-pulse space-y-4">
                      {Array.from({ length: 15 }).map((_, i) => (
                        <div key={i} className="h-4 bg-muted rounded"></div>
                      ))}
                    </div>
                  ) : error ? (
                    <div className="text-center py-12 space-y-4">
                      <AlertCircle className="h-12 w-12 text-destructive mx-auto" />
                      <div>
                        <h3 className="text-lg font-semibold mb-2">
                          {error === "content-not-available" ? "Content Not Available" : "Error Loading Content"}
                        </h3>
                        <p className="text-muted-foreground">{errorMessage || "Unable to load chapter content"}</p>
                      </div>
                      {error === "content-fetch-error" && (
                        <Button onClick={() => chapter && fetchChapterContent(chapter)} variant="outline">
                          <RefreshCw className="h-4 w-4 mr-2" />
                          Retry
                        </Button>
                      )}
                    </div>
                  ) : chapterContent ? (
                    <div className="prose prose-lg dark:prose-invert max-w-none">
                      <div
                        className="chapter-content leading-relaxed text-base"
                        dangerouslySetInnerHTML={renderHtmlContent(chapterContent.content)}
                        style={chapterTypographyStyle}
                      />
                    </div>
                  ) : null}
                </div>
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

            {chapter && (
              <div className="mt-6">
                <Card>
                  <CardContent className="p-4">
                    <ChapterNavigation
                      novelSlug={slug!}
                      currentChapterNumber={chapterNumber}
                      currentChapterTitle={chapter.title}
                    />
                  </CardContent>
                </Card>
              </div>
            )}

            {/* Comments Section */}
            <div className="mt-8 text-center">
              <Button variant="outline" size="lg" onClick={handleShowComments} className="mb-6 bg-transparent">
                <MessageCircle className="h-4 w-4 mr-2" />
                {showComments ? "Hide Comments" : "Show Comments"}
                {totalComments > 0 && (
                  <Badge variant="secondary" className="ml-2">
                    {totalComments > 99 ? "99+" : totalComments}
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
                            <>
                              <Send className="mr-2 h-4 w-4" />
                              Post Comment
                            </>
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
                            <p className="text-muted-foreground">
                              No comments yet. Be the first to share your thoughts!
                            </p>
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
          margin-bottom: var(--paragraph-spacing, 1.2em);
          text-align: justify;
        }
        
        .chapter-content div {
          margin-bottom: var(--paragraph-spacing, 1em);
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
