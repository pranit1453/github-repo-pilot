import { fetchApi, API_BASE_URL } from './apiClient';

export interface Repository {
  id: string;
  githubRepoId: number;
  owner: string;
  name: string;
  fullName: string;
  isPrivate: boolean;
  defaultBranch: string;
  language: string | null;
  htmlUrl: string | null;
  description: string | null;
  indexStatus: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  indexedAt: string | null;
  chunkCount: number;
  filesTotal: number;
  filesProcessed: number;
}

export interface RepositoryStats {
  totalRepositories: number;
  pendingIndexCount: number;
  indexingCount: number;
  completedIndexCount: number;
  failedIndexCount: number;
  totalFilesProcessed: number;
  totalChunks: number;
  languages: Record<string, number>;
}

export interface PaginatedRepositoriesResponse {
  contents: Repository[];
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isLastPage: boolean;
  isFirstPage: boolean;
}

export const repositoryService = {
  /**
   * Triggers background sync of GitHub repositories for the authenticated user.
   */
  async triggerSync(): Promise<void> {
    return fetchApi<void>('/api/repository/sync', {
      method: 'POST',
    });
  },

  /**
   * Fetches paginated repositories list.
   */
  async getRepositories(
    page = 0,
    size = 10,
    keyword = '',
    sortBy = 'name',
    sortDirection = 'asc'
  ): Promise<PaginatedRepositoriesResponse> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDirection,
    });
    if (keyword.trim()) {
      params.append('keyword', keyword.trim());
    }

    return fetchApi<PaginatedRepositoriesResponse>(`/api/repository?${params.toString()}`);
  },

  /**
   * Fetches user repository & indexing statistics.
   */
  async getStats(): Promise<RepositoryStats> {
    return fetchApi<RepositoryStats>('/api/repository/stats');
  },

  /**
   * Subscribes to real-time Server-Sent Events (SSE) for repository sync updates.
   */
  subscribeToSyncEvents(
    onMessage: (data: { status: string }) => void,
    onError?: (event: Event) => void
  ): EventSource {
    const sseUrl = `${API_BASE_URL}/api/repository/sync/events`;
    const eventSource = new EventSource(sseUrl, { withCredentials: true });

    eventSource.addEventListener('repository-sync', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data);
        onMessage(data);
      } catch (err) {
        console.error('Failed to parse SSE payload:', err);
      }
    });

    eventSource.addEventListener('connected', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data);
        onMessage(data);
      } catch (err) {
        console.error('Failed to parse SSE connected payload:', err);
      }
    });

    if (onError) {
      eventSource.onerror = onError;
    }

    return eventSource;
  },
};
