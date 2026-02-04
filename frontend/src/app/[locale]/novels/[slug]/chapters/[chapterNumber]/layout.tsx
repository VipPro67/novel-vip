import { Metadata } from "next"
import { api } from "@/services/api"

type Props = {
    params: Promise<{ slug: string; chapterNumber: string; locale: string }>
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
    const { slug, chapterNumber } = await params
    const chapterNum = parseInt(chapterNumber)

    // Default metadata
    const defaultTitle = "Read Novel Chapter – NovelVip"
    const defaultDesc = "Read novel chapters online for free on NovelVip."

    if (!slug || isNaN(chapterNum)) {
        return {
            title: defaultTitle,
            description: defaultDesc,
        }
    }

    try {
        // Parallel fetch for better performance
        const [novelRes, chapterRes] = await Promise.all([
            api.getNovelBySlug(slug),
            api.getChapterByNumber2(slug, chapterNum)
        ])

        if (novelRes.success && novelRes.data && chapterRes.success && chapterRes.data) {
            const novel = novelRes.data
            const chapter = chapterRes.data

            const title = `${novel.title} – Chapter ${chapter.chapterNumber}: ${chapter.title}`
            const description = `Read ${novel.title} Chapter ${chapter.chapterNumber}: ${chapter.title} online for free. ${novel.description ? novel.description.substring(0, 50) + "..." : ""}`.trim()

            return {
                title,
                description,
                openGraph: {
                    title,
                    description,
                    type: "article",
                    images: novel.imageUrl ? [novel.imageUrl] : [],
                },
                twitter: {
                    card: "summary_large_image",
                    title,
                    description,
                    images: novel.imageUrl ? [novel.imageUrl] : [],
                },
            }
        } else if (novelRes.success && novelRes.data) {
            // Fallback if chapter fetch fails but novel succeeds
            const novel = novelRes.data
            return {
                title: `${novel.title} – Chapter ${chapterNum}`,
                description: `Read ${novel.title} Chapter ${chapterNum} online for free.`,
            }
        }

    } catch (error) {
        console.error("Failed to generate metadata for chapter:", error)
    }

    return {
        title: defaultTitle,
        description: defaultDesc,
    }
}

export default function ChapterLayout({
    children,
}: {
    children: React.ReactNode
}) {
    return <>{children}</>
}
