export interface User {
  userId?: string;
  id?: string;
  githubId?: number;
  githubUsername?: string;
  username?: string;
  displayName?: string;
  name?: string;
  email?: string;
  avatarUrl?: string;
  githubUrl?: string;
}

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

export interface OAuthErrorResponse {
  error: string;
  message: string;
}
