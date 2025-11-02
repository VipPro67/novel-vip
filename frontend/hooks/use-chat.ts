"use client"

import { useState, useCallback } from "react"
import { api } from "@/lib/api"
import { useToast } from "@/hooks/use-toast"

export interface ChatMessage {
  id: string
  content: string
  senderId: string
  senderName: string
  senderAvatar?: string
  timestamp: string
  isSystem?: boolean
  edited?: boolean
}

export interface ChatGroup {
  id: string
  name: string
  description?: string
  avatar?: string
  memberCount: number
  lastMessage?: string
  lastMessageTime?: string
  isPrivate: boolean
  createdAt: string
}

export function useChat() {
  const [groups, setGroups] = useState<ChatGroup[]>([])
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { toast } = useToast()

  const fetchGroups = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const response = await api.getAllGroups({ page: 0, size: 50 })
      if (response.success && response.data) {
        setGroups(response.data.content || [])
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "Failed to fetch groups"
      setError(errorMessage)
      console.error("[v0] Error fetching groups:", errorMessage)
    } finally {
      setLoading(false)
    }
  }, [])

  const fetchGroupMessages = useCallback(async (groupId: string) => {
    try {
      setLoading(true)
      setError(null)
      const response = await api.getMessagesByGroup(groupId, { page: 0, size: 100 })
      if (response.success && response.data) {
        setMessages(response.data.content || [])
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "Failed to fetch messages"
      setError(errorMessage)
      console.error("[v0] Error fetching messages:", errorMessage)
    } finally {
      setLoading(false)
    }
  }, [])

  const sendMessage = useCallback(
    async (groupId: string, content: string) => {
      try {
        setError(null)
        // This would call the actual send message API
        // For now, we'll add it optimistically
        const newMessage: ChatMessage = {
          id: Date.now().toString(),
          content,
          senderId: "current-user",
          senderName: "You",
          timestamp: new Date().toLocaleTimeString(),
        }
        setMessages((prev) => [...prev, newMessage])
        toast({ title: "Message sent" })
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "Failed to send message"
        setError(errorMessage)
        toast({ title: "Error", description: errorMessage, variant: "destructive" })
      }
    },
    [toast],
  )

  const createGroup = useCallback(
    async (name: string, description?: string) => {
      try {
        setError(null)
        // This would call the actual create group API
        const newGroup: ChatGroup = {
          id: Date.now().toString(),
          name,
          description,
          memberCount: 1,
          isPrivate: false,
          createdAt: new Date().toISOString(),
        }
        setGroups((prev) => [newGroup, ...prev])
        toast({ title: "Group created successfully" })
        return newGroup
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "Failed to create group"
        setError(errorMessage)
        toast({ title: "Error", description: errorMessage, variant: "destructive" })
      }
    },
    [toast],
  )

  return {
    groups,
    messages,
    loading,
    error,
    fetchGroups,
    fetchGroupMessages,
    sendMessage,
    createGroup,
  }
}
