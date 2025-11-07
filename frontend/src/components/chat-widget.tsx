"use client"

import { useState, useEffect } from "react"
import { X, Send, Paperclip, Smile, Plus, MessageSquare, Users, User, Phone, Video, Info } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { ScrollArea } from "@/components/ui/scroll-area"
import { useChat } from "@/hooks/use-chat"
import { useToast } from "@/hooks/use-toast"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"

export function ChatWidget() {
  const [isOpen, setIsOpen] = useState(false)
  const [activeGroupId, setActiveGroupId] = useState<string | null>(null)
  const [chatTab, setChatTab] = useState<"groups" | "users">("groups")
  const [messageInput, setMessageInput] = useState("")
  const [showCreateGroup, setShowCreateGroup] = useState(false)
  const [newGroupName, setNewGroupName] = useState("")
  const [newGroupDescription, setNewGroupDescription] = useState("")
  const [searchQuery, setSearchQuery] = useState("")

  const { groups, messages, loading, fetchGroups, fetchGroupMessages, sendMessage, createGroup } = useChat()
  const { toast } = useToast()

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
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="fixed bottom-6 right-6 z-40 w-14 h-14 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 shadow-lg hover:shadow-xl transition-all hover:scale-110 flex items-center justify-center text-white border border-blue-400"
      >
        {isOpen ? (
          <X size={24} />
        ) : (
          <div className="relative">
            <MessageSquare size={24} />
            {unreadCount > 0 && (
              <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">
                {Math.min(unreadCount, 9)}
              </span>
            )}
          </div>
        )}
      </button>

      {isOpen && (
        <div className="fixed bottom-24 right-6 z-40 w-full max-w-4xl h-[600px] bg-slate-900 rounded-2xl shadow-2xl flex border border-slate-700 overflow-hidden">
          {/* Left Sidebar - Conversations */}
          <div className="w-72 border-r border-slate-700 flex flex-col bg-slate-900">
            {/* Header */}
            <div className="p-4 border-b border-slate-700">
              <h3 className="text-white font-semibold mb-3">Messages</h3>
              <Tabs value={chatTab} onValueChange={(v) => setChatTab(v as "groups" | "users")} className="w-full">
                <TabsList className="grid w-full grid-cols-2 bg-slate-800 border border-slate-700">
                  <TabsTrigger value="groups" className="flex items-center gap-2 text-xs">
                    <Users size={14} />
                    Groups
                  </TabsTrigger>
                  <TabsTrigger value="users" className="flex items-center gap-2 text-xs">
                    <User size={14} />
                    Chats
                  </TabsTrigger>
                </TabsList>
              </Tabs>
            </div>

            {/* Search */}
            <div className="p-3 border-b border-slate-700">
              <Input
                placeholder={chatTab === "groups" ? "Search groups..." : "Search chats..."}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="bg-slate-800 border-slate-700 text-white placeholder-slate-500 h-9"
              />
            </div>

            {/* Conversations List */}
            <ScrollArea className="flex-1">
              {chatTab === "groups" && (
                <div className="p-2 space-y-1">
                  {loading && <div className="p-4 text-center text-slate-400 text-sm">Loading groups...</div>}
                  {filteredGroups.length === 0 && !loading && (
                    <div className="p-4 text-center text-slate-400 text-sm">No groups. Create one to get started.</div>
                  )}
                  {filteredGroups.map((group) => (
                    <button
                      key={group.id}
                      onClick={() => setActiveGroupId(group.id)}
                      className={`w-full p-3 rounded-lg transition-colors text-left ${
                        activeGroupId === group.id ? "bg-blue-600" : "hover:bg-slate-800"
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center flex-shrink-0 text-white text-sm font-bold">
                          <Users size={16} />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-white text-sm font-medium truncate">{group.name}</p>
                          <p className="text-slate-400 text-xs truncate">{group.memberCount} members</p>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              )}

              {chatTab === "users" && (
                <div className="p-4 text-center text-slate-400 text-sm">Direct messaging feature coming soon</div>
              )}
            </ScrollArea>

            {/* Create Group Button */}
            {chatTab === "groups" && (
              <div className="p-3 border-t border-slate-700">
                <Button
                  onClick={() => setShowCreateGroup(true)}
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white flex items-center gap-2"
                >
                  <Plus size={16} />
                  New Group
                </Button>
              </div>
            )}
          </div>

          {/* Right Content Area */}
          <div className="flex-1 flex flex-col bg-slate-850">
            {activeGroup ? (
              <>
                <div className="bg-gradient-to-r from-slate-800 to-slate-900 p-4 border-b border-slate-700 flex items-center justify-between">
                  <div>
                    <h3 className="text-white font-semibold flex items-center gap-2">
                      <Users size={18} />
                      {activeGroup.name}
                    </h3>
                    <p className="text-slate-400 text-xs mt-1">{activeGroup.memberCount} members</p>
                  </div>
                  <div className="flex gap-2">
                    <button className="p-2 hover:bg-slate-700 rounded-lg transition-colors" title="Voice call">
                      <Phone size={18} className="text-slate-300" />
                    </button>
                    <button className="p-2 hover:bg-slate-700 rounded-lg transition-colors" title="Video call">
                      <Video size={18} className="text-slate-300" />
                    </button>
                    <button className="p-2 hover:bg-slate-700 rounded-lg transition-colors" title="Group info">
                      <Info size={18} className="text-slate-300" />
                    </button>
                    <button
                      onClick={() => setActiveGroupId(null)}
                      className="p-2 hover:bg-slate-700 rounded-lg transition-colors"
                    >
                      <X size={18} className="text-slate-300" />
                    </button>
                  </div>
                </div>

                {/* Messages Area */}
                <ScrollArea className="flex-1 p-4">
                  <div className="space-y-4">
                    {messages.length === 0 && (
                      <div className="flex items-center justify-center h-full text-slate-400">
                        No messages yet. Start the conversation!
                      </div>
                    )}
                    {messages.map((msg) => (
                      <div key={msg.id} className="flex gap-3 animate-in fade-in">
                        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex-shrink-0 flex items-center justify-center text-white text-xs font-bold">
                          {msg.senderName.charAt(0).toUpperCase()}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            <p className="text-white text-sm font-medium">{msg.senderName}</p>
                            <span className="text-slate-500 text-xs">{msg.timestamp}</span>
                          </div>
                          <p
                            className={`text-sm mt-1 break-words ${
                              msg.isSystem ? "text-yellow-400 italic" : "text-slate-300"
                            }`}
                          >
                            {msg.content}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                </ScrollArea>

                {/* Message Input */}
                <div className="p-4 border-t border-slate-700 bg-slate-800">
                  <div className="flex gap-2 items-end">
                    <button className="p-2 hover:bg-slate-700 rounded-lg transition-colors flex-shrink-0">
                      <Paperclip size={18} className="text-slate-400" />
                    </button>
                    <button className="p-2 hover:bg-slate-700 rounded-lg transition-colors flex-shrink-0">
                      <Smile size={18} className="text-slate-400" />
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
                      className="bg-slate-700 border-slate-600 text-white placeholder-slate-500 flex-1 h-9"
                    />
                    <Button
                      size="sm"
                      onClick={handleSendMessage}
                      disabled={!messageInput.trim()}
                      className="flex-shrink-0 bg-blue-600 hover:bg-blue-700"
                    >
                      <Send size={16} />
                    </Button>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex-1 flex items-center justify-center text-slate-400">
                <div className="text-center">
                  <MessageSquare size={48} className="mx-auto mb-4 opacity-50" />
                  <p className="text-lg font-medium">Select a conversation</p>
                  <p className="text-sm">Choose a group or chat to start messaging</p>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      <Dialog open={showCreateGroup} onOpenChange={setShowCreateGroup}>
        <DialogContent className="bg-slate-900 border-slate-700">
          <DialogHeader>
            <DialogTitle className="text-white">Create New Group</DialogTitle>
            <DialogDescription className="text-slate-400">
              Start a new group conversation with your community
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <label className="text-sm text-slate-300 block mb-2">Group Name</label>
              <Input
                placeholder="Enter group name"
                value={newGroupName}
                onChange={(e) => setNewGroupName(e.target.value)}
                className="bg-slate-800 border-slate-700 text-white"
              />
            </div>
            <div>
              <label className="text-sm text-slate-300 block mb-2">Description (Optional)</label>
              <Input
                placeholder="What is this group about?"
                value={newGroupDescription}
                onChange={(e) => setNewGroupDescription(e.target.value)}
                className="bg-slate-800 border-slate-700 text-white"
              />
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setShowCreateGroup(false)}>
                Cancel
              </Button>
              <Button
                onClick={handleCreateGroup}
                disabled={!newGroupName.trim()}
                className="bg-blue-600 hover:bg-blue-700"
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
