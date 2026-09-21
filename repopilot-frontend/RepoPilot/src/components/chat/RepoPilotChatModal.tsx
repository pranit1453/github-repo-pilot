import React, { useState, useEffect, useRef } from 'react';
import { chatService } from '../../services/api/chatService';
import type { ChatSession, ChatMessage } from '../../services/api/chatService';
import {
  Plus,
  Send,
  Loader2,
  X,
  Bot,
  User,
  FileCode,
  Sparkles,
  ChevronRight,
  Code2
} from 'lucide-react';
import { Button } from '../ui/button';

interface RepoPilotChatModalProps {
  repositoryId: string | null;
  repositoryName: string | null;
  isOpen: boolean;
  onClose: () => void;
}

export const RepoPilotChatModal: React.FC<RepoPilotChatModalProps> = ({
  repositoryId,
  repositoryName,
  isOpen,
  onClose,
}) => {
  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [activeSessionId, setActiveSessionId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputQuery, setInputQuery] = useState<string>('');
  const [isSessionsLoading, setIsSessionsLoading] = useState<boolean>(false);
  const [isMessagesLoading, setIsMessagesLoading] = useState<boolean>(false);
  const [isSending, setIsSending] = useState<boolean>(false);
  const [streamError, setStreamError] = useState<string | null>(null);

  const chatBottomRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    chatBottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // Fetch chat sessions when modal opens or repositoryId changes
  useEffect(() => {
    if (isOpen && repositoryId) {
      loadSessions(repositoryId);
    }
  }, [isOpen, repositoryId]);

  // Fetch messages when active session changes
  useEffect(() => {
    if (activeSessionId) {
      loadMessages(activeSessionId);
    } else {
      setMessages([]);
    }
  }, [activeSessionId]);

  useEffect(() => {
    scrollToBottom();
  }, [messages, isSending]);

  const loadSessions = async (repoId: string) => {
    try {
      setIsSessionsLoading(true);
      const data = await chatService.getSessions(repoId);
      setSessions(data || []);
      if (data && data.length > 0) {
        setActiveSessionId(data[0].id);
      } else {
        // Automatically create initial session if none exists
        await handleCreateSession(repoId);
      }
    } catch (err) {
      console.error('Failed to load chat sessions:', err);
    } finally {
      setIsSessionsLoading(false);
    }
  };

  const loadMessages = async (sessionId: string) => {
    try {
      setIsMessagesLoading(true);
      const data = await chatService.getMessages(sessionId);
      setMessages(data || []);
    } catch (err) {
      console.error('Failed to load chat messages:', err);
    } finally {
      setIsMessagesLoading(false);
    }
  };

  const handleCreateSession = async (repoId?: string) => {
    const targetRepoId = repoId || repositoryId;
    if (!targetRepoId) return;

    try {
      const newSession = await chatService.createSession(targetRepoId, `Chat ${new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`);
      setSessions((prev) => [newSession, ...prev]);
      setActiveSessionId(newSession.id);
      setMessages([]);
    } catch (err) {
      console.error('Failed to create chat session:', err);
    }
  };

  const handleSendMessage = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!inputQuery.trim() || !activeSessionId || isSending) return;

    const userText = inputQuery.trim();
    setInputQuery('');
    setStreamError(null);
    setIsSending(true);

    // Optimistically push user message to UI thread
    const tempUserMsg: ChatMessage = {
      id: `temp-${Date.now()}`,
      role: 'USER',
      content: userText,
      citations: [],
      createdAt: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, tempUserMsg]);

    try {
      const assistantMsg = await chatService.sendMessage(activeSessionId, userText);
      setMessages((prev) => [...prev, assistantMsg]);
    } catch (err: any) {
      console.error('Failed to send message:', err);
      setStreamError(err.message || 'Failed to communicate with AI server.');
    } finally {
      setIsSending(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-md p-2 sm:p-4 animate-in fade-in duration-200">
      <div className="relative w-full max-w-5xl h-[85vh] rounded-2xl border border-border/80 bg-card shadow-2xl flex flex-col md:flex-row overflow-hidden">
        {/* Left Sidebar: Sessions list */}
        <div className="w-full md:w-64 border-b md:border-b-0 md:border-r border-border/60 bg-muted/20 p-4 flex flex-col justify-between shrink-0">
          <div className="space-y-4 flex-1 min-h-0 flex flex-col">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Sparkles className="size-4 text-primary" />
                <h3 className="text-sm font-bold text-foreground">Chat Sessions</h3>
              </div>
              <Button
                size="sm"
                variant="outline"
                onClick={() => handleCreateSession()}
                className="size-7 p-0 rounded-lg cursor-pointer"
                title="New Chat Session"
              >
                <Plus className="size-4" />
              </Button>
            </div>

            {/* Sessions Scrollable List */}
            <div className="flex-1 overflow-y-auto space-y-1.5 pr-1 custom-scrollbar">
              {isSessionsLoading ? (
                <div className="p-4 text-center text-xs text-muted-foreground">Loading sessions...</div>
              ) : sessions.length === 0 ? (
                <div className="p-4 text-center text-xs text-muted-foreground">No active sessions.</div>
              ) : (
                sessions.map((s) => (
                  <button
                    key={s.id}
                    onClick={() => setActiveSessionId(s.id)}
                    className={`w-full text-left p-2.5 rounded-xl text-xs font-medium transition-all flex items-center justify-between group cursor-pointer ${
                      activeSessionId === s.id
                        ? 'bg-primary text-primary-foreground font-semibold shadow-xs'
                        : 'text-muted-foreground hover:text-foreground hover:bg-muted/50'
                    }`}
                  >
                    <span className="truncate flex-1">{s.title || 'Chat Session'}</span>
                    <ChevronRight className={`size-3.5 opacity-0 group-hover:opacity-100 transition-opacity ${activeSessionId === s.id ? 'opacity-100' : ''}`} />
                  </button>
                ))
              )}
            </div>
          </div>

          <div className="pt-3 border-t border-border/40 text-[11px] text-muted-foreground font-mono truncate">
            Repository: <strong className="text-foreground">{repositoryName}</strong>
          </div>
        </div>

        {/* Right Main Chat Thread Area */}
        <div className="flex-1 flex flex-col min-w-0 bg-background/50">
          {/* Header */}
          <div className="p-4 border-b border-border/60 bg-card/80 backdrop-blur-md flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="size-8 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center text-primary">
                <Bot className="size-4" />
              </div>
              <div>
                <h2 className="text-sm font-bold text-foreground flex items-center gap-2">
                  RepoPilot AI Assistant
                  <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 font-semibold">
                    Qdrant RAG Enabled
                  </span>
                </h2>
                <p className="text-[11px] text-muted-foreground">
                  Answering queries grounded exclusively in repository code context.
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg hover:bg-muted text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
            >
              <X className="size-4" />
            </button>
          </div>

          {/* Messages Container */}
          <div className="flex-1 p-4 overflow-y-auto space-y-4 custom-scrollbar">
            {isMessagesLoading ? (
              <div className="flex items-center justify-center h-full text-xs text-muted-foreground gap-2">
                <Loader2 className="size-4 animate-spin text-primary" />
                Loading conversation history...
              </div>
            ) : messages.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full text-center p-6 space-y-3">
                <div className="size-12 rounded-2xl bg-primary/10 border border-primary/20 flex items-center justify-center text-primary">
                  <Bot className="size-6" />
                </div>
                <h3 className="text-sm font-bold text-foreground">Ask RepoPilot anything about {repositoryName}</h3>
                <p className="text-xs text-muted-foreground max-w-md">
                  Ask architecture questions, function implementations, dependency flows, or logic explanations.
                </p>
              </div>
            ) : (
              <>
                {messages.map((msg) => (
                  <div
                    key={msg.id}
                    className={`flex items-start gap-3 ${
                      msg.role === 'USER' ? 'flex-row-reverse' : ''
                    }`}
                  >
                    <div
                      className={`size-8 rounded-lg flex items-center justify-center shrink-0 ${
                        msg.role === 'USER'
                          ? 'bg-primary text-primary-foreground font-bold text-xs'
                          : 'bg-muted border border-border/60 text-primary'
                      }`}
                    >
                      {msg.role === 'USER' ? <User className="size-4" /> : <Bot className="size-4" />}
                    </div>

                    <div
                      className={`max-w-[80%] rounded-2xl p-4 text-xs space-y-3 ${
                        msg.role === 'USER'
                          ? 'bg-primary text-primary-foreground font-medium rounded-tr-none'
                          : 'bg-card border border-border/80 text-foreground rounded-tl-none shadow-xs'
                      }`}
                    >
                      <p className="whitespace-pre-wrap leading-relaxed">{msg.content}</p>

                      {/* Citations List if available */}
                      {msg.citations && msg.citations.length > 0 && (
                        <div className="pt-2 border-t border-border/40 space-y-1.5">
                          <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1">
                            <FileCode className="size-3 text-primary" /> Code Citations ({msg.citations.length})
                          </span>
                          <div className="flex flex-wrap gap-1.5">
                            {msg.citations.map((cit, cIdx) => (
                              <div
                                key={cIdx}
                                className="px-2.5 py-1 rounded-lg bg-muted/60 border border-border/60 text-[10px] font-mono flex items-center gap-1.5 text-foreground hover:border-primary/40 transition-colors"
                              >
                                <Code2 className="size-3 text-primary shrink-0" />
                                <span className="font-semibold truncate max-w-[180px]" title={cit.filePath}>
                                  {cit.filePath?.split('/').pop() || cit.filePath}
                                </span>
                                {cit.startLine && (
                                  <span className="text-muted-foreground">
                                    L{cit.startLine}{cit.endLine ? `-L${cit.endLine}` : ''}
                                  </span>
                                )}
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                ))}

                {/* Loading state while waiting for blocking response */}
                {isSending && (
                  <div className="flex items-start gap-3">
                    <div className="size-8 rounded-lg bg-muted border border-border/60 text-primary flex items-center justify-center shrink-0">
                      <Bot className="size-4 animate-spin" />
                    </div>
                    <div className="max-w-[80%] rounded-2xl p-4 text-xs bg-card border border-border/80 text-foreground rounded-tl-none shadow-xs flex items-center gap-2 text-muted-foreground">
                      <Loader2 className="size-3.5 animate-spin text-primary" />
                      <span>RepoPilot is thinking & retrieving code context...</span>
                    </div>
                  </div>
                )}
              </>
            )}

            {streamError && (
              <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-500 text-xs font-medium">
                {streamError}
              </div>
            )}

            <div ref={chatBottomRef} />
          </div>

          {/* Footer Input Box */}
          <form onSubmit={handleSendMessage} className="p-3.5 border-t border-border/60 bg-card/80 backdrop-blur-md flex items-center gap-2">
            <input
              type="text"
              value={inputQuery}
              onChange={(e) => setInputQuery(e.target.value)}
              disabled={isSending || !activeSessionId}
              placeholder={isSending ? 'RepoPilot is thinking...' : 'Ask a question about this repository...'}
              className="flex-1 h-10 px-3.5 text-xs rounded-xl border border-input/80 bg-background/80 focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary text-foreground placeholder:text-muted-foreground/60 disabled:opacity-50"
            />
            <Button
              type="submit"
              disabled={!inputQuery.trim() || isSending || !activeSessionId}
              className="h-10 px-4 rounded-xl gap-1.5 text-xs font-bold cursor-pointer"
            >
              {isSending ? (
                <Loader2 className="size-4 animate-spin" />
              ) : (
                <>
                  <Send className="size-3.5" /> Send
                </>
              )}
            </Button>
          </form>
        </div>
      </div>
    </div>
  );
};
