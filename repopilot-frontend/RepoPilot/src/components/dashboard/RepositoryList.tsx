import React, { useState, useMemo } from 'react';
import type { Repository } from '../../services/api/repositoryService';
import {
  Search,
  Lock,
  Globe,
  Clock,
  FileCode,
  Layers,
  AlertCircle,
  LayoutGrid,
  List as ListIcon,
  ArrowUpDown,
  Cpu,
  ExternalLink,
  CheckCircle2,
  XCircle,
  Loader2,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  Bot
} from 'lucide-react';

interface RepositoryListProps {
  repositories: Repository[];
  totalElements: number;
  currentPage: number;
  totalPages: number;
  pageSize?: number;
  isLoading: boolean;
  searchKeyword: string;
  onSearchChange: (keyword: string) => void;
  onPageChange: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
  onIndexRepo?: (repoId: string, repoName: string) => void;
  onInspectDetails?: (repo: Repository) => void;
  onOpenChat?: (repo: Repository) => void;
}

export const RepositoryList: React.FC<RepositoryListProps> = ({
  repositories,
  totalElements,
  currentPage,
  totalPages,
  pageSize = 10,
  isLoading,
  searchKeyword,
  onSearchChange,
  onPageChange,
  onPageSizeChange,
  onIndexRepo,
  onInspectDetails,
  onOpenChat,
}) => {
  // Local state for UI controls matching exact screenshot filters
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [visibilityFilter, setVisibilityFilter] = useState<string>('ALL');
  const [sortBy, setSortBy] = useState<'name' | 'language' | 'status'>('name');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');

  // Client-side filtering & sorting for smooth interactive UI
  const filteredRepositories = useMemo(() => {
    return repositories.filter((repo) => {
      // Filter by index status
      if (statusFilter !== 'ALL' && repo.indexStatus !== statusFilter) {
        return false;
      }
      // Filter by visibility
      if (visibilityFilter === 'PRIVATE' && !repo.isPrivate) return false;
      if (visibilityFilter === 'PUBLIC' && repo.isPrivate) return false;
      return true;
    }).sort((a, b) => {
      let comp = 0;
      if (sortBy === 'name') {
        comp = a.name.localeCompare(b.name);
      } else if (sortBy === 'language') {
        comp = (a.language || '').localeCompare(b.language || '');
      } else if (sortBy === 'status') {
        comp = a.indexStatus.localeCompare(b.indexStatus);
      }
      return sortDirection === 'asc' ? comp : -comp;
    });
  }, [repositories, statusFilter, visibilityFilter, sortBy, sortDirection]);

  const toggleSortDirection = () => {
    setSortDirection((prev) => (prev === 'asc' ? 'desc' : 'asc'));
  };

  return (
    <div className="space-y-6">
      {/* Top Filter and Toolbar Bar - Matching Exact Screenshot Design */}
      <div className="p-3.5 rounded-2xl border border-border/80 bg-card/80 backdrop-blur-md flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-3 shadow-sm">
        {/* Left: Search input field */}
        <div className="relative flex-1 min-w-[240px]">
          <Search className="size-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <input
            type="text"
            value={searchKeyword}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search by name, language, topic..."
            className="w-full h-10 pl-10 pr-3.5 text-xs rounded-xl border border-input/80 bg-background/60 focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-all text-foreground placeholder:text-muted-foreground/60"
          />
        </div>

        {/* Right: Sort & Filter dropdowns with view toggle */}
        <div className="flex items-center gap-2.5 flex-wrap shrink-0">
          {/* Sort By Dropdown */}
          <div className="flex items-center gap-1 bg-background/60 border border-input/80 rounded-xl px-2 py-1">
            <span className="text-[11px] text-muted-foreground flex items-center gap-1 pl-1">
              <ArrowUpDown className="size-3" />
            </span>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value as any)}
              className="h-8 bg-transparent text-xs font-medium focus:outline-none cursor-pointer text-foreground pr-1 [color-scheme:dark]"
            >
              <option value="name" className="bg-[#12141c] text-slate-100 py-1 font-medium">Sort by Name</option>
              <option value="language" className="bg-[#12141c] text-slate-100 py-1 font-medium">Sort by Language</option>
              <option value="status" className="bg-[#12141c] text-slate-100 py-1 font-medium">Sort by Status</option>
            </select>
            <button
              onClick={toggleSortDirection}
              className="text-[10px] uppercase font-bold font-mono px-2 py-0.5 rounded-md bg-secondary text-secondary-foreground hover:bg-secondary/80 transition-colors cursor-pointer"
              title={`Switch to ${sortDirection === 'asc' ? 'Descending' : 'Ascending'}`}
            >
              {sortDirection.toUpperCase()}
            </button>
          </div>

          {/* Index Status Filter Dropdown */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-10 px-3 bg-background/60 border border-input/80 rounded-xl text-xs font-medium focus:outline-none cursor-pointer text-foreground [color-scheme:dark]"
          >
            <option value="ALL" className="bg-[#12141c] text-slate-100 py-1 font-medium">All Index Status</option>
            <option value="PENDING" className="bg-[#12141c] text-slate-100 py-1 font-medium">PENDING</option>
            <option value="IN_PROGRESS" className="bg-[#12141c] text-slate-100 py-1 font-medium">IN_PROGRESS</option>
            <option value="COMPLETED" className="bg-[#12141c] text-slate-100 py-1 font-medium">COMPLETED</option>
            <option value="FAILED" className="bg-[#12141c] text-slate-100 py-1 font-medium">FAILED</option>
          </select>

          {/* Visibility Filter Dropdown */}
          <select
            value={visibilityFilter}
            onChange={(e) => setVisibilityFilter(e.target.value)}
            className="h-10 px-3 bg-background/60 border border-input/80 rounded-xl text-xs font-medium focus:outline-none cursor-pointer text-foreground [color-scheme:dark]"
          >
            <option value="ALL" className="bg-[#12141c] text-slate-100 py-1 font-medium">All Visibility</option>
            <option value="PUBLIC" className="bg-[#12141c] text-slate-100 py-1 font-medium">Public</option>
            <option value="PRIVATE" className="bg-[#12141c] text-slate-100 py-1 font-medium">Private</option>
          </select>

          {/* View Mode Toggle Buttons */}
          <div className="flex items-center gap-1 p-1 bg-background/60 border border-input/80 rounded-xl">
            <button
              onClick={() => setViewMode('grid')}
              className={`p-1.5 rounded-lg transition-all cursor-pointer ${
                viewMode === 'grid'
                  ? 'bg-secondary text-foreground shadow-xs font-semibold'
                  : 'text-muted-foreground hover:text-foreground'
              }`}
              title="Grid View"
            >
              <LayoutGrid className="size-4" />
            </button>
            <button
              onClick={() => setViewMode('list')}
              className={`p-1.5 rounded-lg transition-all cursor-pointer ${
                viewMode === 'list'
                  ? 'bg-secondary text-foreground shadow-xs font-semibold'
                  : 'text-muted-foreground hover:text-foreground'
              }`}
              title="List View"
            >
              <ListIcon className="size-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Main Content View */}
      {isLoading ? (
        <div
          className={
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5 animate-pulse'
              : 'space-y-3 animate-pulse'
          }
        >
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="h-56 rounded-2xl bg-card/40 border border-border/40" />
          ))}
        </div>
      ) : filteredRepositories.length === 0 ? (
        <div className="p-12 rounded-2xl border border-dashed border-border/80 bg-card/40 text-center space-y-3">
          <AlertCircle className="size-10 text-muted-foreground mx-auto" />
          <h3 className="text-sm font-semibold text-foreground">No repositories found</h3>
          <p className="text-xs text-muted-foreground max-w-sm mx-auto">
            {searchKeyword || statusFilter !== 'ALL' || visibilityFilter !== 'ALL'
              ? 'No repositories match your active filter criteria.'
              : 'Click "Sync Repositories" to import your GitHub repositories into RepoPilot.'}
          </p>
        </div>
      ) : viewMode === 'grid' ? (
        /* 3-Column Grid View - Matching Exact Card Layout from Screenshot */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filteredRepositories.map((repo) => (
            <div
              key={repo.id}
              className="p-5 rounded-2xl border border-border/80 bg-card/80 backdrop-blur-md hover:border-border hover:shadow-lg transition-all flex flex-col justify-between gap-4 group"
            >
              {/* Card Header & Badges */}
              <div className="space-y-3">
                {/* Repo Name & Visibility Badge */}
                <div className="flex items-start justify-between gap-3">
                  <h3 className="text-base font-bold text-foreground tracking-tight line-clamp-1 group-hover:text-primary transition-colors">
                    {repo.name}
                  </h3>

                  <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-medium border border-border/60 bg-muted/40 text-muted-foreground shrink-0">
                    {repo.isPrivate ? (
                      <>
                        <Lock className="size-3" /> Private
                      </>
                    ) : (
                      <>
                        <Globe className="size-3" /> Public
                      </>
                    )}
                  </span>
                </div>

                {/* Index Status Badge & Language Pill */}
                <div className="flex items-center gap-2 flex-wrap">
                  {(() => {
                    const isIndexed = repo.indexStatus === 'INDEXED' || repo.indexStatus === 'COMPLETED';
                    const isIndexing = repo.indexStatus === 'INDEXING' || repo.indexStatus === 'IN_PROGRESS' || repo.indexStatus === 'CHUNKING';
                    const isFailed = repo.indexStatus === 'FAILED';

                    return (
                      <span
                        className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold tracking-wider uppercase border ${
                          isIndexed
                            ? 'bg-emerald-500/10 text-emerald-500 border-emerald-500/30'
                            : isIndexing
                            ? 'bg-amber-500/10 text-amber-500 border-amber-500/30 animate-pulse'
                            : isFailed
                            ? 'bg-rose-500/10 text-rose-500 border-rose-500/30'
                            : 'bg-slate-500/10 text-slate-400 border-slate-500/30'
                        }`}
                      >
                        {isIndexed ? (
                          <CheckCircle2 className="size-3 text-emerald-500" />
                        ) : isFailed ? (
                          <XCircle className="size-3 text-rose-500" />
                        ) : isIndexing ? (
                          <Loader2 className="size-3 animate-spin text-amber-500" />
                        ) : (
                          <Clock className="size-3 text-slate-400" />
                        )}
                        {isIndexed ? 'INDEXED' : isIndexing ? 'INDEXING' : isFailed ? 'FAILED' : 'PENDING'}
                      </span>
                    );
                  })()}

                  {repo.language && (
                    <span className="px-2.5 py-0.5 rounded-full bg-secondary text-secondary-foreground font-semibold text-[10px] border border-border/50">
                      {repo.language}
                    </span>
                  )}
                </div>

                {/* Description Text */}
                <p className="text-xs text-muted-foreground/90 line-clamp-2 min-h-[2.5rem]">
                  {repo.description || 'No description provided for this repository.'}
                </p>
              </div>

              {/* Stats Box & Actions */}
              <div className="space-y-3.5 pt-2">
                {/* Stats Container Box: Files & Chunks */}
                <div className="p-3 rounded-xl bg-background/80 border border-border/60 flex items-center justify-between text-xs font-medium text-muted-foreground">
                  <span className="flex items-center gap-1.5">
                    <FileCode className="size-3.5 text-muted-foreground/80" /> Files: <span className="font-semibold text-foreground">{repo.filesProcessed}/{repo.filesTotal}</span>
                  </span>

                  <span className="flex items-center gap-1.5">
                    <Layers className="size-3.5 text-muted-foreground/80" /> Chunks: <span className="font-semibold text-foreground">{repo.chunkCount}</span>
                  </span>
                </div>

                {/* Card Footer Actions Row */}
                <div className="flex items-center justify-between gap-2 pt-1 flex-wrap">
                  <button
                    onClick={() => onInspectDetails && onInspectDetails(repo)}
                    className="text-xs text-muted-foreground hover:text-foreground font-medium transition-colors cursor-pointer flex items-center gap-1"
                  >
                    Inspect
                    {repo.htmlUrl && (
                      <a
                        href={repo.htmlUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        onClick={(e) => e.stopPropagation()}
                        className="hover:text-primary ml-0.5"
                        title="Open in GitHub"
                      >
                        <ExternalLink className="size-3" />
                      </a>
                    )}
                  </button>

                  <div className="flex items-center gap-1.5 shrink-0">
                    {repo.indexStatus === 'INDEXED' || repo.indexStatus === 'COMPLETED' ? (
                      <button
                        onClick={() => onOpenChat && onOpenChat(repo)}
                        className="bg-primary hover:bg-primary/90 text-primary-foreground font-bold text-xs px-3.5 py-1.5 rounded-xl flex items-center gap-1.5 transition-all cursor-pointer shadow-xs active:scale-95"
                      >
                        <Bot className="size-3.5" /> AI Chat
                      </button>
                    ) : (
                      <button
                        onClick={() => onIndexRepo && onIndexRepo(repo.id, repo.name)}
                        disabled={repo.indexStatus === 'INDEXING' || repo.indexStatus === 'IN_PROGRESS' || repo.indexStatus === 'CHUNKING'}
                        className="bg-secondary hover:bg-secondary/80 text-secondary-foreground font-bold text-xs px-3.5 py-1.5 rounded-xl flex items-center gap-1.5 transition-all cursor-pointer shadow-xs active:scale-95 disabled:opacity-60"
                      >
                        {repo.indexStatus === 'INDEXING' || repo.indexStatus === 'IN_PROGRESS' || repo.indexStatus === 'CHUNKING' ? (
                          <>
                            <Loader2 className="size-3.5 animate-spin text-primary" /> Indexing...
                          </>
                        ) : (
                          <>
                            <Cpu className="size-3.5" /> Index Now
                          </>
                        )}
                      </button>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        /* List View - Sleek Horizontal Row Layout */
        <div className="space-y-3">
          {filteredRepositories.map((repo) => {
            const isIndexed = repo.indexStatus === 'INDEXED' || repo.indexStatus === 'COMPLETED';
            const isIndexing = repo.indexStatus === 'INDEXING' || repo.indexStatus === 'IN_PROGRESS' || repo.indexStatus === 'CHUNKING';
            const isFailed = repo.indexStatus === 'FAILED';

            return (
              <div
                key={repo.id}
                className="p-4 rounded-xl border border-border/80 bg-card/80 backdrop-blur-md hover:border-border transition-all flex flex-col md:flex-row items-start md:items-center justify-between gap-4"
              >
                <div className="space-y-1.5 flex-1 min-w-0">
                  <div className="flex items-center gap-2.5 flex-wrap">
                    <h3 className="text-sm font-bold text-foreground tracking-tight">{repo.name}</h3>
                    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-medium border border-border/60 bg-muted/40 text-muted-foreground">
                      {repo.isPrivate ? <Lock className="size-2.5" /> : <Globe className="size-2.5" />}
                      {repo.isPrivate ? 'Private' : 'Public'}
                    </span>

                    <span
                      className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider border ${
                        isIndexed
                          ? 'bg-emerald-500/10 text-emerald-500 border-emerald-500/30'
                          : isIndexing
                          ? 'bg-amber-500/10 text-amber-500 border-amber-500/30 animate-pulse'
                          : isFailed
                          ? 'bg-rose-500/10 text-rose-500 border-rose-500/30'
                          : 'bg-slate-500/10 text-slate-400 border-slate-500/30'
                      }`}
                    >
                      {isIndexed ? (
                        <CheckCircle2 className="size-2.5 text-emerald-500" />
                      ) : isFailed ? (
                        <XCircle className="size-2.5 text-rose-500" />
                      ) : isIndexing ? (
                        <Loader2 className="size-2.5 animate-spin text-amber-500" />
                      ) : (
                        <Clock className="size-2.5 text-slate-400" />
                      )}
                      {isIndexed ? 'INDEXED' : isIndexing ? 'INDEXING' : isFailed ? 'FAILED' : 'PENDING'}
                    </span>

                    {repo.language && (
                      <span className="px-2 py-0.5 rounded-full bg-secondary text-secondary-foreground text-[10px] font-semibold border border-border/50">
                        {repo.language}
                      </span>
                    )}
                  </div>

                  <p className="text-xs text-muted-foreground line-clamp-1">{repo.description || 'No description provided for this repository.'}</p>
                </div>

                {/* Right Stats & Actions */}
                <div className="flex items-center gap-4 shrink-0 self-end md:self-auto">
                  <div className="text-xs font-medium text-muted-foreground flex items-center gap-4">
                    <span>Files: <strong className="text-foreground">{repo.filesProcessed}/{repo.filesTotal}</strong></span>
                    <span>Chunks: <strong className="text-foreground">{repo.chunkCount}</strong></span>
                  </div>

                  <div className="flex items-center gap-1.5 shrink-0">
                    {isIndexed ? (
                      <button
                        onClick={() => onOpenChat && onOpenChat(repo)}
                        className="bg-primary hover:bg-primary/90 text-primary-foreground font-bold text-xs px-3.5 py-1.5 rounded-lg flex items-center gap-1.5 transition-all cursor-pointer shadow-xs active:scale-95"
                      >
                        <Bot className="size-3.5" /> AI Chat
                      </button>
                    ) : (
                      <button
                        onClick={() => onIndexRepo && onIndexRepo(repo.id, repo.name)}
                        disabled={isIndexing}
                        className="bg-secondary hover:bg-secondary/80 text-secondary-foreground font-bold text-xs px-3.5 py-1.5 rounded-lg flex items-center gap-1.5 transition-all cursor-pointer shadow-xs active:scale-95 disabled:opacity-60"
                      >
                        {isIndexing ? (
                          <>
                            <Loader2 className="size-3.5 animate-spin text-primary" /> Indexing...
                          </>
                        ) : (
                          <>
                            <Cpu className="size-3.5" /> Index Now
                          </>
                        )}
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Pagination Controls - Exact Match to Screenshot 3 */}
      <div className="p-3.5 rounded-2xl border border-border/80 bg-card/80 backdrop-blur-md flex flex-col sm:flex-row items-center justify-between gap-4 shadow-sm text-xs font-medium text-muted-foreground">
        {/* Left: Showing X to Y of Z repositories */}
        <div>
          Showing <strong className="text-foreground font-bold">{totalElements === 0 ? 0 : currentPage * pageSize + 1}</strong> to <strong className="text-foreground font-bold">{Math.min((currentPage + 1) * pageSize, totalElements)}</strong> of <strong className="text-foreground font-bold">{totalElements}</strong> repositories
        </div>

        {/* Right: Per page selector & Navigation buttons */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            <span className="text-muted-foreground">Per page:</span>
            <select
              value={pageSize}
              onChange={(e) => onPageSizeChange && onPageSizeChange(Number(e.target.value))}
              className="h-8 px-2 bg-background/80 border border-input/80 rounded-xl text-xs font-bold text-foreground focus:outline-none cursor-pointer [color-scheme:dark]"
            >
              <option value={10} className="bg-[#12141c] text-slate-100 font-medium">10</option>
              <option value={20} className="bg-[#12141c] text-slate-100 font-medium">20</option>
              <option value={50} className="bg-[#12141c] text-slate-100 font-medium">50</option>
            </select>
          </div>

          <div className="flex items-center gap-1.5">
            {/* First Page << */}
            <button
              onClick={() => onPageChange(0)}
              disabled={currentPage === 0 || isLoading}
              className="size-8 rounded-xl border border-input/80 bg-background/60 hover:bg-secondary text-muted-foreground hover:text-foreground flex items-center justify-center disabled:opacity-30 disabled:cursor-not-allowed transition-all cursor-pointer"
              title="First Page"
            >
              <ChevronsLeft className="size-4" />
            </button>

            {/* Previous Page < */}
            <button
              onClick={() => onPageChange(currentPage - 1)}
              disabled={currentPage === 0 || isLoading}
              className="size-8 rounded-xl border border-input/80 bg-background/60 hover:bg-secondary text-muted-foreground hover:text-foreground flex items-center justify-center disabled:opacity-30 disabled:cursor-not-allowed transition-all cursor-pointer"
              title="Previous Page"
            >
              <ChevronLeft className="size-4" />
            </button>

            {/* Page X of Y Indicator */}
            <span className="px-2 text-xs font-medium text-muted-foreground">
              Page <strong className="text-foreground font-bold">{currentPage + 1}</strong> of <strong className="text-foreground font-bold">{totalPages || 1}</strong>
            </span>

            {/* Next Page > */}
            <button
              onClick={() => onPageChange(currentPage + 1)}
              disabled={currentPage >= totalPages - 1 || isLoading}
              className="size-8 rounded-xl border border-input/80 bg-background/60 hover:bg-secondary text-muted-foreground hover:text-foreground flex items-center justify-center disabled:opacity-30 disabled:cursor-not-allowed transition-all cursor-pointer"
              title="Next Page"
            >
              <ChevronRight className="size-4" />
            </button>

            {/* Last Page >> */}
            <button
              onClick={() => onPageChange(Math.max(0, totalPages - 1))}
              disabled={currentPage >= totalPages - 1 || isLoading}
              className="size-8 rounded-xl border border-input/80 bg-background/60 hover:bg-secondary text-muted-foreground hover:text-foreground flex items-center justify-center disabled:opacity-30 disabled:cursor-not-allowed transition-all cursor-pointer"
              title="Last Page"
            >
              <ChevronsRight className="size-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
