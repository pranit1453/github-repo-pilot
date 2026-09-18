import { fetchApi } from './apiClient';

export interface ChatSession {
  id: string;
  repositoryId: string;
  title: string;
  createdAt: string;
}

export interface CitationDto {
  filePath?: string;
  startLine?: number;
  endLine?: number;
  language?: string;
}

export interface ChatMessage {
  id: string;
  role: 'USER' | 'ASSISTANT';
  content: string;
  citations: CitationDto[];
  createdAt: string;
}

export const chatService = {
  /**
   * Creates a new chat session for a repository.
   */
  async createSession(repositoryId: string, title?: string): Promise<ChatSession> {
    return fetchApi<ChatSession>('/api/chat/sessions', {
      method: 'POST',
      body: JSON.stringify({ repositoryId, title }),
    });
  },

  /**
   * Fetches active chat sessions for a repository.
   */
  async getSessions(repositoryId: string): Promise<ChatSession[]> {
    return fetchApi<ChatSession[]>(`/api/chat/sessions?repositoryId=${encodeURIComponent(repositoryId)}`);
  },

  /**
   * Fetches message history for a specific chat session.
   */
  async getMessages(sessionId: string): Promise<ChatMessage[]> {
    return fetchApi<ChatMessage[]>(`/api/chat/sessions/${sessionId}/messages`);
  },

  /**
   * Sends a chat message synchronously (blocking).
   */
  async sendMessage(sessionId: string, content: string): Promise<ChatMessage> {
    return fetchApi<ChatMessage>(`/api/chat/sessions/${sessionId}/messages`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    });
  },
};
