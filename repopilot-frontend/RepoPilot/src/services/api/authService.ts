import { fetchApi, API_BASE_URL } from './apiClient';
import type { User } from '../../types/auth';

export const authService = {
  /**
   * Returns the backend OAuth login endpoint.
   * Backend will initiate GitHub OAuth redirect.
   */
  getLoginUrl(): string {
    return `${API_BASE_URL}/api/auth/login`;
  },

  /**
   * Fetches the current logged-in user profile.
   * Leverages HttpOnly cookie attached automatically via credentials: 'include'.
   */
  async getCurrentUser(): Promise<User> {
    return fetchApi<User>('/api/auth/me');
  },

  /**
   * Triggers logout on backend to clear HttpOnly cookie session.
   */
  async logout(): Promise<void> {
    return fetchApi<void>('/api/auth/logout', {
      method: 'POST',
    });
  },

  /**
   * Refreshes the access token using the HttpOnly refresh token cookie.
   */
  async refreshToken(): Promise<void> {
    return fetchApi<void>('/api/auth/refresh', {
      method: 'POST',
    });
  },
};
