"use client"

import { useEffect, useRef } from "react"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { Bold, Italic, Underline, AlignLeft, AlignCenter, AlignRight, List } from "lucide-react"

interface RichTextEditorProps {
  content: string
  onChange: (content: string) => void
}

export default function RichTextEditor({ content, onChange }: RichTextEditorProps) {
  const editorRef = useRef<HTMLDivElement>(null)

  const execCommand = (command: string, value?: string) => {
    // document.execCommand is deprecated but still usable here for a simple editor
    // It mirrors existing project implementation.
    // Future: replace with a modern editor (TipTap, Quill, slate)
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
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
        <Button type="button" variant="ghost" size="sm" onClick={() => execCommand("underline")} className="h-8 w-8 p-0">
          <Underline className="h-4 w-4" />
        </Button>

        <Separator orientation="vertical" className="h-8" />

        <Button type="button" variant="ghost" size="sm" onClick={() => execCommand("justifyLeft")} className="h-8 w-8 p-0">
          <AlignLeft className="h-4 w-4" />
        </Button>
        <Button type="button" variant="ghost" size="sm" onClick={() => execCommand("justifyCenter")} className="h-8 w-8 p-0">
          <AlignCenter className="h-4 w-4" />
        </Button>
        <Button type="button" variant="ghost" size="sm" onClick={() => execCommand("justifyRight")} className="h-8 w-8 p-0">
          <AlignRight className="h-4 w-4" />
        </Button>

        <Separator orientation="vertical" className="h-8" />

        <Button type="button" variant="ghost" size="sm" onClick={() => execCommand("insertUnorderedList")} className="h-8 w-8 p-0">
          <List className="h-4 w-4" />
        </Button>

        <Separator orientation="vertical" className="h-8" />

        <Button type="button" variant="ghost" size="sm" onClick={() => execCommand("formatBlock", "h1")} className="h-8 px-2 text-xs font-bold">
          H1
        </Button>
        <Button type="button" variant="ghost" size="sm" onClick={() => execCommand("formatBlock", "h2")} className="h-8 px-2 text-xs font-bold">
          H2
        </Button>
        <Button type="button" variant="ghost" size="sm" onClick={() => execCommand("formatBlock", "h3")} className="h-8 px-2 text-xs font-bold">
          H3
        </Button>
        <Button type="button" variant="ghost" size="sm" onClick={() => execCommand("formatBlock", "p")} className="h-8 px-2 text-xs">
          P
        </Button>
      </div>

      {/* Editor */}
      <div ref={editorRef} contentEditable onInput={handleInput} className="min-h-[400px] p-4 focus:outline-none prose prose-sm max-w-none dark:prose-invert" style={{ whiteSpace: "pre-wrap" }} suppressContentEditableWarning={true} />
    </div>
  )
}
