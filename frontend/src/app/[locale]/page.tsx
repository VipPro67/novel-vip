import { getTranslations } from "next-intl/server" // Lưu ý: dùng /server
import { ArrowRight, TrendingUp, Star, BookOpen } from "lucide-react"
import { Link } from "@/navigation"
import { Button } from "@/components/ui/button"
import { NovelCard } from "@/components/novel/novel-card"
import { RecentlyReadSection } from "@/components/home/recently-read-section"
import { api } from "@/services/api" // Giả sử api này hỗ trợ gọi từ server (fetch)

// Helper để fetch dữ liệu trên server
async function getHomeData() {
  // Promise.all để chạy song song 3 request, tiết kiệm thời gian
  const [hotRes, topRes, latestRes] = await Promise.all([
    api.getHotNovels({ size: 6 }),
    api.getTopRatedNovels({ size: 6 }),
    api.getLatestNovels({ size: 6 }),
  ])

  return {
    hotNovels: hotRes.success ? hotRes.data.content : [],
    topRatedNovels: topRes.success ? topRes.data.content : [],
    latestNovels: latestRes.success ? latestRes.data.content : [],
  }
}

export default async function HomePage() {
  // Fetch dữ liệu ngay trên server
  const t = await getTranslations("Home")
  const { hotNovels, topRatedNovels, latestNovels } = await getHomeData()

  return (
    <div className="min-h-screen bg-background">
      <main>
        <div className="container mx-auto px-4 py-6 space-y-6">
          <RecentlyReadSection />
          <section>
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center space-x-2">
                <BookOpen className="h-6 w-6 text-primary" />
                <h2 className="text-3xl font-bold">{t("latest")}</h2>
              </div>
              <Button asChild variant="ghost">
                <Link href="/novels/latest">{t("viewAll")}<ArrowRight className="ml-2 h-4 w-4" /></Link>
              </Button>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-6">
              {latestNovels.map((novel: any) => (
                <NovelCard key={novel.id} novel={novel} />
              ))}
            </div>
          </section>
          <section>
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center space-x-2">
                <TrendingUp className="h-6 w-6 text-primary" />
                <h2 className="text-3xl font-bold">{t("hot")}</h2>
              </div>
              <Button asChild variant="ghost">
                <Link href="/novels/hot">{t("viewAll")}<ArrowRight className="ml-2 h-4 w-4" /></Link>
              </Button>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-6">
              {hotNovels.map((novel: any) => (
                <NovelCard key={novel.id} novel={novel} />
              ))}
            </div>
          </section>
          <section>
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center space-x-2">
                <Star className="h-6 w-6 text-primary" />
                <h2 className="text-3xl font-bold">{t("top")}</h2>
              </div>
              <Button asChild variant="ghost">
                <Link href="/novels/top">{t("viewAll")}<ArrowRight className="ml-2 h-4 w-4" /></Link>
              </Button>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-6">
              {topRatedNovels.map((novel: any) => (
                <NovelCard key={novel.id} novel={novel} />
              ))}
            </div>
          </section>
        </div>
      </main>
    </div>
  )
}