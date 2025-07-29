"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { ArrowRight, TrendingUp, Star, Clock, BookOpen } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Header } from "@/components/layout/header"
import { NovelCard } from "@/components/novel/novel-card"
import { useAuth } from "@/components/providers/auth-provider"
import { api, type Novel } from "@/lib/api"

export default function HomePage() {
  const { isAuthenticated } = useAuth()
  const [hotNovels, setHotNovels] = useState<Novel[]>([])
  const [topRatedNovels, setTopRatedNovels] = useState<Novel[]>([])
  const [latestNovels, setLatestNovels] = useState<Novel[]>([])
  const [recentlyRead, setRecentlyRead] = useState<Novel[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchHomeData()
  }, [isAuthenticated])

  const fetchHomeData = async () => {
    try {
      const [hotResponse, topRatedResponse, latestResponse] = await Promise.all([
        api.getHotNovels({ size: 6 }),
        api.getTopRatedNovels({ size: 6 }),
        api.getLatestNovels({ size: 6 }),
      ])

      if (hotResponse.success) {
        setHotNovels(hotResponse.data.content)
      }
      if (topRatedResponse.success) {
        setTopRatedNovels(topRatedResponse.data.content)
      }
      if (latestResponse.success) {
        setLatestNovels(latestResponse.data.content)
      }

      // Fetch recently read novels if user is authenticated
      if (isAuthenticated) {
        try {
          const recentResponse = await api.getRecentlyRead(6)
          if (recentResponse.success) {
            setRecentlyRead(recentResponse.data)
          }
        } catch (error) {
          console.error("Failed to fetch recently read novels:", error)
        }
      }
    } catch (error) {
      console.error("Failed to fetch home data:", error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <main>
        {/* Hero Section */}
        <section className="relative py-20 px-4 text-center bg-gradient-to-b from-primary/10 to-background">
          <div className="container mx-auto px-4">
            <div className="max-w-4xl mx-auto">
              <h1 className="text-4xl md:text-6xl font-bold mb-6 text-foreground">Discover Amazing Stories</h1>
              <p className="text-xl md:text-2xl text-muted-foreground mb-8 leading-relaxed">
                Dive into a world of captivating novels, from fantasy epics to romantic tales. Your next favorite story
                awaits.
              </p>
              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <Button asChild size="lg" className="text-lg px-8">
                  <Link href="/novels">
                    Browse Novels
                    <ArrowRight className="ml-2 h-5 w-5" />
                  </Link>
                </Button>
                <Button asChild variant="outline" size="lg" className="text-lg px-8 bg-transparent">
                  <Link href="/novels/hot">Start Reading Free</Link>
                </Button>
              </div>
            </div>
          </div>
        </section>

        <div className="container mx-auto px-4 py-12 space-y-16">
          {/* Recently Read Section - Only show if user is authenticated and has reading history */}
          {isAuthenticated && recentlyRead.length > 0 && (
            <section>
              <div className="flex items-center justify-between mb-8">
                <div className="flex items-center space-x-2">
                  <Clock className="h-6 w-6 text-primary" />
                  <h2 className="text-3xl font-bold">Continue Reading</h2>
                </div>
                <Button asChild variant="ghost">
                  <Link href="/profile">
                    View All
                    <ArrowRight className="ml-2 h-4 w-4" />
                  </Link>
                </Button>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {recentlyRead.map((novel) => (
                  <NovelCard key={novel.id} novel={novel} />
                ))}
              </div>
            </section>
          )}

          {/* Hot Novels Section */}
          <section>
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center space-x-2">
                <TrendingUp className="h-6 w-6 text-primary" />
                <h2 className="text-3xl font-bold">Hot Novels</h2>
              </div>
              <Button asChild variant="ghost">
                <Link href="/novels/hot">
                  View All
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
            </div>
            {loading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {Array.from({ length: 6 }).map((_, i) => (
                  <Card key={i} className="animate-pulse">
                    <CardContent className="p-4">
                      <div className="aspect-[3/4] bg-muted rounded-lg mb-4" />
                      <div className="space-y-2">
                        <div className="h-4 bg-muted rounded" />
                        <div className="h-3 bg-muted rounded w-2/3" />
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {hotNovels.map((novel) => (
                  <NovelCard key={novel.id} novel={novel} />
                ))}
              </div>
            )}
          </section>

          {/* Top Rated Section */}
          <section>
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center space-x-2">
                <Star className="h-6 w-6 text-primary" />
                <h2 className="text-3xl font-bold">Top Rated</h2>
              </div>
              <Button asChild variant="ghost">
                <Link href="/novels/top">
                  View All
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
            </div>
            {loading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {Array.from({ length: 6 }).map((_, i) => (
                  <Card key={i} className="animate-pulse">
                    <CardContent className="p-4">
                      <div className="aspect-[3/4] bg-muted rounded-lg mb-4" />
                      <div className="space-y-2">
                        <div className="h-4 bg-muted rounded" />
                        <div className="h-3 bg-muted rounded w-2/3" />
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {topRatedNovels.map((novel) => (
                  <NovelCard key={novel.id} novel={novel} />
                ))}
              </div>
            )}
          </section>

          {/* Latest Updates Section */}
          <section>
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center space-x-2">
                <BookOpen className="h-6 w-6 text-primary" />
                <h2 className="text-3xl font-bold">Latest Updates</h2>
              </div>
              <Button asChild variant="ghost">
                <Link href="/novels/latest">
                  View All
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
            </div>
            {loading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {Array.from({ length: 6 }).map((_, i) => (
                  <Card key={i} className="animate-pulse">
                    <CardContent className="p-4">
                      <div className="aspect-[3/4] bg-muted rounded-lg mb-4" />
                      <div className="space-y-2">
                        <div className="h-4 bg-muted rounded" />
                        <div className="h-3 bg-muted rounded w-2/3" />
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {latestNovels.map((novel) => (
                  <NovelCard key={novel.id} novel={novel} />
                ))}
              </div>
            )}
          </section>

          {/* Call to Action Section */}
          <section className="text-center py-16">
            <div className="max-w-2xl mx-auto">
              <h2 className="text-3xl font-bold mb-4">Ready to Start Your Journey?</h2>
              <p className="text-xl text-muted-foreground mb-8">
                Join thousands of readers discovering their next favorite story every day.
              </p>
              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <Button asChild size="lg">
                  <Link href="/register">Sign Up Free</Link>
                </Button>
                <Button asChild variant="outline" size="lg">
                  <Link href="/novels">Browse Novels</Link>
                </Button>
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>
  )
}
