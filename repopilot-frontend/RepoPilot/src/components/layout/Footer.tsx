import React from 'react';
import { Bot, GitFork } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="border-t border-border/40 bg-background/50 py-8 text-xs text-muted-foreground relative z-10">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <Bot className="size-4 text-primary" />
          <span className="font-medium text-foreground">GitHub Repo Pilot RAG</span>
          <span>&copy; {new Date().getFullYear()}</span>
        </div>
        <div className="flex items-center gap-6">
          <a href="#features" className="hover:text-foreground transition-colors">
            Features
          </a>
          <a href="#how-it-works" className="hover:text-foreground transition-colors">
            Workflow
          </a>
          <div className="flex items-center gap-1.5 text-muted-foreground/80">
            <GitFork className="size-3.5" />
            <span>v1.0.0</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
