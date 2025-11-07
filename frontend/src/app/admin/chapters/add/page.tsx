"use client"

import type React from "react"

import { useState, useEffect, useRef } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Progress } from "@/components/ui/progress"
import { useToast } from "@/hooks/use-toast"
import { api, type Novel } from "@/lib/api"
import {
	Bold,
	Italic,
	Underline,
	AlignLeft,
	AlignCenter,
	AlignRight,
	List,
	Type,
	Upload,
	FileText,
	X,
	BookOpen,
	Clock,
	FileCheck,
	Lightbulb,
} from "lucide-react"

interface RichTextEditorProps {
	content: string
	onChange: (content: string) => void
}

function RichTextEditor({ content, onChange }: RichTextEditorProps) {
	const editorRef = useRef<HTMLDivElement>(null)

	const execCommand = (command: string, value?: string) => {
		document.execCommand(command, false, value)
		if (editorRef.current) {
			onChange(editorRef.current.innerHTML)
		}
	}

	const handleInput = () => {
		if (editorRef.current) {
			onChange(editorRef.current.innerHTML)
		}
	}

	useEffect(() => {
		if (editorRef.current && editorRef.current.innerHTML !== content) {
			editorRef.current.innerHTML = content
		}
	}, [content])

	return (
		<div className="border rounded-lg overflow-hidden">
			{/* Toolbar */}
			<div className="flex flex-wrap gap-1 p-2 border-b bg-muted/50">
				<Button type="button" variant="ghost" size="sm" onClick={() => execCommand("bold")} className="h-8 w-8 p-0">
					<Bold className="h-4 w-4" />
				</Button>
				<Button type="button" variant="ghost" size="sm" onClick={() => execCommand("italic")} className="h-8 w-8 p-0">
					<Italic className="h-4 w-4" />
				</Button>
				<Button
					type="button"
					variant="ghost"
					size="sm"
					onClick={() => execCommand("underline")}
					className="h-8 w-8 p-0"
				>
					<Underline className="h-4 w-4" />
				</Button>

				<Separator orientation="vertical" className="h-8" />

				<Button
					type="button"
					variant="ghost"
					size="sm"
					onClick={() => execCommand("justifyLeft")}
					className="h-8 w-8 p-0"
				>
					<AlignLeft className="h-4 w-4" />
				</Button>
				<Button
					type="button"
					variant="ghost"
					size="sm"
					onClick={() => execCommand("justifyCenter")}
					className="h-8 w-8 p-0"
				>
					<AlignCenter className="h-4 w-4" />
				</Button>
				<Button
					type="button"
					variant="ghost"
					size="sm"
					onClick={() => execCommand("justifyRight")}
					className="h-8 w-8 p-0"
				>
					<AlignRight className="h-4 w-4" />
				</Button>

				<Separator orientation="vertical" className="h-8" />

				<Button
					type="button"
					variant="ghost"
					size="sm"
					onClick={() => execCommand("insertUnorderedList")}
					className="h-8 w-8 p-0"
				>
					<List className="h-4 w-4" />
				</Button>

				<Separator orientation="vertical" className="h-8" />

				<Button
					type="button"
					variant="ghost"
					size="sm"
					onClick={() => execCommand("formatBlock", "h1")}
					className="h-8 px-2 text-xs font-bold"
				>
					H1
				</Button>
				<Button
					type="button"
					variant="ghost"
					size="sm"
					onClick={() => execCommand("formatBlock", "h2")}
					className="h-8 px-2 text-xs font-bold"
				>
					H2
				</Button>
				<Button
					type="button"
					variant="ghost"
					size="sm"
					onClick={() => execCommand("formatBlock", "h3")}
					className="h-8 px-2 text-xs font-bold"
				>
					H3
				</Button>
				<Button
					type="button"
					variant="ghost"
					size="sm"
					onClick={() => execCommand("formatBlock", "p")}
					className="h-8 px-2 text-xs"
				>
					P
				</Button>
			</div>

			{/* Editor */}
			<div
				ref={editorRef}
				contentEditable
				onInput={handleInput}
				className="min-h-[400px] p-4 focus:outline-none prose prose-sm max-w-none dark:prose-invert"
				style={{ whiteSpace: "pre-wrap" }}
				suppressContentEditableWarning={true}
			/>
		</div>
	)
}

