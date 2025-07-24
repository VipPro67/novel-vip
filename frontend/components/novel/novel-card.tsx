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
    <Card className="overflow-hidden hover:shadow-lg transition-shadow">
      <Link href={`/novels/${novel.id}`}>
        <div className="aspect-[3/4] relative">
          <Image
            src={novel.coverImage || "/placeholder.svg?height=400&width=300"}
            alt={novel.title}
            fill
            className="object-cover"
          />
          <div className="absolute top-2 right-2">
            <Badge variant="secondary" className="text-xs">
              {novel.status}
            </Badge>
          </div>
        </div>
        <CardContent className="p-4">
          <h3 className="font-semibold text-sm mb-1 line-clamp-2">{novel.title}</h3>
          <p className="text-xs text-muted-foreground mb-2">by {novel.author}</p>
          <p className="text-xs text-muted-foreground line-clamp-2 mb-3">{novel.description}</p>

          <div className="flex items-center justify-between text-xs text-muted-foreground">
            <div className="flex items-center space-x-1">
              <Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
              <span>{novel.rating}/5</span>
            </div>
            <div className="flex items-center space-x-1">
              <Eye className="h-3 w-3" />
              <span>{novel.views.toLocaleString()}</span>
            </div>
            <div className="flex items-center space-x-1">
              <BookOpen className="h-3 w-3" />
              <span>{novel.totalChapters}</span>
            </div>
          </div>

          <div className="flex flex-wrap gap-1 mt-2">
            {novel.categories.slice(0, 2).map((category) => (
              <Badge key={category.id} variant="outline" className="text-xs">
                {category.name}
              </Badge>
            ))}
          </div>
        </CardContent>
      </Link>
    </Card>
  )
}
