import React from 'react';
import type { RepositoryStats } from '../../services/api/repositoryService';
import { Database, FileCode, Layers, Clock, Code2 } from 'lucide-react';

interface StatsOverviewProps {
  stats: RepositoryStats | null;
  isLoading: boolean;
}

export const StatsOverview: React.FC<StatsOverviewProps> = ({ stats, isLoading }) => {
  if (isLoading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 animate-pulse">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="h-24 rounded-xl bg-card/40 border border-border/40" />
        ))}
      </div>
    );
  }

  const totalRepos = stats?.totalRepositories || 0;
  const pendingIndex = stats?.pendingIndexCount || 0;
  const completedIndex = stats?.completedIndexCount || 0;
  const totalFiles = stats?.totalFilesProcessed || 0;
  const totalChunks = stats?.totalChunks || 0;
  const languages = stats?.languages || {};

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Repositories */}
        <div className="p-4 rounded-xl border border-border/60 bg-card/60 backdrop-blur-sm flex items-center gap-3">
          <div className="size-10 rounded-lg bg-primary/10 flex items-center justify-center text-primary shrink-0">
            <Database className="size-5" />
          </div>
          <div>
            <p className="text-[11px] font-medium text-muted-foreground uppercase tracking-wider">Synced Repos</p>
            <h3 className="text-xl font-bold tracking-tight">{totalRepos}</h3>
          </div>
        </div>

        {/* Index Status Overview */}
        <div className="p-4 rounded-xl border border-border/60 bg-card/60 backdrop-blur-sm flex items-center gap-3">
          <div className="size-10 rounded-lg bg-amber-500/10 flex items-center justify-center text-amber-500 shrink-0">
            <Clock className="size-5" />
          </div>
          <div>
            <p className="text-[11px] font-medium text-muted-foreground uppercase tracking-wider">Index Status</p>
            <div className="flex items-center gap-2 mt-0.5">
              <span className="text-xs font-semibold text-amber-500">{pendingIndex} Pending</span>
              {completedIndex > 0 && (
                <span className="text-xs font-semibold text-emerald-500">&bull; {completedIndex} Indexed</span>
              )}
            </div>
          </div>
        </div>

        {/* Processed Files */}
        <div className="p-4 rounded-xl border border-border/60 bg-card/60 backdrop-blur-sm flex items-center gap-3">
          <div className="size-10 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-500 shrink-0">
            <FileCode className="size-5" />
          </div>
          <div>
            <p className="text-[11px] font-medium text-muted-foreground uppercase tracking-wider">Processed Files</p>
            <h3 className="text-xl font-bold tracking-tight">{totalFiles}</h3>
          </div>
        </div>

        {/* Chunks */}
        <div className="p-4 rounded-xl border border-border/60 bg-card/60 backdrop-blur-sm flex items-center gap-3">
          <div className="size-10 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-500 shrink-0">
            <Layers className="size-5" />
          </div>
          <div>
            <p className="text-[11px] font-medium text-muted-foreground uppercase tracking-wider">RAG Chunks</p>
            <h3 className="text-xl font-bold tracking-tight">{totalChunks}</h3>
          </div>
        </div>
      </div>

      {/* Languages Distribution */}
      {Object.keys(languages).length > 0 && (
        <div className="p-3 rounded-xl border border-border/40 bg-card/40 flex items-center gap-2 text-xs flex-wrap">
          <span className="flex items-center gap-1 font-medium text-muted-foreground mr-1">
            <Code2 className="size-3.5" /> Primary Languages:
          </span>
          {Object.entries(languages).map(([lang, count]) => (
            <span
              key={lang}
              className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-secondary text-secondary-foreground text-[11px] font-medium border border-border/40"
            >
              {lang}
              <span className="text-[10px] text-muted-foreground font-mono">({count})</span>
            </span>
          ))}
        </div>
      )}
    </div>
  );
};
