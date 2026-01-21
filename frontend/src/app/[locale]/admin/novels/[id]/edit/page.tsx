"use client";

import type React from "react";

import { useState, useEffect } from "react";
import { useParams } from "next/navigation";
import { useRouter } from "@/navigation";
import { Header } from "@/components/layout/header";
import { AuthGuard } from "@/components/auth/auth-guard";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { EpubUpload } from "@/components/ui/epub-upload";
import { ArrowLeft, Upload, X, Loader2, FileText } from "lucide-react";
import { Link } from "@/navigation";
import { useToast } from "@/hooks/use-toast";
import { api } from "@/services/api";
import Image from "next/image";
import { MultiSelectCombobox } from "@/components/ui/multi-select-combobox";
import { Novel } from "@/models";
import type { NovelSource, CreateNovelSourceDTO, SourcePlatform } from "@/services/api/novel-sources";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Badge } from "@/components/ui/badge";

interface NovelFormData {
  title: string;
  slug: string;
  description: string;
  author: string;
  status: string;
  categories: string[];
  genres: string[];
  tags: string[];
}

export default function EditNovelPage() {
  const params = useParams();
  const router = useRouter();
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [novel, setNovel] = useState<Novel | null>(null);
  const [coverImage, setCoverImage] = useState<File | null>(null);
  const [coverPreview, setCoverPreview] = useState<string>("");
  const [newCategory, setNewCategory] = useState("");
  const [newGenre, setNewGenre] = useState("");
  const [newTag, setNewTag] = useState("");
  const [categoryOptions, setCategoryOptions] = useState<string[]>([]);
  const [genreOptions, setGenreOptions] = useState<string[]>([]);
  const [tagOptions, setTagOptions] = useState<string[]>([]);
  const [epubFile, setEpubFile] = useState<File | null>(null);
  const [epubPreview, setEpubPreview] = useState<string>("");
  const [uploading, setUploading] = useState(false);

  // Novel sources state
  const [novelSources, setNovelSources] = useState<NovelSource[]>([]);
  const [loadingSources, setLoadingSources] = useState(false);
  const [showAddSource, setShowAddSource] = useState(false);
  const [newSource, setNewSource] = useState<CreateNovelSourceDTO>({
    novelId: "",
    sourceUrl: "",
    sourceId: "",
    sourcePlatform: "SHUBA69",
    syncIntervalMinutes: 60,
  });
  const [syncingSource, setSyncingSource] = useState<string | null>(null);
  const [deletingSource, setDeletingSource] = useState<string | null>(null);
  
  const [formData, setFormData] = useState<NovelFormData>({
    title: "",
    slug: "",
    description: "",
    author: "",
    status: "ONGOING",
    categories: [],
    genres: [],
    tags: [],
  });

  useEffect(() => {
    fetchNovel();
    fetchNovelSources();
  }, [params.id]);

  // Load selectable options for categories, genres, and tags
  useEffect(() => {
    const fetchNames = async (path: string): Promise<string[]> => {
      try {
        const res = await api.request(path, { method: "GET" });
        const list = Array.isArray(res?.data) ? res.data : [];
        return list.map((x: any) => x?.name).filter(Boolean);
      } catch {
        return [];
      }
    };
    (async () => {
      const [genres, categories, tags] = await Promise.all([
        fetchNames("/api/novels/genres"),
        fetchNames("/api/novels/categories"),
        fetchNames("/api/novels/tags"),
      ]);
      setGenreOptions(genres);
      setCategoryOptions(categories);
      setTagOptions(tags);
    })();
  }, []);

  const fetchNovel = async () => {
    try {
      console.log("Fetching novel:", params.id);
      const response = await api.getNovelById(params.id);
      if (response.success) {
        const novelData = response.data;
        setNovel(novelData);
        setFormData({
          title: novelData.title,
          slug: novelData.slug,
          description: novelData.description,
          author: novelData.author,
          status: novelData.status,
          categories: novelData.categories?.map((c) => c.name) || [],
          genres: novelData.genres?.map((g) => g.name) || [],
          tags: novelData.tags?.map((t) => t.name) || [],
        });
        if (novelData?.imageUrl) {
          setCoverPreview(novelData.imageUrl);
        }
      }
    } catch (error) {
      console.error("Error fetching novel:", error);
      toast({
        title: "Error",
        description: "Failed to load novel",
        variant: "destructive",
      });
    } finally {
      setInitialLoading(false);
    }
  };

  const fetchNovelSources = async () => {
    setLoadingSources(true);
    try {
      const response = await api.getNovelSourcesByNovelId(params.id as string);
      if (response.success) {
        setNovelSources(response.data || []);
      }
    } catch (error) {
      console.error("Error fetching novel sources:", error);
    } finally {
      setLoadingSources(false);
    }
  };

  const handleAddSource = async () => {
    if (!newSource.sourceUrl.trim()) {
      toast({
        title: "Validation Error",
        description: "Source URL is required",
        variant: "destructive",
      });
      return;
    }

    setLoading(true);
    try {
      const sourceData = {
        ...newSource,
        novelId: params.id as string,
      };
      const response = await api.createNovelSource(sourceData);
      if (response.success) {
        toast({
          title: "Success",
          description: "Novel source created successfully",
        });
        setShowAddSource(false);
        setNewSource({
          novelId: "",
          sourceUrl: "",
          sourceId: "",
          sourcePlatform: "SHUBA69",
          syncIntervalMinutes: 60,
        });
        fetchNovelSources();
      }
    } catch (error) {
      console.error("Error creating novel source:", error);
      toast({
        title: "Error",
        description: "Failed to create novel source",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleTriggerSync = async (sourceId: string) => {
    setSyncingSource(sourceId);
    try {
      const response = await api.triggerSync(sourceId);
      if (response.success) {
        toast({
          title: "Success",
          description: "Sync triggered successfully",
        });
        fetchNovelSources();
      }
    } catch (error) {
      console.error("Error triggering sync:", error);
      toast({
        title: "Error",
        description: "Failed to trigger sync",
        variant: "destructive",
      });
    } finally {
      setSyncingSource(null);
    }
  };

  const handleToggleSource = async (sourceId: string, enabled: boolean) => {
    try {
      const response = await api.updateNovelSource(sourceId, { enabled: !enabled });
      if (response.success) {
        toast({
          title: "Success",
          description: `Source ${!enabled ? "enabled" : "disabled"} successfully`,
        });
        fetchNovelSources();
      }
    } catch (error) {
      console.error("Error toggling source:", error);
      toast({
        title: "Error",
        description: "Failed to update source",
        variant: "destructive",
      });
    }
  };

  const handleDeleteSource = async (sourceId: string) => {
    try {
      const response = await api.deleteNovelSource(sourceId);
      if (response.success) {
        toast({
          title: "Success",
          description: "Source deleted successfully",
        });
        setDeletingSource(null);
        fetchNovelSources();
      }
    } catch (error) {
      console.error("Error deleting source:", error);
      toast({
        title: "Error",
        description: "Failed to delete source",
        variant: "destructive",
      });
    }
  };

  const getSyncStatusBadge = (status: string) => {
    const variants: Record<string, "default" | "secondary" | "destructive" | "outline"> = {
      IDLE: "secondary",
      SYNCING: "default",
      SUCCESS: "outline",
      FAILED: "destructive",
    };
    return (
      <Badge variant={variants[status] || "default"}>
        {status}
      </Badge>
    );
  };

  const handleInputChange = (field: keyof NovelFormData, value: string) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));

    if (field === "title") {
      const slug = value
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .replace(/[^a-z0-9\s-]/g, "")
        .replace(/\s+/g, "-")
        .replace(/-+/g, "-")
        .trim();
      setFormData((prev) => ({ ...prev, slug }));
    }
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setCoverImage(file);
      const reader = new FileReader();
      reader.onload = (e) => {
        setCoverPreview(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const addToArray = (
    field: "categories" | "genres" | "tags",
    value: string
  ) => {
    if (value.trim() && !formData[field].includes(value.trim())) {
      setFormData((prev) => ({
        ...prev,
        [field]: [...prev[field], value.trim()],
      }));
    }
  };

  const removeFromArray = (
    field: "categories" | "genres" | "tags",
    value: string
  ) => {
    setFormData((prev) => ({
      ...prev,
      [field]: prev[field].filter((item) => item !== value),
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      let coverImageId = null;
      if (coverImage) {
        const uploadResponse = await api.updateNovelCover(params.id, coverImage);
        if (uploadResponse.success) {
          coverImageId = uploadResponse.data.id;
        }
      }

      const novelData = {
        title: formData.title,
        slug: formData.slug,
        description: formData.description,
        author: formData.author,
        status: formData.status,
        categories: formData.categories,
        genres: formData.genres,
        tags: formData.tags,
        ...(coverImageId && { coverImage: coverImageId }),
      };

      const response = await api.updateNovel(novel!.id, novelData);

      if (response.success) {
        toast({
          title: "Success",
          description: "Novel updated successfully",
        });
        router.push("/admin/novels");
      }
    } catch (error) {
      console.error("Error updating novel:", error);
      toast({
        title: "Error",
        description: "Failed to update novel",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };
  const handleEpubChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      // Validate EPUB file type
      if (
        !file.name.endsWith(".epub") &&
        file.type !== "application/epub+zip"
      ) {
        toast({
          title: "Invalid file",
          description: "Please select a valid EPUB file",
          variant: "destructive",
        });
        return;
      }

      setEpubFile(file);
      setEpubPreview(file.name);
    }
  };

  const handleUploadEpub = async () => {
    if (!epubFile) {
      toast({
        title: "No file selected",
        description: "Please select an EPUB file to upload",
        variant: "destructive",
      });
      return;
    }

    setUploading(true);
    try {
      const response = await api.addChaptersFromEpub(novel?.id, epubFile);
      if (response.success) {
        toast({
          title: "Success",
          description:
            "EPUB file uploaded successfully. You will get notification about this shortly.",
        });
        setEpubFile(null);
        setEpubPreview("");
      }
    } catch (error) {
      const err = error instanceof Error ? error : new Error("Upload failed");
      toast({
        title: "Upload failed",
        description: err.message,
        variant: "destructive",
      });
    } finally {
      setUploading(false);
    }
  };

  if (initialLoading) {
    return (
      <AuthGuard requireRole={["ADMIN","AUTHOR"]}>
        <div className="min-h-screen bg-background">
          <main className="container mx-auto px-4 py-8">
            <div className="flex items-center justify-center h-64">
              <Loader2 className="h-8 w-8 animate-spin" />
            </div>
          </main>
        </div>
      </AuthGuard>
    );
  }

  if (!novel) {
    return (
      <AuthGuard requireRole={["ADMIN","AUTHOR"]}>
        <div className="min-h-screen bg-background">
          <main className="container mx-auto px-4 py-8">
            <div className="text-center">
              <h1 className="text-2xl font-bold">Novel not found</h1>
              <Link href="/admin/novels">
                <Button className="mt-4">Back to Novels</Button>
              </Link>
            </div>
          </main>
        </div>
      </AuthGuard>
    );
  }

  return (
    <AuthGuard requireRole={["ADMIN","AUTHOR"]}>
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
              <Link href="/admin/novels">
                <Button variant="outline" size="sm">
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back to Novels
                </Button>
              </Link>
              <div>
                <h1 className="text-3xl font-bold">Edit Novel</h1>
                <p className="text-muted-foreground">
                  Update novel information
                </p>
              </div>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Main Form */}
                <div className="lg:col-span-2 space-y-6">
                  <Card>
                    <CardHeader>
                      <CardTitle>Basic Information</CardTitle>
                      <CardDescription>
                        Update the basic details of the novel
                      </CardDescription>
                    </CardHeader>

                  {/* Novel Sources */}
                  <Card>
                    <CardHeader>
                      <div className="flex items-center justify-between">
                        <div>
                          <CardTitle>Novel Sources</CardTitle>
                          <CardDescription>
                            Manage external sources for automatic chapter imports
                          </CardDescription>
                        </div>
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => setShowAddSource(!showAddSource)}
                        >
                          {showAddSource ? "Cancel" : "+ Add Source"}
                        </Button>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      {/* Add New Source Form */}
                      {showAddSource && (
                        <div className="p-4 border rounded-lg space-y-4 bg-muted/50">
                          <div className="space-y-2">
                            <Label htmlFor="sourceUrl">Source URL *</Label>
                            <Input
                              id="sourceUrl"
                              value={newSource.sourceUrl}
                              onChange={(e) =>
                                setNewSource({ ...newSource, sourceUrl: e.target.value })
                              }
                              placeholder="https://example.com/novel/123"
                            />
                          </div>
                          <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-2">
                              <Label htmlFor="sourceId">Source ID</Label>
                              <Input
                                id="sourceId"
                                value={newSource.sourceId}
                                onChange={(e) =>
                                  setNewSource({ ...newSource, sourceId: e.target.value })
                                }
                                placeholder="Novel ID on source"
                              />
                            </div>
                            <div className="space-y-2">
                              <Label htmlFor="sourcePlatform">Platform</Label>
                              <Select
                                value={newSource.sourcePlatform}
                                onValueChange={(value) =>
                                  setNewSource({ ...newSource, sourcePlatform: value as SourcePlatform })
                                }
                              >
                                <SelectTrigger>
                                  <SelectValue placeholder="Select platform" />
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem value="SHUBA69">SHUBA69</SelectItem>
                                  <SelectItem value="OTHER">OTHER</SelectItem>
                                </SelectContent>
                              </Select>
                            </div>
                          </div>
                          <div className="space-y-2">
                            <Label htmlFor="syncInterval">Sync Interval (minutes)</Label>
                            <Input
                              id="syncInterval"
                              type="number"
                              value={newSource.syncIntervalMinutes}
                              onChange={(e) =>
                                setNewSource({
                                  ...newSource,
                                  syncIntervalMinutes: parseInt(e.target.value) || 60,
                                })
                              }
                              min={15}
                              placeholder="60"
                            />
                          </div>
                          <Button
                            type="button"
                            onClick={handleAddSource}
                            disabled={loading}
                            className="w-full"
                          >
                            {loading ? "Creating..." : "Create Source"}
                          </Button>
                        </div>
                      )}

                      {/* Sources List */}
                      {loadingSources ? (
                        <div className="flex items-center justify-center py-8">
                          <Loader2 className="h-6 w-6 animate-spin" />
                        </div>
                      ) : novelSources.length === 0 ? (
                        <div className="text-center py-8 text-muted-foreground">
                          No sources configured yet
                        </div>
                      ) : (
                        <div className="space-y-3">
                          {novelSources.map((source) => (
                            <div
                              key={source.id}
                              className="p-4 border rounded-lg space-y-3"
                            >
                              <div className="flex items-start justify-between">
                                <div className="flex-1 space-y-1">
                                  <div className="flex items-center gap-2">
                                    <Badge variant="outline">
                                      {source.sourcePlatform}
                                    </Badge>
                                    {getSyncStatusBadge(source.syncStatus)}
                                    {!source.enabled && (
                                      <Badge variant="secondary">Disabled</Badge>
                                    )}
                                  </div>
                                  <p className="text-sm font-medium break-all">
                                    {source.sourceUrl}
                                  </p>
                                  {source.sourceId && (
                                    <p className="text-xs text-muted-foreground">
                                      ID: {source.sourceId}
                                    </p>
                                  )}
                                </div>
                              </div>

                              <div className="flex flex-wrap gap-2 text-xs text-muted-foreground">
                                {source.lastSyncedChapter && (
                                  <span>Last synced: Ch. {source.lastSyncedChapter}</span>
                                )}
                                {source.lastSyncTime && (
                                  <span>
                                    • {new Date(source.lastSyncTime).toLocaleString()}
                                  </span>
                                )}
                                <span>
                                  • Interval: {source.syncIntervalMinutes}min
                                </span>
                                {source.consecutiveFailures > 0 && (
                                  <span className="text-destructive">
                                    • Failures: {source.consecutiveFailures}
                                  </span>
                                )}
                              </div>

                              {source.errorMessage && (
                                <p className="text-xs text-destructive bg-destructive/10 p-2 rounded">
                                  {source.errorMessage}
                                </p>
                              )}

                              <div className="flex gap-2">

        {/* Delete Confirmation Dialog */}
        <AlertDialog
          open={!!deletingSource}
          onOpenChange={() => setDeletingSource(null)}
        >
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Delete Novel Source</AlertDialogTitle>
              <AlertDialogDescription>
                Are you sure you want to delete this source? This action cannot be
                undone.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction
                onClick={() => deletingSource && handleDeleteSource(deletingSource)}
                className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              >
                Delete
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
                                <Button
                                  type="button"
                                  size="sm"
                                  variant="outline"
                                  onClick={() => handleTriggerSync(source.id)}
                                  disabled={syncingSource === source.id || source.syncStatus === "SYNCING"}
                                >
                                  {syncingSource === source.id ? (
                                    <>
                                      <Loader2 className="h-3 w-3 mr-1 animate-spin" />
                                      Syncing...
                                    </>
                                  ) : (
                                    "Sync Now"
                                  )}
                                </Button>
                                <Button
                                  type="button"
                                  size="sm"
                                  variant="outline"
                                  onClick={() =>
                                    handleToggleSource(source.id, source.enabled)
                                  }
                                >
                                  {source.enabled ? "Disable" : "Enable"}
                                </Button>
                                <Button
                                  type="button"
                                  size="sm"
                                  variant="destructive"
                                  onClick={() => setDeletingSource(source.id)}
                                >
                                  <X className="h-3 w-3" />
                                </Button>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </CardContent>
                  </Card>
                    <CardContent className="space-y-4">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="space-y-2">
                          <Label htmlFor="title">Title *</Label>
                          <Input
                            id="title"
                            value={formData.title}
                            onChange={(e) =>
                              handleInputChange("title", e.target.value)
                            }
                            placeholder="Enter novel title"
                            required
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="slug">Slug *</Label>
                          <Input
                            id="slug"
                            value={formData.slug}
                            onChange={(e) =>
                              handleInputChange("slug", e.target.value)
                            }
                            placeholder="novel-slug"
                            required
                          />
                        </div>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="space-y-2">
                          <Label htmlFor="author">Author *</Label>
                          <Input
                            id="author"
                            value={formData.author}
                            onChange={(e) =>
                              handleInputChange("author", e.target.value)
                            }
                            placeholder="Author name"
                            required
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="status">Status *</Label>
                          <Select
                            value={formData.status}
                            onValueChange={(value) =>
                              handleInputChange("status", value)
                            }
                          >
                            <SelectTrigger>
                              <SelectValue placeholder="Select status" />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="ONGOING">Ongoing</SelectItem>
                              <SelectItem value="COMPLETED">
                                Completed
                              </SelectItem>
                              <SelectItem value="HIATUS">Hiatus</SelectItem>
                              <SelectItem value="DROPPED">Dropped</SelectItem>
                            </SelectContent>
                          </Select>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="description">Description *</Label>
                        <Textarea
                          id="description"
                          value={formData.description}
                          onChange={(e) =>
                            handleInputChange("description", e.target.value)
                          }
                          placeholder="Enter novel description"
                          rows={6}
                          required
                        />
                      </div>
                    </CardContent>
                  </Card>
                  {/* Categories, Genres, Tags */}
                  <Card>
                    <CardHeader>
                      <CardTitle>Classification</CardTitle>
                      <CardDescription>
                        Add categories, genres, and tags
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-6">
                      {/* Categories */}
                      <div className="space-y-2">
                        <Label>Categories</Label>
                        <MultiSelectCombobox
                          label="Pick categories"
                          options={categoryOptions}
                          value={formData.categories}
                          onChange={(next) =>
                            setFormData((prev) => ({
                              ...prev,
                              categories: next,
                            }))
                          }
                          placeholder="Select categories…"
                          allowCreate
                        />
                      </div>

                      {/* Genres */}
                      <div className="space-y-2">
                        <Label>Genres</Label>
                        <MultiSelectCombobox
                          label="Pick genres"
                          options={genreOptions}
                          value={formData.genres}
                          onChange={(next) =>
                            setFormData((prev) => ({ ...prev, genres: next }))
                          }
                          placeholder="Select genres…"
                          allowCreate
                        />
                      </div>

                      {/* Tags */}
                      <div className="space-y-2">
                        <Label>Tags</Label>
                        <MultiSelectCombobox
                          label="Pick tags"
                          options={tagOptions}
                          value={formData.tags}
                          onChange={(next) =>
                            setFormData((prev) => ({ ...prev, tags: next }))
                          }
                          placeholder="Select tags…"
                          allowCreate
                        />
                      </div>
                    </CardContent>
                  </Card>{" "}
                </div>

                {/* Sidebar */}
                <div className="space-y-6">
                  {/* Cover Image */}
                  <Card>
                    <CardHeader>
                      <CardTitle>Cover Image</CardTitle>
                      <CardDescription>
                        Update the cover image for the novel
                      </CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-4">
                        {coverPreview ? (
                          <div className="relative">
                            <Image
                              src={coverPreview || "/placeholder.svg"}
                              alt="Cover preview"
                              width={200}
                              height={300}
                              className="rounded-lg object-cover mx-auto"
                            />
                            <Button
                              type="button"
                              variant="destructive"
                              size="sm"
                              className="absolute top-2 right-2"
                              onClick={() => {
                                setCoverImage(null);
                                setCoverPreview("");
                              }}
                            >
                              <X className="h-4 w-4" />
                            </Button>
                          </div>
                        ) : (
                          <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
                            <Upload className="mx-auto h-12 w-12 text-gray-400" />
                            <p className="mt-2 text-sm text-gray-600">
                              Upload cover image
                            </p>
                          </div>
                        )}
                        <Input
                          type="file"
                          accept="image/*"
                          onChange={handleImageChange}
                          className="cursor-pointer"
                        />
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle>EPUB File</CardTitle>
                      <CardDescription>
                        Upload an EPUB file for the novel
                      </CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-4">
                        {epubPreview ? (
                          <div className="relative">
                            <div className="flex items-center gap-3 p-4 bg-blue-50 rounded-lg border border-blue-200">
                              <FileText className="h-8 w-8 text-blue-600" />
                              <div className="flex-1">
                                <p className="font-medium text-sm text-blue-900">
                                  {epubPreview}
                                </p>
                                <p className="text-xs text-blue-700">
                                  {epubFile &&
                                    `${(epubFile.size / 1024 / 1024).toFixed(
                                      2
                                    )} MB`}
                                </p>
                              </div>
                              <Button
                                type="button"
                                variant="ghost"
                                size="sm"
                                onClick={() => {
                                  setEpubFile(null);
                                  setEpubPreview("");
                                }}
                              >
                                <X className="h-4 w-4" />
                              </Button>
                            </div>
                          </div>
                        ) : (
                          <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
                            <Upload className="mx-auto h-12 w-12 text-gray-400" />
                            <p className="mt-2 text-sm text-gray-600">
                              Upload EPUB file
                            </p>
                          </div>
                        )}
                        <Input
                          type="file"
                          accept=".epub,application/epub+zip"
                          onChange={handleEpubChange}
                          className="cursor-pointer"
                          disabled={uploading}
                        />
                        {epubFile && (
                          <Button
                            type="button"
                            onClick={handleUploadEpub}
                            disabled={uploading}
                            className="w-full"
                          >
                            {uploading ? (
                              <>
                                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                Uploading...
                              </>
                            ) : (
                              <>
                                <Upload className="h-4 w-4 mr-2" />
                                Upload EPUB
                              </>
                            )}
                          </Button>
                        )}
                      </div>
                    </CardContent>
                  </Card>

                  {/* Actions */}
                  <Card>
                    <CardContent className="pt-6">
                      <div className="space-y-2">
                        <Button
                          type="submit"
                          className="w-full"
                          disabled={loading}
                        >
                          {loading ? "Updating..." : "Update Novel"}
                        </Button>
                        <Button
                          type="button"
                          variant="outline"
                          className="w-full bg-transparent"
                          asChild
                        >
                          <Link href="/admin/novels">Cancel</Link>
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </div>
            </form>
          </div>
        </main>
      </div>
    </AuthGuard>
  );
}
