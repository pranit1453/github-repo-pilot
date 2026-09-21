import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Navbar } from '../components/layout/Navbar';
import { Footer } from '../components/layout/Footer';
import { BackgroundAnimation } from '../components/landing/BackgroundAnimation';
import { SyncPipelineVisualizer } from '../components/dashboard/SyncPipelineVisualizer';
import type { SyncStage } from '../components/dashboard/SyncPipelineVisualizer';
import { IndexingPipelineVisualizer } from '../components/dashboard/IndexingPipelineVisualizer';
import { RepoPilotChatModal } from '../components/chat/RepoPilotChatModal';
import { StatsOverview } from '../components/dashboard/StatsOverview';
import { RepositoryList } from '../components/dashboard/RepositoryList';
import { repositoryService } from '../services/api/repositoryService';
import type { Repository, RepositoryStats } from '../services/api/repositoryService';
import { LogOut, CheckCircle2, User as UserIcon, X, ExternalLink } from 'lucide-react';
import { Button } from '../components/ui/button';

export const DashboardPage: React.FC = () => {
  const { user, logout, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!user && !isLoading) {
      navigate('/');
    }
  }, [user, isLoading, navigate]);

  // State management
  const [syncStage, setSyncStage] = useState<SyncStage>('IDLE');
  const [isSyncing, setIsSyncing] = useState<boolean>(false);
  const [lastSyncedAt, setLastSyncedAt] = useState<Date | null>(null);
  const [syncError, setSyncError] = useState<string | null>(null);

  const [stats, setStats] = useState<RepositoryStats | null>(null);
  const [isStatsLoading, setIsStatsLoading] = useState<boolean>(true);

  const [repositories, setRepositories] = useState<Repository[]>([]);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [pageSize, setPageSize] = useState<number>(10);
  const [isReposLoading, setIsReposLoading] = useState<boolean>(true);
  const [searchKeyword, setSearchKeyword] = useState<string>('');

  const [isProfileModalOpen, setIsProfileModalOpen] = useState<boolean>(false);

  // Indexing Pipeline Modal State
  const [indexingRepoId, setIndexingRepoId] = useState<string | null>(null);
  const [indexingRepoName, setIndexingRepoName] = useState<string | null>(null);
  const [isIndexingModalOpen, setIsIndexingModalOpen] = useState<boolean>(false);

  // RepoPilot Chat Modal State
  const [chatRepoId, setChatRepoId] = useState<string | null>(null);
  const [chatRepoName, setChatRepoName] = useState<string | null>(null);
  const [isChatModalOpen, setIsChatModalOpen] = useState<boolean>(false);

  const eventSourceRef = useRef<EventSource | null>(null);

  // Load repositories and stats from backend
  const loadStats = useCallback(async () => {
    try {
      setIsStatsLoading(true);
      const data = await repositoryService.getStats();
      setStats(data);
    } catch (err) {
      console.error('Failed to load repository stats:', err);
    } finally {
      setIsStatsLoading(false);
    }
  }, []);

  const loadRepositories = useCallback(async (page = 0, size = 10, keyword = '') => {
    try {
      setIsReposLoading(true);
      const response = await repositoryService.getRepositories(page, size, keyword);
      setRepositories(response.contents || []);
      setTotalElements(response.totalElements || 0);
      setCurrentPage(response.currentPage || 0);
      setTotalPages(response.totalPages || 0);
    } catch (err) {
      console.error('Failed to load repositories:', err);
    } finally {
      setIsReposLoading(false);
    }
  }, []);

  // Initial load
  useEffect(() => {
    loadStats();
    loadRepositories(0, pageSize, searchKeyword);
  }, [loadStats, loadRepositories, pageSize, searchKeyword]);

  // Cleanup EventSource on unmount
  useEffect(() => {
    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, []);

  // Handle triggering repository sync
  const handleTriggerSync = async () => {
    setSyncError(null);
    setIsSyncing(true);
    setSyncStage('CONNECTED');

    // Close any previous SSE connection
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    try {
      // Subscribe to SSE sync events
      eventSourceRef.current = repositoryService.subscribeToSyncEvents(
        (data) => {
          console.log('SSE Sync Event Received:', data);
          if (data.status === 'CONNECTED') {
            setSyncStage('CONNECTED');
          } else if (data.status === 'INITIATED') {
            setSyncStage('INITIATED');
          } else if (data.status === 'PROCESSING') {
            setSyncStage('PROCESSING');
          } else if (data.status === 'COMPLETED') {
            setSyncStage('COMPLETED');
            setIsSyncing(false);
            setLastSyncedAt(new Date());
            // Refresh stats and repo list upon completion
            loadStats();
            loadRepositories(currentPage, pageSize, searchKeyword);
            if (eventSourceRef.current) {
              eventSourceRef.current.close();
            }
          } else if (data.status === 'FAILED') {
            setSyncStage('FAILED');
            setIsSyncing(false);
            setSyncError('Backend repository synchronization failed. Please check GitHub credentials.');
            if (eventSourceRef.current) {
              eventSourceRef.current.close();
            }
          }
        },
        (err) => {
          console.error('SSE Connection Error:', err);
          // If SSE encounters an error during sync
          if (isSyncing) {
            setSyncStage('FAILED');
            setIsSyncing(false);
            setSyncError('Connection to backend sync stream lost.');
          }
          if (eventSourceRef.current) {
            eventSourceRef.current.close();
          }
        }
      );

      // Trigger backend sync operation
      await repositoryService.triggerSync();
    } catch (err: any) {
      console.error('Failed to trigger sync:', err);
      setSyncStage('FAILED');
      setIsSyncing(false);
      setSyncError(err.message || 'Failed to initiate repository sync.');
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    }
  };

  const userDisplayName = user?.displayName || user?.name || user?.githubUsername || user?.username || 'Pilot User';
  const userGithubUsername = user?.githubUsername || user?.username;

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground relative">
      <BackgroundAnimation />
      <Navbar />

      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 py-8 relative z-10 space-y-8">
        {/* Welcome Header Card */}
        <div className="p-6 rounded-2xl border border-border/80 bg-card/80 backdrop-blur-md flex flex-col md:flex-row items-start md:items-center justify-between gap-4 shadow-sm">
          <div className="flex items-center gap-4">
            {user?.avatarUrl ? (
              <img src={user.avatarUrl} alt={userDisplayName} className="size-12 rounded-full border-2 border-primary/30" />
            ) : (
              <div className="size-12 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center text-primary font-bold text-lg">
                {userDisplayName.charAt(0).toUpperCase()}
              </div>
            )}
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-lg font-semibold tracking-tight">Welcome, {userDisplayName}</h1>
                <span className="inline-flex items-center gap-1 text-[10px] px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 font-medium">
                  <CheckCircle2 className="size-3" /> Authenticated
                </span>
              </div>
              <p className="text-xs text-muted-foreground">
                 {userGithubUsername ? `@${userGithubUsername}` : 'Ready to index repositories.'}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2.5">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setIsProfileModalOpen(true)}
              className="gap-2 text-xs cursor-pointer"
            >
              <UserIcon className="size-3.5 text-primary" /> View Profile
            </Button>

            <Button
              variant="outline"
              size="sm"
              onClick={logout}
              className="gap-2 text-xs cursor-pointer text-muted-foreground hover:text-foreground"
            >
              <LogOut className="size-3.5" /> Sign Out
            </Button>
          </div>
        </div>

        {/* Real-time Repository Sync Pipeline Visualizer */}
        <SyncPipelineVisualizer
          currentStage={syncStage}
          isSyncing={isSyncing}
          onTriggerSync={handleTriggerSync}
          lastSyncedAt={lastSyncedAt}
          errorMessage={syncError}
        />

        {/* Repository Stats Overview */}
        <StatsOverview stats={stats} isLoading={isStatsLoading} />

        {/* Synced Repository List Explorer */}
        <RepositoryList
          repositories={repositories}
          totalElements={totalElements}
          currentPage={currentPage}
          totalPages={totalPages}
          pageSize={pageSize}
          isLoading={isReposLoading}
          searchKeyword={searchKeyword}
          onSearchChange={(kw) => {
            setSearchKeyword(kw);
            setCurrentPage(0);
          }}
          onPageChange={(page) => {
            setCurrentPage(page);
            loadRepositories(page, pageSize, searchKeyword);
          }}
          onPageSizeChange={(newSize) => {
            setPageSize(newSize);
            setCurrentPage(0);
            loadRepositories(0, newSize, searchKeyword);
          }}
          onIndexRepo={(id, name) => {
            setIndexingRepoId(id);
            setIndexingRepoName(name);
            setIsIndexingModalOpen(true);
          }}
          onOpenChat={(repo) => {
            setChatRepoId(repo.id);
            setChatRepoName(repo.name);
            setIsChatModalOpen(true);
          }}
        />
      </main>

      {/* Real-time Repository Indexing Pipeline Modal */}
      <IndexingPipelineVisualizer
        repositoryId={indexingRepoId}
        repositoryName={indexingRepoName}
        isOpen={isIndexingModalOpen}
        onClose={() => setIsIndexingModalOpen(false)}
        onIndexingComplete={() => {
          loadStats();
          loadRepositories(currentPage, pageSize, searchKeyword);
        }}
      />

      {/* RepoPilot AI Chat Modal */}
      <RepoPilotChatModal
        repositoryId={chatRepoId}
        repositoryName={chatRepoName}
        isOpen={isChatModalOpen}
        onClose={() => setIsChatModalOpen(false)}
      />

      {/* User Profile Details Modal */}
      {isProfileModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4 animate-in fade-in duration-200">
          <div className="relative w-full max-w-md rounded-2xl border border-border/80 bg-card p-6 shadow-2xl space-y-6">
            {/* Modal Header */}
            <div className="flex items-center justify-between border-b border-border/40 pb-4">
              <div className="flex items-center gap-2">
                <UserIcon className="size-5 text-primary" />
                <h2 className="text-base font-bold text-foreground">User Profile Details</h2>
              </div>
              <button
                onClick={() => setIsProfileModalOpen(false)}
                className="p-1 rounded-lg hover:bg-muted text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
              >
                <X className="size-4" />
              </button>
            </div>

            {/* Profile Avatar & Primary Details */}
            <div className="flex items-center gap-4 p-4 rounded-xl bg-muted/30 border border-border/50">
              {user?.avatarUrl ? (
                <img src={user.avatarUrl} alt={userDisplayName} className="size-16 rounded-full border-2 border-primary/40 shadow-sm" />
              ) : (
                <div className="size-16 rounded-full bg-primary/20 border border-primary/30 flex items-center justify-center text-primary font-bold text-2xl">
                  {userDisplayName.charAt(0).toUpperCase()}
                </div>
              )}
              <div className="space-y-1 min-w-0 flex-1">
                <h3 className="text-base font-bold text-foreground truncate">{userDisplayName}</h3>
                {userGithubUsername && (
                  <p className="text-xs text-muted-foreground flex items-center gap-1 font-mono">
                    <UserIcon className="size-3" /> @{userGithubUsername}
                  </p>
                )}
                <span className="inline-flex items-center gap-1 text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 font-semibold">
                  <CheckCircle2 className="size-3" /> Active
                </span>
              </div>
            </div>

            {/* Detailed Properties Grid */}
            <div className="space-y-2.5 text-xs font-mono">
              <div className="p-3 rounded-xl bg-background/80 border border-border/60 flex items-center justify-between">
                <span className="text-muted-foreground">User ID:</span>
                <span className="text-foreground font-semibold truncate max-w-[200px]" title={user?.userId || user?.id || 'N/A'}>
                  {user?.userId || user?.id || 'N/A'}
                </span>
              </div>

              {user?.githubId && (
                <div className="p-3 rounded-xl bg-background/80 border border-border/60 flex items-center justify-between">
                  <span className="text-muted-foreground">GitHub ID:</span>
                  <span className="text-foreground font-semibold">{user.githubId}</span>
                </div>
              )}

              {userGithubUsername && (
                <div className="p-3 rounded-xl bg-background/80 border border-border/60 flex items-center justify-between">
                  <span className="text-muted-foreground">GitHub Profile:</span>
                  <a
                    href={`https://github.com/${userGithubUsername}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-primary hover:underline flex items-center gap-1 font-semibold"
                  >
                    github.com/{userGithubUsername}
                    <ExternalLink className="size-3" />
                  </a>
                </div>
              )}
            </div>

            {/* Modal Footer */}
            <div className="pt-2">
              <Button
                onClick={() => setIsProfileModalOpen(false)}
                className="w-full justify-center font-semibold text-xs cursor-pointer"
              >
                Close Profile
              </Button>
            </div>
          </div>
        </div>
      )}

      <Footer />
    </div>
  );
};
