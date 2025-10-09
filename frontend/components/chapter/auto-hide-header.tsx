"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { ChevronLeft, ChevronRight, Bookmark, Settings } from "lucide-react"
import { Button } from "@/components/ui/button"
import Link from "next/link"

interface AutoHideHeaderProps {
  novelSlug: string
  novelTitle: string
  chapterNumber: number
  chapterId: string
  chapterTitle: string
  onPrevChapter: () => void
  onNextChapter: () => void
  canGoPrev: boolean
  onOpenSettings?: () => void
  onToggleBookmark?: () => void
  reportTrigger?: React.ReactNode
}

export default function AutoHideHeader({
  novelSlug,
  novelTitle,
  chapterNumber,
  chapterId,
  chapterTitle,
  onPrevChapter,
  onNextChapter,
  canGoPrev,
  onOpenSettings,
  onToggleBookmark,
  reportTrigger,
}: AutoHideHeaderProps) {
  const [isVisible, setIsVisible] = useState(true)
  const [lastScrollY, setLastScrollY] = useState(0)

  useEffect(() => {
    const handleScroll = () => {
      const currentScrollY = window.scrollY

      // Show header when scrolling up or at the top
      if (currentScrollY < lastScrollY || currentScrollY < 50) {
        setIsVisible(true)
      }
      // Hide header when scrolling down (and not at the top)
      else if (currentScrollY > lastScrollY && currentScrollY > 100) {
        setIsVisible(false)
      }

      setLastScrollY(currentScrollY)
    }

    window.addEventListener("scroll", handleScroll, { passive: true })

    return () => {
      window.removeEventListener("scroll", handleScroll)
    }
  }, [lastScrollY])

  return (
    <div
      className={`fixed top-0 left-0 right-0 z-50 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 border-b transition-transform duration-300 ${
        isVisible ? "translate-y-0" : "-translate-y-full"
      }`}
    >
      <div className="container mx-auto px-4 py-3">
        <div className="flex items-center justify-between gap-4">
          {/* Left: Novel title and chapter navigation */}
          <div className="flex items-center gap-2 min-w-0 flex-1">
            <Link href={`/novels/${novelSlug}`}>
              <Button variant="ghost" size="sm" className="text-muted-foreground hover:text-foreground">
                {novelTitle}
              </Button>
            </Link>
            <span className="text-muted-foreground">•</span>
            <span className="font-medium text-sm">Chapter {chapterNumber}</span>
          </div>

          {/* Center: Chapter navigation buttons */}
          <div className="flex items-center gap-1">
            <Button variant="ghost" size="sm" onClick={onPrevChapter} disabled={!canGoPrev}>
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="sm" onClick={onNextChapter}>
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>

          {/* Right: Action buttons */}
          <div className="flex items-center gap-1">
            {onToggleBookmark && (
              <Button variant="ghost" size="sm" onClick={onToggleBookmark}>
                <Bookmark className="h-4 w-4" />
              </Button>
            )}
            {reportTrigger}
            {onOpenSettings && (
              <Button variant="ghost" size="sm" onClick={onOpenSettings}>
                <Settings className="h-4 w-4" />
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
