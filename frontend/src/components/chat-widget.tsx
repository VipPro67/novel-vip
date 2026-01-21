"use client"

import { useState, useEffect } from "react"
import { X, Send, Paperclip, Smile, Plus, MessageSquare, Users, User } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { ScrollArea } from "@/components/ui/scroll-area"
import { useChat } from "@/hooks/use-chat"
import { useToast } from "@/hooks/use-toast"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"

export function ChatWidget() {
  const [isOpen, setIsOpen] = useState(false)
  const [isChatEnabled, setIsChatEnabled] = useState(false)
  const [activeGroupId, setActiveGroupId] = useState<string | null>(null)
  const [chatTab, setChatTab] = useState<"groups" | "users">("groups")
  const [messageInput, setMessageInput] = useState("")
  const [showCreateGroup, setShowCreateGroup] = useState(false)
  const [newGroupName, setNewGroupName] = useState("")
  const [newGroupDescription, setNewGroupDescription] = useState("")
  const [searchQuery, setSearchQuery] = useState("")

  const { groups, messages, loading, fetchGroups, fetchGroupMessages, sendMessage, createGroup } = useChat()
  const { toast } = useToast()

  // Load chat enabled setting from localStorage
  useEffect(() => {
    if (typeof window !== "undefined") {
      const chatEnabled = localStorage.getItem("chatWidgetEnabled") === "true"
      setIsChatEnabled(chatEnabled)
    }
  }, [])

  useEffect(() => {
    if (isOpen && groups.length === 0) {
      fetchGroups()
    }
  }, [isOpen, groups.length, fetchGroups])

  useEffect(() => {
    if (activeGroupId) {
      fetchGroupMessages(activeGroupId)
    }
  }, [activeGroupId, fetchGroupMessages])

  const activeGroup = groups.find((g) => g.id === activeGroupId)
  const filteredGroups = groups.filter((g) => g.name.toLowerCase().includes(searchQuery.toLowerCase()))
  const unreadCount = groups.reduce((acc, g) => acc + (g.memberCount > 0 ? 1 : 0), 0)

  const handleSendMessage = async () => {
    if (!messageInput.trim() || !activeGroupId) return
    await sendMessage(activeGroupId, messageInput)
    setMessageInput("")
  }

  const handleCreateGroup = async () => {
    if (!newGroupName.trim()) return
    const group = await createGroup(newGroupName, newGroupDescription)
    if (group) {
      setShowCreateGroup(false)
      setNewGroupName("")
      setNewGroupDescription("")
      setActiveGroupId(group.id)
    }
  }

  return (
    <>
      {isChatEnabled && (
        <button
          onClick={() => setIsOpen(!isOpen)}
          className="fixed bottom-4 right-4 z-40 w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 via-blue-600 to-purple-600 shadow-2xl hover:shadow-purple-500/50 transition-all hover:scale-125 flex items-center justify-center text-white border border-blue-400/50 hover:border-blue-300 backdrop-blur-sm"
          title="Messages"
        >
          {isOpen ? (
            <X size={18} className="transition-transform" />
          ) : (
            <div className="relative">
              <MessageSquare size={18} className="animate-pulse" />
              {unreadCount > 0 && (
                <span className="absolute -top-2 -right-2 bg-gradient-to-r from-red-500 to-red-600 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center font-bold text-[10px] shadow-lg animate-bounce">
                  {Math.min(unreadCount, 9)}
                </span>
              )}
            </div>
          )}
        </button>
      )}

      {isOpen && isChatEnabled && (
        <div className="fixed bottom-20 right-6 z-40 w-full max-w-2xl h-[500px] bg-gradient-to-b from-slate-800 to-slate-900 rounded-2xl shadow-2xl flex border border-slate-700/50 overflow-hidden backdrop-blur-xl">
          {/* Left Sidebar - Conversations */}
          <div className="w-64 border-r border-slate-700/50 flex flex-col bg-gradient-to-b from-slate-800/80 to-slate-900/80">
            {/* Header */}
            <div className="p-4 border-b border-slate-700/50 bg-gradient-to-r from-blue-600/20 to-purple-600/20 backdrop-blur">
              <h3 className="text-white font-bold mb-3 text-base flex items-center gap-2">
                <MessageSquare size={16} className="text-blue-400" />
                Messages
              </h3>
              <Tabs value={chatTab} onValueChange={(v) => setChatTab(v as "groups" | "users")} className="w-full">
                <TabsList className="grid w-full grid-cols-2 bg-slate-900/80 border border-slate-700 rounded-lg">
                  <TabsTrigger value="groups" className="flex items-center gap-2 text-xs font-medium">
                    <Users size={14} />
                    Groups
                  </TabsTrigger>
                  <TabsTrigger value="users" className="flex items-center gap-2 text-xs font-medium">
                    <User size={14} />
                    Chats
                  </TabsTrigger>
                </TabsList>
              </Tabs>
            </div>

            {/* Search */}
            <div className="p-3 border-b border-slate-700/50">
              <Input
                placeholder={chatTab === "groups" ? "🔍 Search groups..." : "🔍 Search chats..."}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="bg-slate-700/60 border-slate-600/50 text-white placeholder-slate-400 h-9 rounded-lg focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
              />
            </div>

            {/* Conversations List */}
            <ScrollArea className="flex-1">
              {chatTab === "groups" && (
                <div className="p-2 space-y-1">
                  {loading && (
                    <div className="p-4 text-center text-slate-400 text-sm animate-pulse">
                      <div className="inline-block">Loading groups...</div>
                    </div>
                  )}
                  {filteredGroups.length === 0 && !loading && (
                    <div className="p-4 text-center text-slate-400 text-sm">
                      <MessageSquare size={32} className="mx-auto mb-2 opacity-40" />
                      No groups yet
                    </div>
                  )}
                  {filteredGroups.map((group) => (
                    <button
                      key={group.id}
                      onClick={() => setActiveGroupId(group.id)}
                      className={`w-full p-3 rounded-xl transition-all duration-200 text-left group ${
                        activeGroupId === group.id
                          ? "bg-gradient-to-r from-blue-600 to-blue-700 shadow-lg shadow-blue-500/20"
                          : "hover:bg-slate-700/40 hover:shadow-md hover:shadow-slate-900/50"
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center flex-shrink-0 text-white text-sm font-bold transition-transform group-hover:scale-110 ${activeGroupId === group.id ? "scale-110 shadow-lg shadow-blue-500/50" : ""}`}>
                          {group.name.charAt(0).toUpperCase()}
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-white text-sm font-semibold truncate">{group.name}</p>
                          <p className="text-slate-400 text-xs truncate">{group.memberCount} members</p>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              )}

              {chatTab === "users" && (
                <div className="p-4 text-center text-slate-400 text-sm">
                  <User size={32} className="mx-auto mb-2 opacity-40" />
                  Coming soon
                </div>
              )}
            </ScrollArea>

            {/* Create Group Button */}
            {chatTab === "groups" && (
              <div className="p-3 border-t border-slate-700/50 bg-gradient-to-r from-blue-600/10 to-purple-600/10 backdrop-blur">
                <Button
                  onClick={() => setShowCreateGroup(true)}
                  className="w-full bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white flex items-center gap-2 shadow-lg hover:shadow-blue-500/40 transition-all"
                >
                  <Plus size={16} />
                  New Group
                </Button>
              </div>
            )}
          </div>

          {/* Right Content Area */}
          <div className="flex-1 flex flex-col bg-gradient-to-b from-slate-800/50 to-slate-900/50">
            {activeGroup ? (
              <>
                <div className="bg-gradient-to-r from-slate-800/60 to-slate-900/60 p-4 border-b border-slate-700/50 flex items-center justify-between backdrop-blur-sm">
                  <div>
                    <h3 className="text-white font-bold flex items-center gap-2 text-base">
                      <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white text-xs font-bold">
                        {activeGroup.name.charAt(0).toUpperCase()}
                      </div>
                      {activeGroup.name}
                    </h3>
                    <p className="text-slate-400 text-xs mt-1">👥 {activeGroup.memberCount} members</p>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => setActiveGroupId(null)}
                      className="p-2 hover:bg-slate-700/60 rounded-lg transition-all hover:scale-110"
                    >
                      <X size={18} className="text-slate-300" />
                    </button>
                  </div>
                </div>

                {/* Messages Area */}
                <ScrollArea className="flex-1 p-4">
                  <div className="space-y-3">
                    {messages.length === 0 && (
                      <div className="flex items-center justify-center h-full text-slate-400">
                        <div className="text-center">
                          <MessageSquare size={40} className="mx-auto mb-2 opacity-40" />
                          <p className="text-sm">No messages yet</p>
                        </div>
                      </div>
                    )}
                    {messages.map((msg, idx) => (
                      <div key={msg.id} className="flex gap-3 animate-in fade-in slide-in-from-bottom-2 duration-300" style={{ animationDelay: `${idx * 50}ms` }}>
                        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex-shrink-0 flex items-center justify-center text-white text-xs font-bold shadow-lg">
                          {msg.sender.username.charAt(0).toUpperCase()}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            <p className="text-white text-sm font-semibold">{msg.sender.username}</p>
                            <span className="text-slate-500 text-xs">{msg.timestamp}</span>
                          </div>
                          <div className={`text-sm mt-1 break-words p-2 rounded-lg ${
                            msg.isSystem
                              ? "text-yellow-300 italic bg-yellow-500/10 border border-yellow-500/20"
                              : "text-slate-200 bg-slate-700/30 border border-slate-600/30"
                          }`}>
                            {msg.content}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </ScrollArea>

                {/* Message Input */}
                <div className="p-4 border-t border-slate-700/50 bg-gradient-to-r from-slate-800/80 to-slate-900/80 backdrop-blur-sm">
                  <div className="flex gap-2 items-end">
                    <button className="p-2 hover:bg-slate-700/60 rounded-lg transition-all hover:scale-110 text-slate-400 hover:text-slate-200">
                      <Paperclip size={18} />
                    </button>
                    <button className="p-2 hover:bg-slate-700/60 rounded-lg transition-all hover:scale-110 text-slate-400 hover:text-slate-200">
                      <Smile size={18} />
                    </button>
                    <Input
                      placeholder="Type a message..."
                      value={messageInput}
                      onChange={(e) => setMessageInput(e.target.value)}
                      onKeyPress={(e) => {
                        if (e.key === "Enter" && messageInput.trim()) {
                          handleSendMessage()
                        }
                      }}
                      className="bg-slate-700/60 border-slate-600/50 text-white placeholder-slate-400 flex-1 h-9 rounded-lg focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                    />
                    <Button
                      size="sm"
                      onClick={handleSendMessage}
                      disabled={!messageInput.trim()}
                      className="flex-shrink-0 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white shadow-lg hover:shadow-blue-500/40 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <Send size={16} />
                    </Button>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex-1 flex items-center justify-center text-slate-400">
                <div className="text-center">
                  <div className="w-16 h-16 rounded-full bg-gradient-to-br from-blue-600/20 to-purple-600/20 flex items-center justify-center mx-auto mb-4">
                    <MessageSquare size={32} className="opacity-50" />
                  </div>
                  <p className="text-lg font-semibold text-white">Select a conversation</p>
                  <p className="text-sm text-slate-400 mt-1">Choose a group to start messaging</p>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      <Dialog open={showCreateGroup} onOpenChange={setShowCreateGroup}>
        <DialogContent className="bg-gradient-to-b from-slate-800 to-slate-900 border-slate-700/50 shadow-2xl">
          <DialogHeader>
            <DialogTitle className="text-white text-xl font-bold flex items-center gap-2">
              <Plus size={20} className="text-blue-400" />
              Create New Group
            </DialogTitle>
            <DialogDescription className="text-slate-400">
              Start a new group conversation with your community
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <label className="text-sm text-slate-300 block mb-2 font-semibold">Group Name *</label>
              <Input
                placeholder="Enter group name"
                value={newGroupName}
                onChange={(e) => setNewGroupName(e.target.value)}
                className="bg-slate-700/60 border-slate-600/50 text-white placeholder-slate-400 rounded-lg focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="text-sm text-slate-300 block mb-2 font-semibold">Description</label>
              <Input
                placeholder="What is this group about?"
                value={newGroupDescription}
                onChange={(e) => setNewGroupDescription(e.target.value)}
                className="bg-slate-700/60 border-slate-600/50 text-white placeholder-slate-400 rounded-lg focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
              />
            </div>
            <div className="flex gap-2 justify-end pt-2">
              <Button variant="outline" onClick={() => setShowCreateGroup(false)} className="border-slate-600/50 hover:bg-slate-700/50">
                Cancel
              </Button>
              <Button
                onClick={handleCreateGroup}
                disabled={!newGroupName.trim()}
                className="bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white shadow-lg hover:shadow-blue-500/40 transition-all disabled:opacity-50"
              >
                Create Group
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </>
  )
}
