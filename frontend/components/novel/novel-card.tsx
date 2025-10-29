import Image from "next/image"
import Link from "next/link"
import { Star, Eye, BookOpen } from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import type { Novel } from "@/lib/api"

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
					<div className="absolute top-1 right-1 sm:top-2 sm:right-2">
						<Badge variant="secondary" className="text-xs px-1 py-0.5 sm:px-2 sm:py-1">
							{novel.status}
						</Badge>
					</div>
				</div>

				{/* Content - Compact on mobile */}
				<CardContent className="p-2 sm:p-3 md:p-4">
					{/* Title and Author */}
					<h3 className="font-semibold text-xs sm:text-sm mb-1 line-clamp-2 leading-tight">{novel.title}</h3>
					<p className="text-xs text-muted-foreground mb-2 hidden sm:block">by {novel.author}</p>

					{/* Description - Hidden on mobile */}
					<p className="text-xs text-muted-foreground mb-2 hidden md:block">
						{novel.description?.length > 100
							? novel.description.substring(0, 100) + "..."
							: novel.description}
					</p>

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
							<span className="hidden sm:inline">{novel.views.toLocaleString()}</span>
							<span className="sm:hidden">
								{novel.views > 1000 ? `${Math.floor(novel.views / 1000)}k` : novel.views}
							</span>
						</div>

						{/* Chapters */}
						<div className="flex items-center space-x-1">
							<BookOpen className="h-3 w-3" />
							<span>{novel.totalChapters}</span>
						</div>
					</div>

					{/* Categories - Limited on mobile */}
					<div className="flex flex-wrap gap-1">
						{novel.categories.slice(0, 1).map((category) => (
							<Badge key={category.id} variant="outline" className="text-xs px-1 py-0.5 sm:px-2">
								{category.name}
							</Badge>
						))}
						{/* Show second category only on larger screens */}
						{novel.categories.length > 1 && (
							<Badge
								key={novel.categories[1].id}
								variant="outline"
								className="text-xs px-1 py-0.5 sm:px-2 hidden sm:inline-flex"
							>
								{novel.categories[1].name}
							</Badge>
						)}
						{/* Show more indicator on mobile if there are more categories */}
						{novel.categories.length > 1 && (
							<Badge variant="outline" className="text-xs px-1 py-0.5 sm:hidden">
								+{novel.categories.length - 1}
							</Badge>
						)}
					</div>

					{/* Author on mobile - moved to bottom */}
					<p className="text-xs text-muted-foreground mt-2 sm:hidden truncate">by {novel.author}</p>
				</CardContent>
			</Link>
		</Card>
	)
}
