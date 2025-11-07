"use client"

import type React from "react"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { MultiSelectCombobox } from "@/components/ui/multi-select-combobox"
import { ArrowLeft, Upload, X, Plus } from "lucide-react"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { api } from "@/lib/api"
import Image from "next/image"

interface NovelFormData {
    title: string
    slug: string
    description: string
    coverImage: File | null
    author: string
    status: string
    categories: string[]
    genres: string[]
    tags: string[]
}

export default function AddNovelPage() {
    const router = useRouter()
    const { toast } = useToast()
    const [loading, setLoading] = useState(false)
    const [coverImage, setCoverImage] = useState<File | null>(null)
    const [coverPreview, setCoverPreview] = useState<string>("")
    const [newCategory, setNewCategory] = useState("")
    const [newGenre, setNewGenre] = useState("")
    const [newTag, setNewTag] = useState("")
    const [categoryOptions, setCategoryOptions] = useState<string[]>([])
    const [genreOptions, setGenreOptions] = useState<string[]>([])
    const [tagOptions, setTagOptions] = useState<string[]>([])

    const [formData, setFormData] = useState<NovelFormData>({
        title: "",
        slug: "",
        description: "",
        coverImage: null,
        author: "",
        status: "ONGOING",
        categories: [],
        genres: [],
        tags: [],
    })

    // Load selectable options from backend
    useEffect(() => {
        const fetchNames = async (path: string): Promise<string[]> => {
            try {
                const res = await api.request(path, { method: "GET" })
                const list = Array.isArray(res?.data) ? res.data : []
                return list.map((x: any) => x?.name).filter(Boolean)
            } catch {
                return []
            }
        }
            ; (async () => {
                const [genres, categories, tags] = await Promise.all([
                    fetchNames("/api/novels/genres"),
                    fetchNames("/api/novels/categories"),
                    fetchNames("/api/novels/tags"),
                ])
                setGenreOptions(genres)
                setCategoryOptions(categories)
                setTagOptions(tags)
            })()
    }, [])

    const handleInputChange = (field: keyof NovelFormData, value: string) => {
        setFormData((prev) => ({
            ...prev,
            [field]: value,
        }))

        if (field === "title") {
            const slug = value
                .normalize("NFD")
                .replace(/[\u0300-\u036f]/g, "")
                .toLowerCase()
                .replace(/[^a-z0-9\s-]/g, "")
                .replace(/\s+/g, "-")
                .replace(/-+/g, "-")
                .trim()
            setFormData(prev => ({ ...prev, slug }))
            }

    }

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0]
        if (file) {
            setCoverImage(file)
            const reader = new FileReader()
            reader.onload = (e) => {
                setCoverPreview(e.target?.result as string)
            }
            reader.readAsDataURL(file)
        }
    }

    const addToArray = (field: "categories" | "genres" | "tags", value: string) => {
        if (value.trim() && !formData[field].includes(value.trim())) {
            setFormData((prev) => ({
                ...prev,
                [field]: [...prev[field], value.trim()],
            }))
        }
    }

    const removeFromArray = (field: "categories" | "genres" | "tags", value: string) => {
        setFormData((prev) => ({
            ...prev,
            [field]: prev[field].filter((item) => item !== value),
        }))
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);

        try {
        const response = await api.createNovel({
            title: formData.title,
            slug: formData.slug,
            description: formData.description,
            author: formData.author,
            status: formData.status,
            categories: formData.categories,
            genres: formData.genres,
            tags: formData.tags,
        });
            if (response.success) {
                toast({ title: "Success", description: "Novel created successfully"});
                router.push("/admin/novels/"+formData.slug);
            }

        } catch (err) {
            console.error(err);
            toast({ title: "Error", description: "Failed to create novel", variant: "destructive" });
        } finally {
            setLoading(false);
        }
    };

    return (
        <AuthGuard requireAdmin>
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
                                <h1 className="text-3xl font-bold">Add New Novel</h1>
                                <p className="text-muted-foreground">Create a new novel in the system</p>
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
                                            <CardDescription>Enter the basic details of the novel</CardDescription>
                                        </CardHeader>
                                        <CardContent className="space-y-4">
                                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                                <div className="space-y-2">
                                                    <Label htmlFor="title">Title *</Label>
                                                    <Input
                                                        id="title"
                                                        value={formData.title}
                                                        onChange={(e) => handleInputChange("title", e.target.value)}
                                                        placeholder="Enter novel title"
                                                        required
                                                    />
                                                </div>
                                                <div className="space-y-2">
                                                    <Label htmlFor="slug">Slug *</Label>
                                                    <Input
                                                        id="slug"
                                                        value={formData.slug}
                                                        onChange={(e) => handleInputChange("slug", e.target.value)}
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
                                                        onChange={(e) => handleInputChange("author", e.target.value)}
                                                        placeholder="Author name"
                                                        required
                                                    />
                                                </div>
                                                <div className="space-y-2">
                                                    <Label htmlFor="status">Status *</Label>
                                                    <Select value={formData.status} onValueChange={(value) => handleInputChange("status", value)}>
                                                        <SelectTrigger>
                                                            <SelectValue placeholder="Select status" />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            <SelectItem value="ONGOING">Ongoing</SelectItem>
                                                            <SelectItem value="COMPLETED">Completed</SelectItem>
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
                                                    onChange={(e) => handleInputChange("description", e.target.value)}
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
                                            <CardDescription>Add categories, genres, and tags</CardDescription>
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
                                                        setFormData(prev => ({ ...prev, categories: next }))
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
                                                        setFormData(prev => ({ ...prev, genres: next }))
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
                                                        setFormData(prev => ({ ...prev, tags: next }))
                                                    }
                                                    placeholder="Select tags…"
                                                    allowCreate
                                                />
                                            </div>
                                        </CardContent>
                                    </Card>
                                </div>

                                {/* Sidebar */}
                                <div className="space-y-6">
                                    {/* Cover Image */}
                                    {/* <Card>
                                        <CardHeader>
                                            <CardTitle>Cover Image</CardTitle>
                                            <CardDescription>Upload a cover image for the novel</CardDescription>
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
                                                                setCoverImage(null)
                                                                setCoverPreview("")
                                                            }}
                                                        >
                                                            <X className="h-4 w-4" />
                                                        </Button>
                                                    </div>
                                                ) : (
                                                    <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
                                                        <Upload className="mx-auto h-12 w-12 text-gray-400" />
                                                        <p className="mt-2 text-sm text-gray-600">Upload cover image</p>
                                                    </div>
                                                )}
                                                <Input type="file" accept="image/*" onChange={handleImageChange} className="cursor-pointer" />
                                            </div>
                                        </CardContent>
                                    </Card> */}

                                    {/* Actions */}
                                    <Card>
                                        <CardContent className="pt-6">
                                            <div className="space-y-2">
                                                <Button type="submit" className="w-full" disabled={loading}>
                                                    {loading ? "Creating..." : "Create Novel"}
                                                </Button>
                                                <Button type="button" variant="outline" className="w-full bg-transparent" asChild>
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
    )
}