export default function AddChapterPage() {
	const router = useRouter()
	const searchParams = useSearchParams()
	const { toast } = useToast()

	const [novels, setNovels] = useState<Novel[]>([])
	const [selectedNovelId, setSelectedNovelId] = useState("")
	const [title, setTitle] = useState("")
	const [chapterNumber, setChapterNumber] = useState("")
	const [contentHtml, setContent] = useState("")
	const [isLoading, setIsLoading] = useState(false)
	const [isLoadingNovels, setIsLoadingNovels] = useState(true)
	const [uploadedFile, setUploadedFile] = useState<File | null>(null)
	const [uploadProgress, setUploadProgress] = useState(0)
	const [activeTab, setActiveTab] = useState("editor")

	// Get novelId from URL params
	const novelIdParam = searchParams.get("novelId")

	useEffect(() => {
		fetchNovels()
	}, [])

	useEffect(() => {
		if (novelIdParam && novels.length > 0) {
			const novel = novels.find((n) => n.id === novelIdParam)
			if (novel) {
				setSelectedNovelId(novelIdParam)
				toast({
					title: "Novel Pre-selected",
					description: `Selected "${novel.title}" from URL parameter`,
				})
			} else {
				toast({
					title: "Novel Not Found",
					description: "The novel ID from the URL was not found",
					variant: "destructive",
				})
			}
		}
	}, [novelIdParam, novels, toast])

	const fetchNovels = async () => {
		try {
			setIsLoadingNovels(true)
			const response = await api.getNovels({ size: 100 })
			if (response.success) {
				setNovels(response.data.content)
			} else {
				toast({
					title: "Error",
					description: "Failed to fetch novels",
					variant: "destructive",
				})
			}
		} catch (error) {
			console.error("Error fetching novels:", error)
			toast({
				title: "Error",
				description: "Failed to fetch novels",
				variant: "destructive",
			})
		} finally {
			setIsLoadingNovels(false)
		}
	}

	const handleFileUpload = async (file: File) => {
		if (!file) return

		// Validate file type
		const allowedTypes = [
			"text/plain",
			"text/html",
			"application/msword",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/rtf",
		]

		if (!allowedTypes.includes(file.type)) {
			toast({
				title: "Invalid File Type",
				description: "Please upload a TXT, HTML, DOC, DOCX, or RTF file",
				variant: "destructive",
			})
			return
		}

		// Validate file size (max 10MB)
		if (file.size > 10 * 1024 * 1024) {
			toast({
				title: "File Too Large",
				description: "Please upload a file smaller than 10MB",
				variant: "destructive",
			})
			return
		}

		setUploadedFile(file)
		setUploadProgress(0)

		try {
			// Simulate upload progress
			const progressInterval = setInterval(() => {
				setUploadProgress((prev) => {
					if (prev >= 90) {
						clearInterval(progressInterval)
						return 90
					}
					return prev + 10
				})
			}, 100)

			// Read file content
			const text = await file.text()

			// Process content based on file type
			let processedContent = text
			if (file.type === "text/html") {
				processedContent = text
			} else {
				// Convert plain text to HTML with basic formatting
				processedContent = text
					.split("\n\n")
					.map((paragraph) => paragraph.trim())
					.filter((paragraph) => paragraph.length > 0)
					.map((paragraph) => `<p>${paragraph.replace(/\n/g, "<br>")}</p>`)
					.join("\n")
			}

			setContent(processedContent)

			// Auto-fill title if empty
			if (!title) {
				const fileName = file.name.replace(/\.[^/.]+$/, "")
				setTitle(fileName)
			}

			clearInterval(progressInterval)
			setUploadProgress(100)

			toast({
				title: "File Uploaded Successfully",
				description: `Content from "${file.name}" has been imported`,
			})

			// Switch to editor tab to show the content
			setActiveTab("editor")
		} catch (error) {
			console.error("Error processing file:", error)
			toast({
				title: "Upload Failed",
				description: "Failed to process the uploaded file",
				variant: "destructive",
			})
			setUploadedFile(null)
			setUploadProgress(0)
		}
	}

	const handleDrop = (e: React.DragEvent) => {
		e.preventDefault()
		const files = Array.from(e.dataTransfer.files)
		if (files.length > 0) {
			handleFileUpload(files[0])
		}
	}

	const handleDragOver = (e: React.DragEvent) => {
		e.preventDefault()
	}

	const removeUploadedFile = () => {
		setUploadedFile(null)
		setUploadProgress(0)
	}

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault()

		if (!selectedNovelId || !title || !chapterNumber || !contentHtml) {
			toast({
				title: "Missing Information",
				description: "Please fill in all required fields",
				variant: "destructive",
			})
			return
		}

		try {
			setIsLoading(true)

			const response = await api.createChapter({
				novelId: selectedNovelId,
				title,
				chapterNumber: Number.parseInt(chapterNumber),
				contentHtml,
			})

			if (response.success) {
				toast({
					title: "Success",
					description: "Chapter created successfully",
				})
				router.push("/admin/chapters")
			} else {
				toast({
					title: "Error",
					description: response.message || "Failed to create chapter",
					variant: "destructive",
				})
			}
		} catch (error) {
			console.error("Error creating chapter:", error)
			toast({
				title: "Error",
				description: "Failed to create chapter",
				variant: "destructive",
			})
		} finally {
			setIsLoading(false)
		}
	}

	// Calculate content statistics
	const getContentStats = () => {
		const plainText = contentHtml
			.replace(/<[^>]*>/g, " ")
			.replace(/\s+/g, " ")
			.trim()
		const wordCount = plainText ? plainText.split(" ").length : 0
		const charCount = plainText.length
		const readingTime = Math.ceil(wordCount / 200) // Assuming 200 words per minute

		return { wordCount, charCount, readingTime }
	}

	const stats = getContentStats()

	return (
		<div className="container mx-auto py-6 max-w-6xl">
			<div className="flex items-center justify-between mb-6">
				<div>
					<h1 className="text-3xl font-bold">Add New Chapter</h1>
					<p className="text-muted-foreground">Create a new chapter for your novel</p>
				</div>
				<Button variant="outline" onClick={() => router.back()}>
					Cancel
				</Button>
			</div>

			<div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
				{/* Main Content */}
				<div className="lg:col-span-3">
					<form onSubmit={handleSubmit} className="space-y-6">
						{/* Basic Information */}
						<Card>
							<CardHeader>
								<CardTitle>Chapter Information</CardTitle>
								<CardDescription>Basic details about the chapter</CardDescription>
							</CardHeader>
							<CardContent className="space-y-4">
								<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
									<div className="space-y-2">
										<Label htmlFor="novel">Novel *</Label>
										<Select value={selectedNovelId} onValueChange={setSelectedNovelId} disabled={isLoadingNovels}>
											<SelectTrigger>
												<SelectValue placeholder={isLoadingNovels ? "Loading novels..." : "Select a novel"} />
											</SelectTrigger>
											<SelectContent>
												{novels.map((novel) => (
													<SelectItem key={novel.id} value={novel.id}>
														{novel.title}
													</SelectItem>
												))}
											</SelectContent>
										</Select>
									</div>

									<div className="space-y-2">
										<Label htmlFor="chapterNumber">Chapter Number *</Label>
										<Input
											id="chapterNumber"
											type="number"
											min="1"
											value={chapterNumber}
											onChange={(e) => setChapterNumber(e.target.value)}
											placeholder="e.g., 1"
											required
										/>
									</div>
								</div>

								<div className="space-y-2">
									<Label htmlFor="title">Chapter Title *</Label>
									<Input
										id="title"
										value={title}
										onChange={(e) => setTitle(e.target.value)}
										placeholder="Enter chapter title"
										required
									/>
								</div>
							</CardContent>
						</Card>

						{/* Content Editor */}
						<Card>
							<CardHeader>
								<CardTitle>Chapter Content</CardTitle>
								<CardDescription>Write your chapter content or upload a file</CardDescription>
							</CardHeader>
							<CardContent>
								<Tabs value={activeTab} onValueChange={setActiveTab}>
									<TabsList className="grid w-full grid-cols-2">
										<TabsTrigger value="editor" className="flex items-center gap-2">
											<Type className="h-4 w-4" />
											Rich Text Editor
										</TabsTrigger>
										<TabsTrigger value="upload" className="flex items-center gap-2">
											<Upload className="h-4 w-4" />
											File Upload
										</TabsTrigger>
									</TabsList>

									<TabsContent value="editor" className="space-y-4">
										<RichTextEditor content={contentHtml} onChange={setContent} />
									</TabsContent>

									<TabsContent value="upload" className="space-y-4">
										<div
											onDrop={handleDrop}
											onDragOver={handleDragOver}
											className="border-2 border-dashed border-muted-foreground/25 rounded-lg p-8 text-center hover:border-muted-foreground/50 transition-colors"
										>
											<Upload className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
											<h3 className="text-lg font-semibold mb-2">Upload Chapter File</h3>
											<p className="text-muted-foreground mb-4">Drag and drop your file here, or click to browse</p>
											<input
												type="file"
												accept=".txt,.html,.doc,.docx,.rtf"
												onChange={(e) => {
													const file = e.target.files?.[0]
													if (file) handleFileUpload(file)
												}}
												className="hidden"
												id="file-upload"
											/>
											<Button
												type="button"
												variant="outline"
												onClick={() => document.getElementById("file-upload")?.click()}
											>
												Choose File
											</Button>
											<p className="text-xs text-muted-foreground mt-2">
												Supported formats: TXT, HTML, DOC, DOCX, RTF (max 10MB)
											</p>
										</div>

										{uploadedFile && (
											<Card>
												<CardContent className="pt-6">
													<div className="flex items-center justify-between mb-2">
														<div className="flex items-center gap-2">
															<FileText className="h-4 w-4" />
															<span className="font-medium">{uploadedFile.name}</span>
															<Badge variant="secondary">{(uploadedFile.size / 1024 / 1024).toFixed(2)} MB</Badge>
														</div>
														<Button type="button" variant="ghost" size="sm" onClick={removeUploadedFile}>
															<X className="h-4 w-4" />
														</Button>
													</div>
													{uploadProgress < 100 && <Progress value={uploadProgress} className="mb-2" />}
													{uploadProgress === 100 && (
														<div className="flex items-center gap-2 text-green-600">
															<FileCheck className="h-4 w-4" />
															<span className="text-sm">File processed successfully</span>
														</div>
													)}
												</CardContent>
											</Card>
										)}
									</TabsContent>
								</Tabs>
							</CardContent>
						</Card>

						{/* Submit Button */}
						<div className="flex justify-end">
							<Button type="submit" disabled={isLoading} className="min-w-[120px]">
								{isLoading ? "Creating..." : "Create Chapter"}
							</Button>
						</div>
					</form>
				</div>

				{/* Sidebar */}
				<div className="space-y-6">
					{/* Content Statistics */}
					<Card>
						<CardHeader>
							<CardTitle className="flex items-center gap-2">
								<BookOpen className="h-5 w-5" />
								Content Statistics
							</CardTitle>
						</CardHeader>
						<CardContent className="space-y-3">
							<div className="flex justify-between">
								<span className="text-sm text-muted-foreground">Words:</span>
								<span className="font-medium">{stats.wordCount.toLocaleString()}</span>
							</div>
							<div className="flex justify-between">
								<span className="text-sm text-muted-foreground">Characters:</span>
								<span className="font-medium">{stats.charCount.toLocaleString()}</span>
							</div>
							<div className="flex justify-between">
								<span className="text-sm text-muted-foreground flex items-center gap-1">
									<Clock className="h-3 w-3" />
									Reading time:
								</span>
								<span className="font-medium">{stats.readingTime} min</span>
							</div>
						</CardContent>
					</Card>

					{/* Writing Tips */}
					<Card>
						<CardHeader>
							<CardTitle className="flex items-center gap-2">
								<Lightbulb className="h-5 w-5" />
								Writing Tips
							</CardTitle>
						</CardHeader>
						<CardContent className="space-y-3">
							<div className="text-sm space-y-2">
								<p>• Keep chapters between 2,000-5,000 words for optimal reading experience</p>
								<p>• Use descriptive chapter titles to engage readers</p>
								<p>• End chapters with hooks to encourage continued reading</p>
								<p>• Maintain consistent formatting throughout your novel</p>
							</div>
						</CardContent>
					</Card>

					{/* URL Parameter Info */}
					{novelIdParam && (
						<Alert>
							<AlertDescription>
								Novel pre-selected from URL parameter. You can change the selection above if needed.
							</AlertDescription>
						</Alert>
					)}
				</div>
			</div>
		</div>
	)
}
