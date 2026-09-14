import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { GitHubLoginButton } from '../auth/GitHubLoginButton';
import { Button } from '../ui/button';
import { Bot, Sparkles, LogOut, User as UserIcon } from 'lucide-react';

import { ThemeToggle } from '../ui/ThemeToggle';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border/40 bg-background/80 backdrop-blur-md transition-all">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
        {/* Brand Logo */}
        <Link to="/" className="flex items-center gap-2.5 group">
          <div className="size-8 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center text-primary group-hover:bg-primary/20 transition-all">
            <Bot className="size-5" />
          </div>
          <div className="flex items-center gap-1.5">
            <span className="font-semibold text-base tracking-tight text-foreground">RepoPilot</span>
            <span className="inline-flex items-center gap-0.5 text-[10px] font-medium px-1.5 py-0.5 rounded-full bg-primary/10 text-primary border border-primary/20">
              <Sparkles className="size-2.5" /> RAG
            </span>
          </div>
        </Link>

        {/* Navigation Links */}
        <nav className="hidden md:flex items-center gap-6 text-xs font-medium text-muted-foreground">
          <a href="#features" className="hover:text-foreground transition-colors">
            Features
          </a>
          <a href="#how-it-works" className="hover:text-foreground transition-colors">
            How it Works
          </a>
          <a href="#rag-architecture" className="hover:text-foreground transition-colors">
            Architecture
          </a>
          <a href="https://github.com" target="_blank" rel="noreferrer" className="hover:text-foreground transition-colors">
            Documentation
          </a>
        </nav>

        {/* Action Buttons & Theme Toggle */}
        <div className="flex items-center gap-2.5">
          <ThemeToggle />

          {isAuthenticated && user ? (
            <div className="flex items-center gap-2.5">
              <Link to="/dashboard">
                <Button variant="ghost" size="sm" className="gap-2 text-xs">
                  {user.avatarUrl ? (
                    <img src={user.avatarUrl} alt={user.name || 'User'} className="size-5 rounded-full" />
                  ) : (
                    <UserIcon className="size-4" />
                  )}
                  <span>{user.username || user.name || 'Dashboard'}</span>
                </Button>
              </Link>
              <Button variant="outline" size="xs" onClick={logout} title="Sign Out">
                <LogOut className="size-3.5" />
              </Button>
            </div>
          ) : (
            <GitHubLoginButton size="sm" variant="default" label="Login with GitHub" />
          )}
        </div>
      </div>
    </header>
  );
};
