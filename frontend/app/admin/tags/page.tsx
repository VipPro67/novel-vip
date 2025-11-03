"use client"

import { useState, useEffect } from "react"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Search, MoreHorizontal, Edit, Trash2, Plus } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"

interface Tag {
  id: string
  name: string
  novelCount: number
  createdAt: string
}

export default function AdminTagsPage() {
  const [tags, setTags] = useState<Tag[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [showAddDialog, setShowAddDialog] = useState(false)
  const [editingTag, setEditingTag] = useState<Tag | null>(null)
  const [tagName, setTagName] = useState("")
  const { toast } = useToast()

  useEffect(() => {
    fetchTags()
  }, [])

  const fetchTags = async () => {
    setLoading(true)
    try {
      // Mock data - replace with actual API call
      const mockTags: Tag[] = [
        { id: "1", name: "Magic", novelCount: 89, createdAt: "2024-01-01" },
        { id: "2", name: "Dragons", novelCount: 45, createdAt: "2024-01-01" },
        { id: "3", name: "School Life", novelCount: 67, createdAt: "2024-01-01" },
        { id: "4", name: "Cultivation", novelCount: 123, createdAt: "2024-01-01" },
        { id: "5", name: "System", novelCount: 78, createdAt: "2024-01-01" },
        { id: "6", name: "Reincarnation", novelCount: 56, createdAt: "2024-01-01" },
        { id: "7", name: "Martial Arts", novelCount: 91, createdAt: "2024-01-01" },
        { id: "8", name: "Slice of Life", novelCount: 34, createdAt: "2024-01-01" },
        { id: "9", name: "Harem", novelCount: 42, createdAt: "2024-01-01" },
        { id: "10", name: "Overpowered MC", novelCount: 87, createdAt: "2024-01-01" },
      ]

      setTags(mockTags)
    } catch (error) {
      console.error("Failed to fetch tags:", error)
      toast({
        title: "Error",
        description: "Failed to fetch tags",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleAddTag = () => {
    setTagName("")
    setEditingTag(null)
    setShowAddDialog(true)
  }

  const handleEditTag = (tag: Tag) => {
    setTagName(tag.name)
    setEditingTag(tag)
    setShowAddDialog(true)
  }

  const handleSaveTag = async () => {
    try {
      if (editingTag) {
        // Update existing tag
        toast({
          title: "Success",
          description: "Tag updated successfully",
        })
      } else {
        // Add new tag
        toast({
          title: "Success",
          description: "Tag added successfully",
        })
      }
      setShowAddDialog(false)
      fetchTags()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to save tag",
        variant: "destructive",
      })
    }
  }

  const handleDeleteTag = async (tag: Tag) => {
    try {
      toast({
        title: "Success",
        description: "Tag deleted successfully",
      })
      fetchTags()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete tag",
        variant: "destructive",
      })
    }
  }

  const filteredTags = tags.filter((tag) => tag.name.toLowerCase().includes(searchQuery.toLowerCase()))

  return (
    <AuthGuard requireAdmin>
      <div className="min-h-screen bg-background">
          <main className="container mx-auto px-4 py-8">
          <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
              <Link href="/admin">
                <Button variant="outline" size="sm">
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back to Dashboard
                </Button>
              </Link>
              <div>
                <h1 className="text-3xl font-bold">Tag Management</h1>
                <p className="text-muted-foreground">Manage novel tags and labels</p>
              </div>
            </div>

            {/* Tag Management */}
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>Tag Management</CardTitle>
                    <CardDescription>Manage tags used to categorize novels</CardDescription>
                  </div>
                  <Button onClick={handleAddTag}>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Tag
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {/* Search */}
                  <div className="flex items-center space-x-2">
                    <div className="relative flex-1 max-w-sm">
                      <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                      <Input
                        placeholder="Search tags..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="pl-8"
                      />
                    </div>
                    {searchQuery && (
                      <Button variant="outline" size="sm" onClick={() => setSearchQuery("")}>
                        Clear
                      </Button>
                    )}
                  </div>

                  {/* Tags Grid */}
                  {loading ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                      {Array.from({ length: 8 }).map((_, i) => (
                        <div key={i} className="border rounded-lg p-4 space-y-2">
                          <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                          <div className="h-3 w-16 bg-muted rounded animate-pulse" />
                          <div className="h-3 w-24 bg-muted rounded animate-pulse" />
                        </div>
                      ))}
                    </div>
                  ) : filteredTags.length === 0 ? (
                    <div className="text-center py-8">
                      <p className="text-muted-foreground">No tags found</p>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                      {filteredTags.map((tag) => (
                        <div key={tag.id} className="border rounded-lg p-4 space-y-2 hover:shadow-md transition-shadow">
                          <div className="flex items-center justify-between">
                            <Badge variant="secondary" className="text-sm">
                              {tag.name}
                            </Badge>
                            <DropdownMenu>
                              <DropdownMenuTrigger asChild>
                                <Button variant="ghost" size="icon" className="h-6 w-6">
                                  <MoreHorizontal className="h-3 w-3" />
                                </Button>
                              </DropdownMenuTrigger>
                              <DropdownMenuContent align="end">
                                <DropdownMenuItem onClick={() => handleEditTag(tag)}>
                                  <Edit className="mr-2 h-4 w-4" />
                                  Edit Tag
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={() => handleDeleteTag(tag)} className="text-destructive">
                                  <Trash2 className="mr-2 h-4 w-4" />
                                  Delete Tag
                                </DropdownMenuItem>
                              </DropdownMenuContent>
                            </DropdownMenu>
                          </div>
                          <p className="text-sm text-muted-foreground">{tag.novelCount} novels</p>
                          <p className="text-xs text-muted-foreground">
                            Created {new Date(tag.createdAt).toLocaleDateString()}
                          </p>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>
        </main>
      </div>

      {/* Add/Edit Tag Dialog */}
      <Dialog open={showAddDialog} onOpenChange={setShowAddDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingTag ? "Edit Tag" : "Add New Tag"}</DialogTitle>
            <DialogDescription>{editingTag ? "Update the tag name" : "Create a new tag for novels"}</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="tagName">Tag Name</Label>
              <Input
                id="tagName"
                value={tagName}
                onChange={(e) => setTagName(e.target.value)}
                placeholder="Enter tag name"
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowAddDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleSaveTag}>{editingTag ? "Update Tag" : "Add Tag"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </AuthGuard>
  )
}
