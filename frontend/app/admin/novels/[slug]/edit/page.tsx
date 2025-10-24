"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ArrowLeft, Upload, X, Plus, Loader2 } from "lucide-react"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { api, type Novel } from "@/lib/api"
import Image from "next/image"

interface NovelFormData {
	title: string
	slug: string
	description: string
	author: string
	status: string
	categories: string[]
	genres: string[]
	tags: string[]
}

export default function EditNovelPage({ params }: { params: { slug: string } }) {
	const router = useRouter()
	const { toast } = useToast()
	const [loading, setLoading] = useState(false)
	const [initialLoading, setInitialLoading] = useState(true)
	const [novel, setNovel] = useState<Novel | null>(null)
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
		author: "",
		status: "ONGOING",
		categories: [],
		genres: [],
		tags: [],
	})

    useEffect(() => {
        fetchNovel()
    }, [params.slug])

    // Load selectable options for categories, genres, and tags
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
        ;(async () => {
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

	const fetchNovel = async () => {
		try {
			const response = await api.getNovelBySlug(params.slug)
			if (response.success) {
				const novelData = response.data
				setNovel(novelData)
                setFormData({
                    title: novelData.title,
                    slug: novelData.slug,
                    description: novelData.description,
                    author: novelData.author,
                    status: novelData.status,
                    categories: novelData.categories?.map((c) => c.name) || [],
                    genres: novelData.genres?.map((g) => g.name) || [],
                    tags: novelData.tags?.map((t) => t.name) || [],
                })
				if (novelData.coverImage?.fileUrl) {
					setCoverPreview(novelData.coverImage.fileUrl)
				}
			}
		} catch (error) {
			console.error("Error fetching novel:", error)
			toast({
				title: "Error",
				description: "Failed to load novel",
				variant: "destructive",
			})
		} finally {
			setInitialLoading(false)
		}
	}

	const handleInputChange = (field: keyof NovelFormData, value: string) => {
		setFormData((prev) => ({
			...prev,
			[field]: value,
		}))

		// Auto-generate slug from title
		if (field === "title") {
			const slug = value
				.toLowerCase()
				.replace(/[^a-z0-9\s-]/g, "")
				.replace(/\s+/g, "-")
				.replace(/-+/g, "-")
				.trim()
			setFormData((prev) => ({ ...prev, slug }))
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
		e.preventDefault()
		setLoading(true)

		try {
			// First upload cover image if provided
			let coverImageFile = null
			if (coverImage) {
				const imageFormData = new FormData()
				imageFormData.append("file", coverImage)

				const uploadResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/files/upload?type=NOVEL_COVER`, {
					method: "POST",
					headers: {
						Authorization: `Bearer ${api.getToken()}`,
					},
					body: imageFormData,
				})

				if (uploadResponse.ok) {
					const uploadResult = await uploadResponse.json()
					coverImageFile = uploadResult.data.id
				}
			}

			// Update novel
			const novelData = {
				...formData,
				...(coverImageFile && { coverImage: coverImageFile }),
			}

			const response = await api.request(`/api/novels/${novel?.id}`, {
				method: "PUT",
				body: JSON.stringify(novelData),
			})

			if (response.success) {
				toast({
					title: "Success",
					description: "Novel updated successfully",
				})
				router.push("/admin/novels")
			}
		} catch (error) {
			console.error("Error updating novel:", error)
			toast({
				title: "Error",
				description: "Failed to update novel",
				variant: "destructive",
			})
		} finally {
			setLoading(false)
		}
	}

	if (initialLoading) {
		return (
			<AuthGuard requireAdmin>
				<div className="min-h-screen bg-background">
					<Header />
					<main className="container mx-auto px-4 py-8">
						<div className="flex items-center justify-center h-64">
							<Loader2 className="h-8 w-8 animate-spin" />
						</div>
					</main>
				</div>
			</AuthGuard>
		)
	}

	if (!novel) {
		return (
			<AuthGuard requireAdmin>
				<div className="min-h-screen bg-background">
					<Header />
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
		)
	}

	return (
		<AuthGuard requireAdmin>
			<div className="min-h-screen bg-background">
				<Header />

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
								<p className="text-muted-foreground">Update novel information</p>
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
											<CardDescription>Update the basic details of the novel</CardDescription>
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
											<CardDescription>Update categories, genres, and tags</CardDescription>
										</CardHeader>
										<CardContent className="space-y-6">
											{/* Categories */}
											<div className="space-y-2">
												<Label>Categories</Label>
                                        <div>
                                            <select
                                                multiple
                                                className="w-full border rounded-md p-2 min-h-[120px] bg-background"
                                                value={formData.categories}
                                                onChange={(e) =>
                                                    setFormData((prev) => ({
                                                        ...prev,
                                                        categories: Array.from(e.currentTarget.selectedOptions, (o) => o.value),
                                                    }))
                                                }
                                            >
                                                {categoryOptions.map((name) => (
                                                    <option key={name} value={name}>
                                                        {name}
                                                    </option>
                                                ))}
                                            </select>
                                        </div>
												<div className="flex flex-wrap gap-2">
													{formData.categories.map((category) => (
														<div
															key={category}
															className="flex items-center gap-1 bg-blue-100 text-blue-800 px-2 py-1 rounded-md text-sm"
														>
															{category}
															<button
																type="button"
																onClick={() => removeFromArray("categories", category)}
																className="text-blue-600 hover:text-blue-800"
															>
																<X className="h-3 w-3" />
															</button>
														</div>
													))}
												</div>
											</div>

											{/* Genres */}
											<div className="space-y-2">
												<Label>Genres</Label>
                                        <div>
                                            <select
                                                multiple
                                                className="w-full border rounded-md p-2 min-h-[120px] bg-background"
                                                value={formData.genres}
                                                onChange={(e) =>
                                                    setFormData((prev) => ({
                                                        ...prev,
                                                        genres: Array.from(e.currentTarget.selectedOptions, (o) => o.value),
                                                    }))
                                                }
                                            >
                                                {genreOptions.map((name) => (
                                                    <option key={name} value={name}>
                                                        {name}
                                                    </option>
                                                ))}
                                            </select>
                                        </div>
												<div className="flex flex-wrap gap-2">
													{formData.genres.map((genre) => (
														<div
															key={genre}
															className="flex items-center gap-1 bg-green-100 text-green-800 px-2 py-1 rounded-md text-sm"
														>
															{genre}
															<button
																type="button"
																onClick={() => removeFromArray("genres", genre)}
																className="text-green-600 hover:text-green-800"
															>
																<X className="h-3 w-3" />
															</button>
														</div>
													))}
												</div>
											</div>

											{/* Tags */}
											<div className="space-y-2">
												<Label>Tags</Label>
												<div className="flex gap-2">
                                            <select
                                                multiple
                                                className="w-full border rounded-md p-2 min-h-[120px] bg-background"
                                                value={formData.tags}
                                                onChange={(e) =>
                                                    setFormData((prev) => ({
                                                        ...prev,
                                                        tags: Array.from(e.currentTarget.selectedOptions, (o) => o.value),
                                                    }))
                                                }
                                            >
                                                {tagOptions.map((name) => (
                                                    <option key={name} value={name}>
                                                        {name}
                                                    </option>
                                                ))}
                                            </select>
												</div>
												<div className="flex flex-wrap gap-2">
													{formData.tags.map((tag) => (
														<div
															key={tag}
															className="flex items-center gap-1 bg-purple-100 text-purple-800 px-2 py-1 rounded-md text-sm"
														>
															{tag}
															<button
																type="button"
																onClick={() => removeFromArray("tags", tag)}
																className="text-purple-600 hover:text-purple-800"
															>
																<X className="h-3 w-3" />
															</button>
														</div>
													))}
												</div>
											</div>
										</CardContent>
									</Card>
								</div>

								{/* Sidebar */}
								<div className="space-y-6">
									{/* Cover Image */}
									<Card>
										<CardHeader>
											<CardTitle>Cover Image</CardTitle>
											<CardDescription>Update the cover image for the novel</CardDescription>
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
									</Card>

									{/* Actions */}
									<Card>
										<CardContent className="pt-6">
											<div className="space-y-2">
												<Button type="submit" className="w-full" disabled={loading}>
													{loading ? "Updating..." : "Update Novel"}
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
