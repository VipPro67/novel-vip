"use client";

import { useEffect, useState } from "react";
import { Clock, Loader2, Search } from "lucide-react";
import { AuthGuard } from "@/components/auth/auth-guard";
import { Header } from "@/components/layout/header";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Pagination } from "@/components/ui/pagination";
import { useToast } from "@/hooks/use-toast";
import { usePagination } from "@/hooks/use-pagination";
import { api } from "@/services/api";
import { Link } from "@/navigation";
import { Badge } from "@/components/ui/badge";
import { ReadingHistory } from "@/models";
import { useTranslations } from "next-intl";

export default function HistoryPage() {
  return (
    <AuthGuard>
      <HistoryContent />
    </AuthGuard>
  );
}

function HistoryContent() {
  const [history, setHistory] = useState<ReadingHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const { toast } = useToast();
  const t = useTranslations("History");
  const {
    currentPage,
    totalPages,
    handlePageChange,
    updateTotalPages,
    getPaginationParams,
  } = usePagination({
    initialPage: 0,
    initialSize: 20,
    initialSortBy: "lastReadAt",
    initialSortDir: "desc",
  });

  useEffect(() => {
    fetchReadingHistory();
  }, [currentPage]);

  const fetchReadingHistory = async () => {
    setLoading(true);
    try {
      const paginationParams = getPaginationParams();
      const response = await api.getReadingHistory(paginationParams);

      if (response.success && response.data?.content) {
        setHistory(response.data.content);
        updateTotalPages(response.data.totalPages || 0);
      }
    } catch (error) {
      console.error("Failed to fetch reading history:", error);
      toast({
        title: "Error",
        description: "Failed to load your reading history",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 60) return `${diffMins} ${t('minutes')} ${t('timeAgo')}`;
    if (diffHours < 24) return `${diffHours} ${t('hours')} ${t('timeAgo')}`;
    if (diffDays < 7) return `${diffDays} ${t('days')} ${t('timeAgo')}`;
    return date.toLocaleDateString();
  };

  return (
        <div className="min-h-screen bg-background">
      <main className="container py-8">
        <div className="max-w-6xl mx-auto space-y-8">
          <div>
            <h1 className="text-3xl font-bold">{t('title')}</h1>
            <p className="text-muted-foreground">
             {t('recentlyRead.description')}
            </p>
          </div>

          <div className="flex items-center gap-4">
            <div className="relative flex-1 max-w-md">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder={t('search')}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>

          {loading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : history.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Clock className="h-12 w-12 text-muted-foreground mb-4" />
                <h3 className="text-lg font-semibold mb-2">
                 {t('noHistoryMessage')}
                </h3>
                <p className="text-muted-foreground text-center">
                  {searchQuery
                    ? "No history matches your search"
                    : "Start reading novels to see your history here"}
                </p>
              </CardContent>
            </Card>
          ) : (
            <>
              <div className="space-y-4">
                {history.map((item) => (
                  <Card
                    key={item.id}
                    className="hover:shadow-md transition-shadow"
                  >
                    <CardContent className="p-6">
                      <div className="flex gap-4">
                        <Link
                          href={`/novels/${item.novel?.slug}`}
                          className="flex-shrink-0"
                        >
                          <img
                            src={
                              item.novel?.imageUrl ||
                              "/placeholder.svg?height=120&width=80"
                            }
                            alt={item.novel?.title || "Novel cover"}
                            className="w-20 h-28 object-cover rounded"
                          />
                        </Link>
                        <div className="flex-1 min-w-0">
                          <Link href={`/novels/${item.novel?.slug}`}>
                            <h3 className="font-semibold text-lg hover:text-primary transition-colors line-clamp-1">
                              {item.novel?.title}
                            </h3>
                          </Link>
                          <p className="text-sm text-muted-foreground mb-2">
                            {item.novel?.author}
                          </p>
                          <div className="flex items-center gap-2 mb-2">
                            <Badge variant="outline">
                              Chapter {item.lastReadChapterIndex}
                            </Badge>
                            <span className="text-sm text-muted-foreground">
                              {Math.round(item.lastReadChapterIndex/item.novel.totalChapters)*100}% {t("completed")}
                            </span>
                          </div>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground">
                            <span className="flex items-center gap-1">
                              <Clock className="h-3 w-3" />
                              {formatDate(item.lastReadAt)}
                            </span>
                          </div>
                        </div>
                        <div className="flex-shrink-0">
                          <Link
                            href={`/novels/${item.novel?.slug}/chapters/${item.lastReadChapterIndex}`}
                          >
                            <button className="px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-colors">
                               {t("continue")}
                            </button>
                          </Link>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>

              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
                showPageNumbers={true}
              />
            </>
          )}
        </div>
      </main>
    </div>
  );
}
