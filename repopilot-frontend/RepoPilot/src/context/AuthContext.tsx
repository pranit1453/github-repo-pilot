import React, { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import type { User } from '../types/auth';
import { authService } from '../services/api/authService';

const REFRESH_INTERVAL_MS = 14 * 60 * 1000; // 14 minutes

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  loginWithGithub: () => void;
  logout: () => Promise<void>;
  checkAuth: () => Promise<boolean>;
  refreshToken: () => Promise<boolean>;
  setError: (error: string | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const refreshTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const clearRefreshTimer = useCallback(() => {
    if (refreshTimerRef.current) {
      clearTimeout(refreshTimerRef.current);
      refreshTimerRef.current = null;
    }
  }, []);

  const logout = useCallback(async () => {
    clearRefreshTimer();
    setIsLoading(true);
    try {
      await authService.logout();
    } catch (err: unknown) {
      console.warn('Logout API call failed or session already invalid:', err);
    } finally {
      setUser(null);
      setError(null);
      setIsLoading(false);
      if (window.location.pathname !== '/') {
        window.location.href = '/';
      }
    }
  }, [clearRefreshTimer]);

  const triggerTokenRefresh = useCallback(async (): Promise<boolean> => {
    try {
      await authService.refreshToken();
      console.log('Access token refreshed successfully after 14 minutes.');
      return true;
    } catch (err) {
      console.warn('Auto token refresh failed. Auto logging out user:', err);
      await logout();
      return false;
    }
  }, [logout]);

  const scheduleTokenRefresh = useCallback(() => {
    clearRefreshTimer();
    refreshTimerRef.current = setTimeout(async () => {
      const success = await triggerTokenRefresh();
      if (success) {
        // Re-schedule for next 14 minutes if successful
        scheduleTokenRefresh();
      }
    }, REFRESH_INTERVAL_MS);
  }, [clearRefreshTimer, triggerTokenRefresh]);

  const checkAuth = useCallback(async (): Promise<boolean> => {
    setIsLoading(true);
    try {
      const userData = await authService.getCurrentUser();
      setUser(userData);
      setError(null);
      scheduleTokenRefresh();
      return true;
    } catch {
      setUser(null);
      clearRefreshTimer();
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [scheduleTokenRefresh, clearRefreshTimer]);

  useEffect(() => {
    // Only check auth automatically if not on the public landing page '/'
    if (window.location.pathname !== '/') {
      checkAuth();
    } else {
      setIsLoading(false);
    }

    return () => {
      clearRefreshTimer();
    };
  }, [checkAuth, clearRefreshTimer]);

  const loginWithGithub = () => {
    window.location.href = authService.getLoginUrl();
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        error,
        loginWithGithub,
        logout,
        checkAuth,
        refreshToken: triggerTokenRefresh,
        setError,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
