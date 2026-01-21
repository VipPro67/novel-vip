import Image from "next/image"
import { Link } from "@/navigation"
import { Star, Eye, BookOpen, ChevronRight } from "lucide-react"
import { useTranslations } from "next-intl"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Novel, ReadingHistory } from "@/models"

interface ReadingHistoryCardProps {
	history: ReadingHistory
	hideImage?: boolean
}

export function ReadingHistoryCard({ history, hideImage = false }: ReadingHistoryCardProps) {
	const t = useTranslations("ReadingHistoryCard")
	const novel = history.novel
	const progressPercentage = novel.totalChapters > 0 
		? Math.round((history.lastReadChapterIndex / novel.totalChapters) * 100)
		: 0

	const formatDate = (dateString: string) => {
		const date = new Date(dateString)
		const now = new Date()
		const diffMs = now.getTime() - date.getTime()
		const diffMins = Math.floor(diffMs / 60000)
		const diffHours = Math.floor(diffMs / 3600000)
		const diffDays = Math.floor(diffMs / 86400000)

		if (diffMins < 60) return `${diffMins}${t("timeAgo.minutesAgo")}`
		if (diffHours < 24) return `${diffHours}${t("timeAgo.hoursAgo")}`
		if (diffDays < 7) return `${diffDays}${t("timeAgo.daysAgo")}`
		
		return date.toLocaleDateString()
	}

	return (
		<Card className="overflow-hidden hover:shadow-lg transition-shadow w-full flex flex-col h-full">
			{/* Cover Image Section */}
			{!hideImage && (
				<Link href={`/novels/${novel.slug}/chapters/${history.lastReadChapterIndex}`} className="flex-shrink-0">
					<div className="aspect-[3/4] sm:aspect-[2/3] md:aspect-[3/4] relative">
						<Image
							src={novel.imageUrl || "/placeholder.svg?height=400&width=300"}
							alt={novel.title}
							fill
							className="object-cover"
						/>
						<div className="absolute top-1 right-1 sm:top-1 sm:right-2">
							<Badge variant="secondary" className="text-[8px] px-1 py-0.5 sm:px-2 sm:py-1">
								{novel.status}
							</Badge>
						</div>
					</div>
				</Link>
			)}

			{/* Content Section */}
			<CardContent className="p-2 sm:p-3 md:p-4 flex-grow flex flex-col justify-between">
				{/* Title and Author */}
				<div>
					<Link href={`/novels/${novel.slug}/chapters/${history.lastReadChapterIndex}`}>
						<h3 className="font-semibold text-xs sm:text-sm mb-1 line-clamp-2 leading-tight hover:text-primary">
							{novel.title}
						</h3>
					</Link>
					<p className="text-xs text-muted-foreground hidden sm:block truncate">{novel.author}</p>
				</div>

				{/* Stats */}
				{!hideImage && (
				<div className="flex items-center justify-between text-xs text-muted-foreground">
					{/* Rating */}
					<div className="flex items-center space-x-1">
						<Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
						<span className="hidden sm:inline">{novel.rating}/5</span>
						<span className="sm:hidden">{novel.rating}</span>
					</div>

					{/* Views - Hidden on very small screens */}
					<div className="flex items-center space-x-1 hidden xs:flex">
						<Eye className="h-3 w-3" />
						<span className="hidden sm:inline">{novel.totalViews?.toLocaleString()}</span>
						<span className="sm:hidden">
							{novel.totalViews > 1000 ? `${Math.floor(novel.totalViews / 1000)}k` : novel.totalViews}
						</span>
					</div>

					{/* Chapters */}
					<div className="flex items-center space-x-1">
						<BookOpen className="h-3 w-3" />
						<span>{novel.totalChapters}</span>
					</div>
					{/* Author on mobile - moved to bottom */}
					<p className="text-xs text-muted-foreground mt-2 sm:hidden truncate mb-2">{t("by")} {novel.author}</p>

				</div>)}


				{/* Reading Progress Section */}
				<div className="border-t pt-1">
					{/* Current Chapter Info */}
					<div className="mb-1">
						<div className="flex items-center justify-between mb-1">
							<span className="text-xs font-medium">{t("chapter")} {history.lastReadChapterIndex}</span>
							<span className="text-xs text-muted-foreground">{progressPercentage}%</span>
						</div>
						{/* Progress Bar */}
						<div className="w-full h-1.5 bg-muted rounded-full overflow-hidden">
							<div
								className="h-full bg-primary transition-all duration-300"
								style={{ width: `${progressPercentage}%` }}
							/>
						</div>
					</div>

					{/* Last Read Time */}
					<p className="text-xs text-muted-foreground">
						{t("lastRead")} {formatDate(history.lastReadAt)}
					</p>
				</div>
			</CardContent>
		</Card>
	)
}
