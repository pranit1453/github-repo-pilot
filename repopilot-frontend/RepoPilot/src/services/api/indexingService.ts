import { fetchApi, API_BASE_URL } from './apiClient';

export interface IndexingResponse {
  repositoryId: string;
  status: 'PENDING' | 'INDEXING' | 'CHUNKING' | 'INDEXED' | 'FAILED' | 'INITIATED';
  message?: string;
}

export const indexingService = {
  /**
   * Triggers background indexing for the specified repository ID.
   */
  async startIndexing(repositoryId: string): Promise<IndexingResponse> {
    return fetchApi<IndexingResponse>(`/api/repositories/${repositoryId}/index`, {
      method: 'POST',
    });
  },

  /**
   * Subscribes to real-time Server-Sent Events (SSE) for repository indexing status updates.
   */
  subscribeToIndexingEvents(
    repositoryId: string,
    onMessage: (data: { status: string; message?: string }) => void,
    onError?: (event: Event) => void
  ): EventSource {
    const sseUrl = `${API_BASE_URL}/api/repositories/${repositoryId}/indexing/events`;
    const eventSource = new EventSource(sseUrl, { withCredentials: true });

    const handleEvent = (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data);
        onMessage(data);
      } catch (err) {
        console.error('Failed to parse indexing SSE payload:', err);
      }
    };

    eventSource.onmessage = handleEvent;
    eventSource.addEventListener('indexing-sync', handleEvent);
    eventSource.addEventListener('connected', handleEvent);

    if (onError) {
      eventSource.onerror = onError;
    }

    return eventSource;
  },
};
