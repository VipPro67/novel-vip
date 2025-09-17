"use client"

import { useEffect, useState } from "react"
import { useParams, useRouter } from "next/navigation"
import Image from "next/image"
import Link from "next/link"
import {
	Heart,
	Star,
	Eye,
	BookOpen,
	MessageCircle,
	Play,
	Edit,
	Trash2,
	MoreVertical,
	Reply,
	ChevronDown,
	ChevronUp,
	Send,
	Loader2,
	Clock,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Textarea } from "@/components/ui/textarea"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Separator } from "@/components/ui/separator"
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
import { Header } from "@/components/layout/header"
import { useAuth } from "@/components/providers/auth-provider"
import { useToast } from "@/hooks/use-toast"
import { api, type Novel, type Chapter, type Comment } from "@/lib/api"
import { formatRelativeTime } from "@/lib/utils"

interface CommentWithReplies extends Comment {
	replies: CommentWithReplies[]
	showReplies?: boolean
}

export default function NovelDetailPage() {
	const params = useParams()
	const slug = params.slug as string | undefined
	const router = useRouter()
	const { isAuthenticated, user } = useAuth()
	const { toast } = useToast()
	const [novel, setNovel] = useState<Novel | null>(null)
	const [chapters, setChapters] = useState<Chapter[]>([])
	const [comments, setComments] = useState<CommentWithReplies[]>([])
	const [isFavorite, setIsFavorite] = useState(false)
	const [userRating, setUserRating] = useState(0)
	const [newComment, setNewComment] = useState("")
	const [editingComment, setEditingComment] = useState<string | null>(null)
	const [editContent, setEditContent] = useState("")
	const [loading, setLoading] = useState(true)
	const [tab, setTab] = useState("chapters") // default tab

	// Comment state
	const [showComments, setShowComments] = useState(false)
	const [commentsLoading, setCommentsLoading] = useState(false)
	const [submittingComment, setSubmittingComment] = useState(false)
	const [totalComments, setTotalComments] = useState(0)
	const [commentsLoaded, setCommentsLoaded] = useState(false)

	// Reply state
	const [replyingTo, setReplyingTo] = useState<string | null>(null)
	const [replyContent, setReplyContent] = useState("")
	const [submittingReply, setSubmittingReply] = useState(false)

	useEffect(() => {
		if (slug) {
			fetchNovelData(slug)
		}
	}, [slug])

	const fetchNovelData = async (novelSlug: string) => {
		setLoading(true)
		try {
			const novelResponse = await api.getNovelBySlug(novelSlug)

			if (!novelResponse.success) {
				throw new Error(novelResponse.message || "Failed to load novel")
			}

			const novelData = novelResponse.data
			setNovel(novelData)

			const chaptersResponse = await api.getChaptersByNovel(novelData.id, { size: 50 })
			if (chaptersResponse.success) {
				setChapters(chaptersResponse.data.content)
			} else {
				setChapters([])
			}

			if (isAuthenticated) {
				// Check favorite status
				try {
					const favoriteResponse = await api.checkFavoriteStatus(novelData.id)
					if (favoriteResponse.success) {
						setIsFavorite(favoriteResponse.data)
					}
				} catch (error) {
					console.error("Failed to check favorite status:", error)
				}

				// Get user rating
				try {
					const ratingResponse = await api.getUserRating(novelData.id)
					if (ratingResponse.success) {
						setUserRating(ratingResponse.data.score)
					}
				} catch (error) {
					console.error("Failed to get user rating:", error)
				}
			}
		} catch (error) {
			console.error("Failed to fetch novel data:", error)
			toast({
				title: "Error",
				description: "Failed to load novel data",
				variant: "destructive",
			})
		} finally {
			setLoading(false)
		}
	}

	const organizeComments = (comments: Comment[]): CommentWithReplies[] => {
		const commentMap = new Map<string, CommentWithReplies>()
		const rootComments: CommentWithReplies[] = []

		// First pass: create all comments with empty replies array
		comments.forEach((comment) => {
			commentMap.set(comment.id, {
				...comment,
				replies: [],
				showReplies: true,
			})
		})

		// Second pass: organize into tree structure
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

	const fetchNovelComments = async () => {
		if (!novel || commentsLoaded) return

		setCommentsLoading(true)
		try {
			const response = await api.getNovelComments(novel.id, {
				page: 0,
				size: 100,
				sortBy: "createdAt",
				sortDir: "asc",
			})

			if (response.success) {
				const organizedComments = organizeComments(response.data.content)
				setComments(organizedComments)
				setTotalComments(response.data.totalElements)
				setCommentsLoaded(true)
			}
		} catch (error) {
			console.error("Failed to fetch comments:", error)
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
		if (!commentsLoaded) {
			fetchNovelComments()
		}
		setTab("comments")
	}

	const handleFavoriteToggle = async () => {
		if (!isAuthenticated) {
			router.push("/login")
			return
		}

		if (!novel) {
			return
		}

		try {
			if (isFavorite) {
				await api.removeFromFavorites(novel.id)
				setIsFavorite(false)
				toast({ title: "Removed from favorites" })
			} else {
				await api.addToFavorites(novel.id)
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

		if (!novel) {
			return
		}

		try {
			await api.rateNovel(novel.id, rating)
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

		if (!newComment.trim() || !novel) return

		setSubmittingComment(true)
		try {
			const response = await api.addComment({
				content: newComment.trim(),
				novelId: novel.id,
			})

			if (response.success) {
				const newCommentWithReplies: CommentWithReplies = {
					...response.data,
					replies: [],
					showReplies: true,
				}
				setComments([newCommentWithReplies, ...comments])
				setTotalComments(totalComments + 1)
				setNewComment("")
				toast({ title: "Comment added successfully" })
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
		if (!isAuthenticated) {
			router.push("/login")
			return
		}

		if (!replyContent.trim() || !novel) return

		setSubmittingReply(true)
		try {
			const response = await api.addComment({
				content: replyContent.trim(),
				novelId: novel.id,
				parentId: parentId,
			})

			if (response.success) {
				const newReply: CommentWithReplies = {
					...response.data,
					replies: [],
					showReplies: true,
				}

				// Add reply to the correct parent comment
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
				setTotalComments(totalComments + 1)
				setReplyContent("")
				setReplyingTo(null)
				toast({ title: "Reply added successfully" })
			}
		} catch (error) {
			console.error("Failed to add reply:", error)
			toast({
				title: "Error",
				description: "Failed to add reply",
				variant: "destructive",
			})
		} finally {
			setSubmittingReply(false)
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
				toast({ title: "Comment updated successfully" })
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
			await api.deleteComment(commentId)

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
			setTotalComments(totalComments - 1)
			toast({ title: "Comment deleted successfully" })
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

	const startReading = () => {
		if (chapters.length > 0) {
			const targetSlug = novel?.slug ?? slug
			if (!targetSlug) {
				return
			}
			router.push(`/novels/${targetSlug}/chapters/${chapters[0].chapterNumber}`)
		}
	}

	const isCommentOwner = (comment: CommentWithReplies) => {
		return user && comment.userId === user.id
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
								{comment.edited || (comment.createdAt !== comment.updatedAt) && (
									<Badge variant="secondary" className="text-xs">
										edited
									</Badge>
								)}
							</div>
						</div>
						<div className="flex items-center space-x-2">
							<span className="text-xs text-muted-foreground">{formatRelativeDate(comment.updatedAt)}</span>
							{isCommentOwner(comment) && (
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
							)}
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
											disabled={!replyContent.trim() || submittingReply}
										>
											{submittingReply ? (
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

	if (loading) {
		return (
			<div className="min-h-screen bg-background">
				<Header />
				<div className="container mx-auto px-4 py-8">
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
				<div className="container mx-auto px-4 py-8">
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

			<main className="container mx-auto px-4 py-8">
				<div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
					{/* Novel Cover and Actions */}
					<div className="lg:col-span-1">
						<Card>
							<CardContent className="p-6">
								<div className="aspect-[3/4] relative mb-4">
									<Image
										src={novel.coverImage?.fileUrl || "/placeholder.svg?height=600&width=450"}
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

										<Button variant="outline" className="w-full bg-transparent" onClick={handleShowComments}>
											<MessageCircle className="mr-2 h-4 w-4" />
											Comments
											{totalComments > 0 && (
												<Badge variant="secondary" className="ml-1 h-5 w-5 rounded-full p-0 text-xs">
													{totalComments > 99 ? "99+" : totalComments}
												</Badge>
											)}
										</Button>
									</div>

									{/* Rating */}
									<div className="text-center">
										<p className="text-sm text-muted-foreground mb-2">Rate this novel</p>
										<div className="flex justify-center space-x-1">
											{[1, 2, 3, 4, 5].map((rating) => (
												<button key={rating} onClick={() => handleRating(rating)} className="p-1">
													<Star
														className={`h-5 w-5 ${rating <= userRating ? "fill-yellow-400 text-yellow-400" : "text-gray-300"
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
								<div className="flex items-center space-x-1">
									<Clock className="h-4 w-4" />
									<span>Updated {formatRelativeDate(novel.updatedAt)}</span>
								</div>
								<Badge variant="secondary">{novel.status}</Badge>
								</div>

								<div className="flex flex-wrap gap-2 mb-4">
									{novel.categories?.map((category) => (
										<Badge key={category.id} variant="outline">
											{category.name}
										</Badge>
									))}
                                    {novel.genres && novel.genres.map((genre) => (
                                        <Badge key={genre.id} variant="outline">
                                            {genre.name}
                                        </Badge>
                                    ))}
                                    {novel.tags && novel.tags.map((tag) => (
                                        <Badge key={tag.id} variant="outline">
                                            {tag.name}
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
							<Tabs value={tab} onValueChange={setTab} defaultValue="chapters" className="w-full">
								<TabsList className="grid w-full grid-cols-2">
									<TabsTrigger value="chapters">Chapters</TabsTrigger>
									<TabsTrigger value="comments" onClick={() => !commentsLoaded && fetchNovelComments()}>
										Comments
									</TabsTrigger>
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
														href={`/novels/${novel.slug}/chapters/${chapter.chapterNumber}`}
														className="block p-3 rounded-lg border hover:bg-muted transition-colors"
													>
														<div className="flex justify-between items-center">
															<div>
																<p className="font-medium">
																	{chapter.title}
																</p>
																<p className="text-sm text-muted-foreground">
																	Updated {formatRelativeDate(chapter.updatedAt)}
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
									{/* Add Comment Form */}
									{isAuthenticated ? (
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
														className="min-h-[100px]"
													/>
													<Button onClick={handleCommentSubmit} disabled={!newComment.trim() || submittingComment}>
														{submittingComment ? (
															<>
																<Loader2 className="mr-2 h-4 w-4 animate-spin" />
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
											</CardContent>
										</Card>
									) : (
										<Card>
											<CardContent className="text-center py-8">
												<p className="text-muted-foreground mb-4">Please log in to comment</p>
												<Button asChild>
													<Link href="/login">Login</Link>
												</Button>
											</CardContent>
										</Card>
									)}

									<Card>
										<CardHeader>
											<CardTitle>Comments ({totalComments})</CardTitle>
										</CardHeader>
										<CardContent>
											{commentsLoading ? (
												<div className="space-y-4">
													{Array.from({ length: 3 }).map((_, i) => (
														<div key={i} className="animate-pulse space-y-2">
															<div className="flex items-center space-x-2">
																<div className="h-8 w-8 bg-muted rounded-full" />
																<div className="h-4 w-24 bg-muted rounded" />
																<div className="h-3 w-16 bg-muted rounded" />
															</div>
															<div className="h-16 bg-muted rounded" />
														</div>
													))}
												</div>
											) : comments.length > 0 ? (
												<div className="space-y-6">{comments.map((comment) => renderComment(comment))}</div>
											) : (
												<div className="text-center py-8">
													<MessageCircle className="mx-auto h-12 w-12 text-muted-foreground mb-4" />
													<p className="text-muted-foreground">No comments yet. Be the first to share your thoughts!</p>
												</div>
											)}
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
