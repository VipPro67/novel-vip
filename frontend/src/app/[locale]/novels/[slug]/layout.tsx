import { Metadata } from "next"
import { api } from "@/services/api"

type Props = {
    params: Promise<{ slug: string; locale: string }>
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
    const { slug } = await params

    // Default metadata
    const defaultTitle = "NovelVip – Read novels online for free"
    const defaultDesc = "Read your favorite novels online for free on NovelVip."

    if (!slug) {
        return {
            title: defaultTitle,
            description: defaultDesc,
        }
    }

    try {
        const response = await api.getNovelBySlug(slug)

        if (response.success && response.data) {
            const novel = response.data
            const title = `${novel.title} – Read online | NovelVip`
            // Truncate description to ~160 chars and strip HTML/newlines if necessary
            const description = novel.description
                ? novel.description.replace(/<[^>]*>/g, '').substring(0, 160).trim() + (novel.description.length > 160 ? "..." : "")
                : defaultDesc

            return {
                title,
                description,
                openGraph: {
                    title,
                    description,
                    type: "book",
                    images: novel.imageUrl ? [novel.imageUrl] : [],
                },
                twitter: {
                    card: "summary_large_image",
                    title,
                    description,
                    images: novel.imageUrl ? [novel.imageUrl] : [],
                },
            }
        }
    } catch (error) {
        console.error("Failed to generate metadata for novel:", error)
    }

    return {
        title: defaultTitle,
        description: defaultDesc,
    }
}

export default function NovelLayout({
    children,
}: {
    children: React.ReactNode
}) {
    return <>{children}</>
}
