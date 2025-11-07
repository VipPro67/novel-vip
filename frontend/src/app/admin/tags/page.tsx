"use client"

import { useState, useEffect } from "react"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
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
import { Textarea } from "@/components/ui/textarea"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { api } from "@/lib/api"

interface Tag {
  id: string
  name: string
  description: string
  novelCount: number
}

export default function AdminTagsPage() {
  const [tags, setTags] = useState<Tag[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [showAddDialog, setShowAddDialog] = useState(false)
  const [editingTag, setEditingTag] = useState<Tag | null>(null)
  const [formData, setFormData] = useState({
    id: "",
    name: "",
    description: ""
  })
  const { toast } = useToast()

  useEffect(() => {
    fetchTags()
  }, [])

  const fetchTags = async () => {
    setLoading(true)
    try {
      const response = await api.getTags()
      setTags(response.data)
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
    setFormData({ name: "", description: "", id: ""})
    setEditingTag(null)
    setShowAddDialog(true)
  }

  const handleEditTag = (tag: Tag) => {
    setFormData({
      name: tag.name,
      description: tag.description,
      id: tag.id
    })
    setEditingTag(tag)
    setShowAddDialog(true)
  }

  const handleSaveTag = async () => {
    try {
      if (editingTag) {
        await api.updateTag(formData.id,formData.name,formData.description)
        toast({
          title: "Success",
          description: "Tag updated successfully",
        })
      } else {
        // Add new tag
        await api.createTag(formData.name,formData.description)
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

  const filteredTags = tags.filter(
    (tag) =>
      tag.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      tag.description.toLowerCase().includes(searchQuery.toLowerCase()),
  )

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
                <p className="text-muted-foreground">Manage novel tags and categories</p>
              </div>
            </div>

            {/* Tag Management */}
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>Tag Management</CardTitle>
                    <CardDescription>Manage tags and their properties</CardDescription>
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

                  {/* Tags Table */}
                  <div className="border rounded-md">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Tag</TableHead>
                          <TableHead>Description</TableHead>
                          <TableHead>Novel Count</TableHead>
                          <TableHead>Created</TableHead>
                          <TableHead className="w-[70px]">Actions</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {loading ? (
                          Array.from({ length: 5 }).map((_, i) => (
                            <TableRow key={i}>
                              <TableCell>
                                <div className="flex items-center space-x-2">
                                  <div className="h-4 w-4 bg-muted rounded animate-pulse" />
                                  <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                                </div>
                              </TableCell>
                              <TableCell>
                                <div className="h-4 w-40 bg-muted rounded animate-pulse" />
                              </TableCell>
                              <TableCell>
                                <div className="h-4 w-12 bg-muted rounded animate-pulse" />
                              </TableCell>
                              <TableCell>
                                <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                              </TableCell>
                              <TableCell>
                                <div className="h-4 w-8 bg-muted rounded animate-pulse" />
                              </TableCell>
                            </TableRow>
                          ))
                        ) : filteredTags.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={5} className="text-center py-8">
                              No tags found
                            </TableCell>
                          </TableRow>
                        ) : (
                          filteredTags.map((tag) => (
                            <TableRow key={tag.id}>
                              <TableCell>
                                <div className="flex items-center space-x-2">
                                  <div className="h-4 w-4 rounded" style={{ backgroundColor: tag.color }} />
                                  <span className="font-medium">{tag.name}</span>
                                </div>
                              </TableCell>
                              <TableCell>
                                <p className="text-sm text-muted-foreground line-clamp-2">{tag.description}</p>
                              </TableCell>
                              <TableCell>
                                <Badge variant="secondary">{tag.novelCount} novels</Badge>
                              </TableCell>
                              <TableCell>
                                <span className="text-sm text-muted-foreground">
                                  {new Date(tag.createdAt).toLocaleDateString()}
                                </span>
                              </TableCell>
                              <TableCell>
                                <DropdownMenu>
                                  <DropdownMenuTrigger asChild>
                                    <Button variant="ghost" size="icon">
                                      <MoreHorizontal className="h-4 w-4" />
                                    </Button>
                                  </DropdownMenuTrigger>
                                  <DropdownMenuContent align="end">
                                    <DropdownMenuItem onClick={() => handleEditTag(tag)}>
                                      <Edit className="mr-2 h-4 w-4" />
                                      Edit Tag
                                    </DropdownMenuItem>
                                    <DropdownMenuSeparator />
                                    <DropdownMenuItem className="text-destructive">
                                      <Trash2 className="mr-2 h-4 w-4" />
                                      Delete Tag
                                    </DropdownMenuItem>
                                  </DropdownMenuContent>
                                </DropdownMenu>
                              </TableCell>
                            </TableRow>
                          ))
                        )}
                      </TableBody>
                    </Table>
                  </div>
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
            <DialogDescription>
              {editingTag ? "Update the tag information" : "Create a new tag for novels"}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="name">Tag Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter tag name"
              />
            </div>
            <div>
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Enter tag description"
                rows={3}
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
