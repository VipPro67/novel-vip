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

interface Genre {
  id: string
  name: string
  description: string
  novelCount: number
}

export default function AdminGenresPage() {
  const [genres, setGenres] = useState<Genre[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [showAddDialog, setShowAddDialog] = useState(false)
  const [editingGenre, setEditingGenre] = useState<Genre | null>(null)
  const [formData, setFormData] = useState({
    id: "",
    name: "",
    description: ""
  })
  const { toast } = useToast()

  useEffect(() => {
    fetchGenres()
  }, [])

  const fetchGenres = async () => {
    setLoading(true)
    try {
      const response = await api.getGenres()
      setGenres(response.data)
    } catch (error) {
      console.error("Failed to fetch genres:", error)
      toast({
        title: "Error",
        description: "Failed to fetch genres",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleAddGenre = () => {
    setFormData({ name: "", description: "", id: ""})
    setEditingGenre(null)
    setShowAddDialog(true)
  }

  const handleEditGenre = (genre: Genre) => {
    setFormData({
      name: genre.name,
      description: genre.description,
      id: genre.id
    })
    setEditingGenre(genre)
    setShowAddDialog(true)
  }

  const handleSaveGenre = async () => {
    try {
      if (editingGenre) {
        await api.updateGenre(formData.id,formData.name,formData.description)
        toast({
          title: "Success",
          description: "Genre updated successfully",
        })
      } else {
        // Add new genre
        await api.createGenre(formData.name,formData.description)
        toast({
          title: "Success",
          description: "Genre added successfully",
        })
      }
      setShowAddDialog(false)
      fetchGenres()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to save genre",
        variant: "destructive",
      })
    }
  }

  const filteredGenres = genres.filter(
    (genre) =>
      genre.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      genre.description.toLowerCase().includes(searchQuery.toLowerCase()),
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
                <h1 className="text-3xl font-bold">Genre Management</h1>
                <p className="text-muted-foreground">Manage novel genres and categories</p>
              </div>
            </div>

            {/* Genre Management */}
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>Genre Management</CardTitle>
                    <CardDescription>Manage genres and their properties</CardDescription>
                  </div>
                  <Button onClick={handleAddGenre}>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Genre
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
                        placeholder="Search genres..."
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

                  {/* Genres Table */}
                  <div className="border rounded-md">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Genre</TableHead>
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
                        ) : filteredGenres.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={5} className="text-center py-8">
                              No genres found
                            </TableCell>
                          </TableRow>
                        ) : (
                          filteredGenres.map((genre) => (
                            <TableRow key={genre.id}>
                              <TableCell>
                                <div className="flex items-center space-x-2">
                                  <div className="h-4 w-4 rounded" style={{ backgroundColor: genre.color }} />
                                  <span className="font-medium">{genre.name}</span>
                                </div>
                              </TableCell>
                              <TableCell>
                                <p className="text-sm text-muted-foreground line-clamp-2">{genre.description}</p>
                              </TableCell>
                              <TableCell>
                                <Badge variant="secondary">{genre.novelCount} novels</Badge>
                              </TableCell>
                              <TableCell>
                                <span className="text-sm text-muted-foreground">
                                  {new Date(genre.createdAt).toLocaleDateString()}
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
                                    <DropdownMenuItem onClick={() => handleEditGenre(genre)}>
                                      <Edit className="mr-2 h-4 w-4" />
                                      Edit Genre
                                    </DropdownMenuItem>
                                    <DropdownMenuSeparator />
                                    <DropdownMenuItem className="text-destructive">
                                      <Trash2 className="mr-2 h-4 w-4" />
                                      Delete Genre
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

      {/* Add/Edit Genre Dialog */}
      <Dialog open={showAddDialog} onOpenChange={setShowAddDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingGenre ? "Edit Genre" : "Add New Genre"}</DialogTitle>
            <DialogDescription>
              {editingGenre ? "Update the genre information" : "Create a new genre for novels"}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="name">Genre Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter genre name"
              />
            </div>
            <div>
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Enter genre description"
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowAddDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleSaveGenre}>{editingGenre ? "Update Genre" : "Add Genre"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </AuthGuard>
  )
}
