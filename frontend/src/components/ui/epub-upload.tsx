"use client"

import type React from "react"
import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Upload, X, FileText, Loader2 } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { api } from "@/lib/api"
import { useParams } from "next/navigation"

interface EpubUploadProps {
  onUploadSuccess?: (fileMetadata: any) => void
  onUploadError?: (error: Error) => void
}

export function EpubUpload({ onUploadSuccess, onUploadError }: EpubUploadProps) {
  const {params} = useParams();
  const { toast } = useToast()
  const [epubFile, setEpubFile] = useState<File | null>(null)
  const [epubPreview, setEpubPreview] = useState<string>("")
  const [uploading, setUploading] = useState(false)

  const handleEpubChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      // Validate EPUB file type
      if (!file.name.endsWith(".epub") && file.type !== "application/epub+zip") {
        toast({
          title: "Invalid file",
          description: "Please select a valid EPUB file",
          variant: "destructive",
        })
        return
      }

      setEpubFile(file)
      setEpubPreview(file.name)
    }
  }

  const handleUploadEpub = async () => {
    if (!epubFile) {
      toast({
        title: "No file selected",
        description: "Please select an EPUB file to upload",
        variant: "destructive",
      })
      return
    }

    setUploading(true)
    try {
      
      const response = await api.addChaptersFromEpub(epubFile)
      if (response.success) {
        toast({
          title: "Success",
          description: "EPUB file uploaded successfully. You will get notification about this shortly.",
        })
        onUploadSuccess?.(response.data)
        setEpubFile(null)
        setEpubPreview("")
      }
    } catch (error) {
      const err = error instanceof Error ? error : new Error("Upload failed")
      toast({
        title: "Upload failed",
        description: err.message,
        variant: "destructive",
      })
      onUploadError?.(err)
    } finally {
      setUploading(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>EPUB File</CardTitle>
        <CardDescription>Upload an EPUB file for the novel</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {epubPreview ? (
            <div className="relative">
              <div className="flex items-center gap-3 p-4 bg-blue-50 rounded-lg border border-blue-200">
                <FileText className="h-8 w-8 text-blue-600" />
                <div className="flex-1">
                  <p className="font-medium text-sm text-blue-900">{epubPreview}</p>
                  <p className="text-xs text-blue-700">
                    {epubFile && `${(epubFile.size / 1024 / 1024).toFixed(2)} MB`}
                  </p>
                </div>
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  onClick={() => {
                    setEpubFile(null)
                    setEpubPreview("")
                  }}
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            </div>
          ) : (
            <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
              <Upload className="mx-auto h-12 w-12 text-gray-400" />
              <p className="mt-2 text-sm text-gray-600">Upload EPUB file</p>
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
            <Button type="button" onClick={handleUploadEpub} disabled={uploading} className="w-full">
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
  )
}
