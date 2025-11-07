"use client";

import type React from "react";

import { useCallback, useEffect, useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Search, Grid, List, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Header } from "@/components/layout/header";
import { NovelCard } from "@/components/novel/novel-card";
import { api, Tag, type Category, type Genre, type Novel } from "@/services/api";
import { Pagination } from "@/components/ui/pagination";

type FilterKey = "keyword" | "title" | "author" | "category" | "genre" | "tag";
type SearchFilters = Partial<Record<FilterKey, string>>;

const filterLabels: Record<FilterKey, string> = {
  keyword: "Keyword",
  title: "Title",
  author: "Author",
  category: "Category",
  genre: "Genre",
  tag: "Tag",
};

const filterKeys: FilterKey[] = [
  "keyword",
  "title",
  "author",
  "category",
  "genre",
  "tag",
];

const cleanFilters = (filters: SearchFilters): SearchFilters => {
  const cleaned: SearchFilters = {};
  filterKeys.forEach((key) => {
    const value = filters[key];
    if (value && value.trim() && value !== "all") {
      cleaned[key] = value.trim();
    }
  });
  return cleaned;
};

export default function SearchPage() {
  const [novels, setNovels] = useState<Novel[]>([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalResults, setTotalResults] = useState(0);
  const [sortBy, setSortBy] = useState("updatedAt");
  const [sortDir, setSortDir] = useState("desc");
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");
  const [searchQuery, setSearchQuery] = useState("");
  const [titleFilter, setTitleFilter] = useState("");
  const [authorFilter, setAuthorFilter] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [selectedGenre, setSelectedGenre] = useState("all");

  const [selectedTag, setSelectedTag] = useState("all");
  const [categories, setCategories] = useState<Category[]>([]);
  const [genres, setGenres] = useState<Genre[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [lastSubmittedFilters, setLastSubmittedFilters] =
    useState<SearchFilters | null>(null);

  const searchParams = useSearchParams();
  const router = useRouter();

  const updateURL = useCallback(
    (
      filters: SearchFilters,
      page: number,
      sort: { sortBy: string; sortDir: string }
    ) => {
      const params = new URLSearchParams();

      Object.entries(filters).forEach(([key, value]) => {
        if (value && value.trim() && value !== "all") {
          params.append(key, value);
        }
      });

      if (page > 0) params.append("page", page.toString());
      params.append("sortBy", sort.sortBy);
      params.append("sortDir", sort.sortDir);

      router.push(`/search?${params.toString()}`, { scroll: false });
    },
    [router]
  );

  const performSearch = useCallback(
    async (
      filters: SearchFilters,
      page = 0,
      sortOverride?: { sortBy: string; sortDir: string }
    ) => {
      const cleaned = cleanFilters(filters);

      if (Object.keys(cleaned).length === 0) {
        setNovels([]);
        setTotalPages(0);
        setTotalResults(0);
        setCurrentPage(0);
        return;
      }

      setLoading(true);
      try {
        const response = await api.searchNovels({
          ...cleaned,
          page,
          size: 20,
          sortBy: sortOverride?.sortBy ?? sortBy,
          sortDir: sortOverride?.sortDir ?? sortDir,
        });

        if (response.success) {
          setNovels(response.data.content);
          setTotalPages(response.data.totalPages);
          setTotalResults(response.data.totalElements);
          setCurrentPage(response.data.pageNumber ?? page);
        } else {
          setNovels([]);
          setTotalPages(0);
          setTotalResults(0);
          setCurrentPage(page);
        }
      } catch (error) {
        console.error("Failed to search novels:", error);
        setNovels([]);
        setTotalPages(0);
        setTotalResults(0);
        setCurrentPage(page);
      } finally {
        setLoading(false);
      }
    },
    [sortBy, sortDir]
  );

  useEffect(() => {
    const fetchTaxonomies = async () => {
      try {
        const [categoryResponse, genreResponse, tagResponse] =
          await Promise.all([
            api.getCategories(),
            api.getGenres(),
            api.getTags(),
          ]);

        if (categoryResponse.success) {
          setCategories(categoryResponse.data);
        }
        if (genreResponse.success) {
          setGenres(genreResponse.data);
        }
        if (tagResponse.success) {
          setTags(tagResponse.data);
        }
      } catch (error) {
        console.error("Failed to load filter data:", error);
      }
    };

    void fetchTaxonomies();
  }, []);

  useEffect(() => {
    const keyword = searchParams.get("keyword") || "";
    const title = searchParams.get("title") || "";
    const author = searchParams.get("author") || "";
    const category = searchParams.get("category") || "all";
    const genre = searchParams.get("genre") || "all";
    const tag = searchParams.get("tag") || "all";
    const page = parseInt(searchParams.get("page") || "0");
    const sort = {
      sortBy: searchParams.get("sortBy") || "updatedAt",
      sortDir: searchParams.get("sortDir") || "desc",
    };

    setSearchQuery(keyword);
    setTitleFilter(title);
    setAuthorFilter(author);
    setSelectedCategory(category);
    setSelectedGenre(genre);
    setSelectedTag(tag);
    setCurrentPage(page);
    setSortBy(sort.sortBy);
    setSortDir(sort.sortDir);

    const initialFilters = cleanFilters({
      keyword,
      title,
      author,
      category,
      genre,
      tag,
    });

    if (Object.keys(initialFilters).length > 0) {
      setLastSubmittedFilters(initialFilters);
      void performSearch(initialFilters, page, sort);
    }
  }, [searchParams, performSearch]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const filters: SearchFilters = {
      keyword: searchQuery,
      title: titleFilter,
      author: authorFilter,
      category: selectedCategory,
      genre: selectedGenre,
      tag: selectedTag,
    };
    const cleaned = cleanFilters(filters);
    if (Object.keys(cleaned).length === 0) {
      setLastSubmittedFilters(null);
      setNovels([]);
      setTotalPages(0);
      setTotalResults(0);
      setCurrentPage(0);
      updateURL({}, 0, { sortBy, sortDir });
      return;
    }
    setLastSubmittedFilters(cleaned);
    setCurrentPage(0);
    updateURL(cleaned, 0, { sortBy, sortDir });
    void performSearch(cleaned, 0);
  };

  const handleSortChange = (value: string) => {
    const [field, direction] = value.split("-");
    setSortBy(field);
    setSortDir(direction);
    setCurrentPage(0);
    if (lastSubmittedFilters) {
      updateURL(lastSubmittedFilters, 0, { sortBy: field, sortDir: direction });
      void performSearch(lastSubmittedFilters, 0, {
        sortBy: field,
        sortDir: direction,
      });
    }
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    if (lastSubmittedFilters) {
      updateURL(lastSubmittedFilters, page, { sortBy, sortDir });
      void performSearch(lastSubmittedFilters, page);
    }
  };

  const activeFilters = useMemo(() => {
    if (!lastSubmittedFilters) return [];
    return (Object.entries(lastSubmittedFilters) as [FilterKey, string][]).map(
      ([key, value]) => ({
        key,
        label: `${filterLabels[key]}: ${value}`,
      })
    );
  }, [lastSubmittedFilters]);

  const hasSubmittedFilters = activeFilters.length > 0;

  const removeFilter = (key: FilterKey) => {
    if (!lastSubmittedFilters) return;

    const updatedFilters: SearchFilters = { ...lastSubmittedFilters };
    delete updatedFilters[key];

    if (key === "keyword") {
      setSearchQuery("");
    } else if (key === "title") {
      setTitleFilter("");
    } else if (key === "author") {
      setAuthorFilter("");
    } else if (key === "category") {
      setSelectedCategory("all");
    } else if (key === "genre") {
      setSelectedGenre("all");
    } else if (key === "tag") {
      setSelectedTag("all");
    }

    const cleaned = cleanFilters(updatedFilters);
    if (Object.keys(cleaned).length === 0) {
      setLastSubmittedFilters(null);
      setNovels([]);
      setTotalPages(0);
      setTotalResults(0);
      setCurrentPage(0);
      return;
    }

    setLastSubmittedFilters(cleaned);
    setCurrentPage(0);
    void performSearch(cleaned, 0);
  };

  const clearAllFilters = () => {
    setSearchQuery("");
    setTitleFilter("");
    setAuthorFilter("");
    setSelectedCategory("");
    setSelectedGenre("");
    setSelectedTag("");
    setLastSubmittedFilters(null);
    setNovels([]);
    setTotalPages(0);
    setTotalResults(0);
    setCurrentPage(0);
  };

    return (
        <div className="min-h-screen bg-background">
                  <main className="container py-8">
                <div className="flex flex-col space-y-6">
                    {/* Search Header */}
                    <div className="flex flex-col space-y-4">
                        <div className="flex items-center space-x-3">
                            <Search className="h-8 w-8 text-primary" />
                            <div>
                                <h1 className="text-3xl font-bold">Search Novels</h1>
                                <p className="text-muted-foreground">
                                    {searchQuery ? `Results for "${searchQuery}"` : "Find your next favorite story"}
                                </p>
                            </div>
                        </div>

            {/* Search Form */}
            <form onSubmit={handleSearch} className="space-y-3">
              <div className="flex flex-col gap-2 sm:flex-row">
                <div className="relative flex-1">
                  <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="Search keywords across the library..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="pl-10"
                  />
                </div>
                <Button type="submit" disabled={loading}>
                  {loading ? "Searching..." : "Search"}
                </Button>
              </div>

              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                <Input
                  placeholder="Filter by title"
                  value={titleFilter}
                  onChange={(e) => setTitleFilter(e.target.value)}
                />
                <Input
                  placeholder="Filter by author"
                  value={authorFilter}
                  onChange={(e) => setAuthorFilter(e.target.value)}
                />
                <Select
                  value={selectedCategory || "all"}
                  onValueChange={setSelectedCategory}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="All categories" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All categories</SelectItem>
                    {categories.map((category) => (
                      <SelectItem key={category.id} value={category.name}>
                        {category.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Select
                  value={selectedGenre || "all"}
                  onValueChange={setSelectedGenre}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="All genres" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All genres</SelectItem>
                    {genres.map((genre) => (
                      <SelectItem key={genre.id} value={genre.name}>
                        {genre.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Select
                  value={selectedTag || "all"}
                  onValueChange={setSelectedTag}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="All tags" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All tags</SelectItem>
                    {tags.map((tag) => (
                      <SelectItem key={tag.id} value={tag.name}>
                        {tag.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </form>
          </div>

          {/* Filters and Controls */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div className="text-sm text-muted-foreground">
              {hasSubmittedFilters
                ? `Showing ${novels.length} of ${totalResults} results`
                : "Use the filters above to start your search."}
            </div>

            <div className="flex items-center space-x-2">
              <Select
                value={`${sortBy}-${sortDir}`}
                onValueChange={handleSortChange}
              >
                <SelectTrigger className="w-48">
                  <SelectValue placeholder="Sort by" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="updatedAt-desc">Latest Updated</SelectItem>
                  <SelectItem value="createdAt-desc">Newest</SelectItem>
                  <SelectItem value="rating-desc">Highest Rated</SelectItem>
                  <SelectItem value="views-desc">Most Popular</SelectItem>
                  <SelectItem value="title-asc">Title A-Z</SelectItem>
                  <SelectItem value="title-desc">Title Z-A</SelectItem>
                </SelectContent>
              </Select>

              <div className="flex border rounded-md">
                <Button
                  variant={viewMode === "grid" ? "default" : "ghost"}
                  size="icon"
                  onClick={() => setViewMode("grid")}
                >
                  <Grid className="h-4 w-4" />
                </Button>
                <Button
                  variant={viewMode === "list" ? "default" : "ghost"}
                  size="icon"
                  onClick={() => setViewMode("list")}
                >
                  <List className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>

          {/* Active Filters */}
          {hasSubmittedFilters && (
            <div className="flex flex-wrap items-center gap-2">
              {activeFilters.map((filter) => (
                <Badge
                  key={filter.key}
                  variant="secondary"
                  className="flex items-center gap-1"
                >
                  {filter.label}
                  <X
                    className="h-3 w-3 cursor-pointer"
                    onClick={() => removeFilter(filter.key)}
                  />
                </Badge>
              ))}
              <Button
                variant="ghost"
                size="sm"
                onClick={clearAllFilters}
                className="text-muted-foreground"
              >
                Clear All
              </Button>
            </div>
          )}

          {/* Results */}
          {loading ? (
            <div
              className={`grid gap-6 ${
                viewMode === "grid"
                  ? "grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5"
                  : "grid-cols-1"
              }`}
            >
              {Array.from({ length: 20 }).map((_, i) => (
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
          ) : !hasSubmittedFilters ? (
            <div className="text-center py-12">
              <Search className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground">
                Enter search details to find novels
              </p>
            </div>
          ) : novels.length === 0 ? (
            <div className="text-center py-12">
              <Search className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground">
                No novels matched the selected filters
              </p>
              <p className="text-sm text-muted-foreground mt-2">
                Try adjusting or clearing your filters
              </p>
            </div>
          ) : (
            <>
              <div className="flex items-center justify-between">
                <p className="text-sm text-muted-foreground">
                  Showing {novels.length} of {totalResults} results
                </p>
              </div>

              <div
                className={`grid gap-6 ${
                  viewMode === "grid"
                    ? "grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5"
                    : "grid-cols-1"
                }`}
              >
                {novels.map((novel) => (
                  <NovelCard key={novel.id} novel={novel} />
                ))}
              </div>
            </>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={handlePageChange}
              showPageNumbers={true}
              className="mt-8"
            />
          )}
        </div>
      </main>
    </div>
  );
}
