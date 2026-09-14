import React from 'react';
import { BackgroundAnimation } from '../components/landing/BackgroundAnimation';
import { Navbar } from '../components/layout/Navbar';
import { Footer } from '../components/layout/Footer';
import { GitHubLoginButton } from '../components/auth/GitHubLoginButton';
import { 
  Sparkles, 
  GitBranch, 
  Search, 
  Zap, 
  ShieldCheck, 
  Cpu
} from 'lucide-react';

export const LandingPage: React.FC = () => {
  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground relative selection:bg-primary/20 selection:text-primary">
      {/* Background Canvas Animation */}
      <BackgroundAnimation />

      {/* Top Navbar */}
      <Navbar />

      {/* Main Content Area */}
      <main className="flex-1 relative z-10 space-y-8">
        {/* Hero Section */}
        <section className="max-w-4xl mx-auto px-4 sm:px-6 pt-20 pb-12 text-center space-y-6">
          {/* Release Pill Badge */}
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/10 border border-primary/20 text-xs font-medium text-primary backdrop-blur-sm">
            <Sparkles className="size-3.5" />
            <span>GitHub Repo Pilot RAG</span>
            <span className="text-muted-foreground">•</span>
            <span className="text-muted-foreground font-normal">v1.0</span>
          </div>

          {/* Minimalist Heading */}
          <h1 className="text-2xl sm:text-3xl md:text-4xl font-semibold tracking-tight text-foreground max-w-2xl mx-auto leading-snug">
            Intelligent Codebase RAG & Retrieval for GitHub Repositories
          </h1>

          {/* Subtitle */}
          <p className="text-xs sm:text-sm text-muted-foreground max-w-xl mx-auto leading-relaxed">
            Index, query, and explore complex repositories with high-precision semantic retrieval. Built with solid architecture and modern security standards.
          </p>

          {/* Primary Action Button */}
          <div className="pt-2 flex justify-center">
            <GitHubLoginButton size="lg" variant="default" label="Start with GitHub OAuth" />
          </div>

          {/* Security & Feature Badges */}
          <div className="pt-6 flex flex-wrap items-center justify-center gap-6 text-[11px] text-muted-foreground">
            <div className="flex items-center gap-1.5">
              <ShieldCheck className="size-3.5 text-primary" />
              <span>HttpOnly Cookie Auth</span>
            </div>
            <div className="flex items-center gap-1.5">
              <Zap className="size-3.5 text-primary" />
              <span>Real-time RAG Context</span>
            </div>
            <div className="flex items-center gap-1.5">
              <GitBranch className="size-3.5 text-primary" />
              <span>GitHub Integration</span>
            </div>
          </div>
        </section>

        {/* Core Feature Cards Grid */}
        <section id="features" className="max-w-5xl mx-auto px-4 sm:px-6 py-8">
          <div className="text-center space-y-1 mb-8">
            <h2 className="text-lg font-semibold tracking-tight">Engineered for Technical Precision</h2>
            <p className="text-xs text-muted-foreground">Minimal overhead, maximum context, clean SOLID design.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
            {/* Card 1 */}
            <div className="p-5 rounded-xl border border-border/60 bg-card/60 backdrop-blur-sm hover:border-primary/40 transition-all space-y-3">
              <div className="size-9 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center text-primary">
                <Search className="size-4.5" />
              </div>
              <h3 className="text-sm font-semibold text-foreground">Semantic Vector Search</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                Query repository files using natural language. Embedding similarity matching brings exact code snippets into prompt context.
              </p>
            </div>

            {/* Card 2 */}
            <div className="p-5 rounded-xl border border-border/60 bg-card/60 backdrop-blur-sm hover:border-primary/40 transition-all space-y-3">
              <div className="size-9 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center text-primary">
                <Cpu className="size-4.5" />
              </div>
              <h3 className="text-sm font-semibold text-foreground">RAG Orchestration</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                AST parsing and chunking strategy designed for large multi-file codebases, preserving class and function scope.
              </p>
            </div>

            {/* Card 3 */}
            <div className="p-5 rounded-xl border border-border/60 bg-card/60 backdrop-blur-sm hover:border-primary/40 transition-all space-y-3">
              <div className="size-9 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center text-primary">
                <ShieldCheck className="size-4.5" />
              </div>
              <h3 className="text-sm font-semibold text-foreground">Secure OAuth2 Cookies</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                Stateless backend authentication using HttpOnly cookies to prevent XSS session hijack and maintain secure API boundaries.
              </p>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <Footer />
    </div>
  );
};
