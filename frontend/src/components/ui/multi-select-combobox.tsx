"use client"

import * as React from "react"
import { Check, ChevronsUpDown, X } from "lucide-react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  Popover,
  PopoverTrigger,
  PopoverContent,
} from "@/components/ui/popover"
import {
  Command,
  CommandInput,
  CommandList,
  CommandItem,
  CommandEmpty,
  CommandGroup,
} from "@/components/ui/command"

type Option = { label: string; value: string }

interface MultiSelectComboboxProps {
  options: string[]
  value: string[]
  onChange: (next: string[]) => void
  placeholder?: string
  emptyText?: string
  allowCreate?: boolean
  label?: string
  className?: string
}

export function MultiSelectCombobox({
  options,
  value,
  onChange,
  placeholder = "Select…",
  emptyText = "No results",
  allowCreate = true,
  label,
  className,
}: MultiSelectComboboxProps) {
  const [open, setOpen] = React.useState(false)
  const [query, setQuery] = React.useState("")
  const opts: Option[] = React.useMemo(
    () => options.map(o => ({ label: o, value: o })),
    [options]
  )

  const toggle = (val: string) => {
    if (value.includes(val)) onChange(value.filter(v => v !== val))
    else onChange([...value, val])
  }

  const clearOne = (val: string) => onChange(value.filter(v => v !== val))
  const clearAll = () => onChange([])

  const canCreate =
    allowCreate &&
    query.trim().length > 0 &&
    !options.some(o => o.toLowerCase() === query.trim().toLowerCase())

  const createAndSelect = () => {
    const v = query.trim()
    if (!v) return
    onChange([...new Set([...value, v])])
    setQuery("")
    setOpen(false)
  }

  return (
    <div className={cn("space-y-2", className)}>
      {label && <div className="text-sm font-medium">{label}</div>}

      {/* Selected chips */}
      <div className="flex flex-wrap gap-2 min-h-10 p-2 rounded-md border bg-background">
        {value.length === 0 && (
          <span className="text-muted-foreground text-sm">{placeholder}</span>
        )}
        {value.map(v => (
          <Badge key={v} variant="secondary" className="flex items-center gap-1">
            {v}
            <button
              type="button"
              onClick={() => clearOne(v)}
              className="ml-1 hover:opacity-80"
              aria-label={`Remove ${v}`}
            >
              <X className="h-3 w-3" />
            </button>
          </Badge>
        ))}
        {value.length > 0 && (
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={clearAll}
            className="ml-auto h-7 px-2"
          >
            Clear
          </Button>
        )}
      </div>

      {/* Trigger */}
      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <Button
            type="button"
            variant="outline"
            role="combobox"
            aria-expanded={open}
            className="w-full justify-between"
          >
            Manage selection
            <ChevronsUpDown className="ml-2 h-4 w-4 opacity-50" />
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-[--radix-popover-trigger-width] p-0" align="start">
          <Command shouldFilter={false}>
            <CommandInput
              placeholder="Search…"
              value={query}
              onValueChange={setQuery}
            />
            <CommandList>
              <CommandEmpty>
                {canCreate ? (
                  <Button
                    type="button"
                    variant="ghost"
                    className="w-full justify-start"
                    onClick={createAndSelect}
                  >
                    Create “{query}”
                  </Button>
                ) : (
                  emptyText
                )}
              </CommandEmpty>
              <CommandGroup>
                {opts
                  .filter(o =>
                    query
                      ? o.label.toLowerCase().includes(query.toLowerCase())
                      : true
                  )
                  .map(o => {
                    const checked = value.includes(o.value)
                    return (
                      <CommandItem
                        key={o.value}
                        onSelect={() => toggle(o.value)}
                      >
                        <Check className={cn("mr-2 h-4 w-4", checked ? "opacity-100" : "opacity-0")} />
                        {o.label}
                      </CommandItem>
                    )
                  })}
              </CommandGroup>
            </CommandList>
          </Command>
        </PopoverContent>
      </Popover>
    </div>
  )
}
