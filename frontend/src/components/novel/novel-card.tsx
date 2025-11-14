import Image from "next/image"
import { Link } from "@/navigation"
import { Star, Eye, BookOpen } from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import type { Novel } from "@/services/api"

interface NovelCardProps {
	novel: Novel
}

export function NovelCard({ novel }: NovelCardProps) {
	return (
		<Card className="overflow-hidden hover:shadow-lg transition-shadow w-full">
			<Link href={`/novels/${novel.slug}`}>
				{/* Cover Image - Smaller on mobile */}
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

				{/* Content - Compact on mobile */}
				<CardContent className="p-2 sm:p-3 md:p-4">
					{/* Title and Author */}
					<h3 className="font-semibold text-xs sm:text-sm mb-1 line-clamp-2 leading-tight">{novel.title}</h3>
					<p className="text-xs text-muted-foreground mb-2 hidden sm:block">{novel.author}</p>

					{/* Stats - Simplified on mobile */}
					<div className="flex items-center justify-between text-xs text-muted-foreground mb-2">
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
					</div>
					{/* Author on mobile - moved to bottom */}
					<p className="text-xs text-muted-foreground mt-2 sm:hidden truncate">by {novel.author}</p>
				</CardContent>
			</Link>
		</Card>
	)
}
