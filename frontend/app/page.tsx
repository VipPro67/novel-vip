"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { ArrowRight, TrendingUp, Star, Clock } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Header } from "@/components/layout/header"
import { NovelCard } from "@/components/novel/novel-card"
import { api, type Novel } from "@/lib/api"

export default function HomePage() {
  const [hotNovels, setHotNovels] = useState<Novel[]>([])
  const [topRatedNovels, setTopRatedNovels] = useState<Novel[]>([])
  const [latestNovels, setLatestNovels] = useState<Novel[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchData = async () => {
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
      } catch (error) {
        console.error("Failed to fetch novels:", error)
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [])

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <main>
        {/* Hero Section */}
        <section className="bg-gradient-to-r from-primary/10 to-secondary/10 py-20">
          <div className="container text-center">
            <h1 className="text-4xl md:text-6xl font-bold mb-6">Discover Amazing Stories</h1>
            <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto">
              Dive into a world of captivating novels, from fantasy epics to romantic tales. Your next favorite story
              awaits.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button size="lg" asChild>
                <Link href="/novels">
                  Browse Novels
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
              <Button size="lg" variant="outline" asChild>
                <Link href="/register">Start Reading Free</Link>
              </Button>
            </div>
          </div>
        </section>

        {/* Hot Novels Section */}
        <section className="py-16">
          <div className="container">
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center space-x-2">
                <TrendingUp className="h-6 w-6 text-primary" />
                <h2 className="text-3xl font-bold">Hot Novels</h2>
              </div>
              <Button variant="outline" asChild>
                <Link href="/novels/hot">
                  View All
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
            </div>

            {loading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
                {Array.from({ length: 6 }).map((_, i) => (
                  <Card key={i} className="animate-pulse">
                    <div className="aspect-[3/4] bg-muted" />
                    <CardContent className="p-4">
                      <div className="h-4 bg-muted rounded mb-2" />
                      <div className="h-3 bg-muted rounded mb-2" />
                      <div className="h-3 bg-muted rounded" />
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
                {hotNovels.map((novel) => (
                  <NovelCard key={novel.id} novel={novel} />
                ))}
              </div>
            )}
          </div>
        </section>

        {/* Top Rated Section */}
        <section className="py-16 bg-muted/50">
          <div className="container">
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center space-x-2">
                <Star className="h-6 w-6 text-primary" />
                <h2 className="text-3xl font-bold">Top Rated</h2>
              </div>
              <Button variant="outline" asChild>
                <Link href="/novels/top-rated">
                  View All
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
            </div>

            {loading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
                {Array.from({ length: 6 }).map((_, i) => (
                  <Card key={i} className="animate-pulse">
                    <div className="aspect-[3/4] bg-muted" />
                    <CardContent className="p-4">
                      <div className="h-4 bg-muted rounded mb-2" />
                      <div className="h-3 bg-muted rounded mb-2" />
                      <div className="h-3 bg-muted rounded" />
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
                {topRatedNovels.map((novel) => (
                  <NovelCard key={novel.id} novel={novel} />
                ))}
              </div>
            )}
          </div>
        </section>

        {/* Latest Updates Section */}
        <section className="py-16">
          <div className="container">
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center space-x-2">
                <Clock className="h-6 w-6 text-primary" />
                <h2 className="text-3xl font-bold">Latest Updates</h2>
              </div>
              <Button variant="outline" asChild>
                <Link href="/novels/latest">
                  View All
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
            </div>

            {loading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
                {Array.from({ length: 6 }).map((_, i) => (
                  <Card key={i} className="animate-pulse">
                    <div className="aspect-[3/4] bg-muted" />
                    <CardContent className="p-4">
                      <div className="h-4 bg-muted rounded mb-2" />
                      <div className="h-3 bg-muted rounded mb-2" />
                      <div className="h-3 bg-muted rounded" />
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
                {latestNovels.map((novel) => (
                  <NovelCard key={novel.id} novel={novel} />
                ))}
              </div>
            )}
          </div>
        </section>

        {/* Features Section */}
        <section className="py-16 bg-muted/50">
          <div className="container">
            <h2 className="text-3xl font-bold text-center mb-12">Why Choose Novel VIP Pro?</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <TrendingUp className="h-5 w-5 text-primary" />
                    <span>Vast Library</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Access thousands of novels across all genres, from bestsellers to hidden gems.
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <Star className="h-5 w-5 text-primary" />
                    <span>Personalized Experience</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Get recommendations based on your reading history and preferences.
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <Clock className="h-5 w-5 text-primary" />
                    <span>Regular Updates</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    Never miss a chapter with our notification system and reading history.
                  </p>
                </CardContent>
              </Card>
            </div>
          </div>
        </section>
      </main>
    </div>
  )
}
