import React from 'react';
import { RefreshCw, CheckCircle2, AlertCircle, Clock, Server, GitBranch, ArrowRight } from 'lucide-react';

export type SyncStage = 'IDLE' | 'CONNECTED' | 'INITIATED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

interface SyncPipelineVisualizerProps {
  currentStage: SyncStage;
  isSyncing: boolean;
  onTriggerSync: () => void;
  lastSyncedAt: Date | null;
  errorMessage?: string | null;
}

export const SyncPipelineVisualizer: React.FC<SyncPipelineVisualizerProps> = ({
  currentStage,
  isSyncing,
  onTriggerSync,
  lastSyncedAt,
  errorMessage,
}) => {
  const steps = [
    {
      id: 'INITIATED',
      title: 'Sync Initiated',
      description: 'Handshake & auth check',
      icon: Server,
    },
    {
      id: 'PROCESSING',
      title: 'Repository Sync',
      description: 'Fetching GitHub metadata & DB persistence',
      icon: GitBranch,
    },
    {
      id: 'COMPLETED',
      title: 'Sync Complete',
      description: 'Metadata ready for RAG indexing',
      icon: CheckCircle2,
    },
  ];

  const getStepState = (stepId: string) => {
    if (currentStage === 'FAILED') return 'failed';
    if (currentStage === 'COMPLETED') return 'completed';
    if (currentStage === 'PROCESSING') {
      if (stepId === 'INITIATED') return 'completed';
      if (stepId === 'PROCESSING') return 'active';
      return 'pending';
    }
    if (currentStage === 'INITIATED' || currentStage === 'CONNECTED') {
      if (stepId === 'INITIATED') return 'active';
      return 'pending';
    }
    return 'idle';
  };

  return (
    <div className="p-6 rounded-2xl border border-border/80 bg-card/70 backdrop-blur-md space-y-6 shadow-sm">
      {/* Header with trigger button */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <h2 className="text-base font-semibold tracking-tight">Repository Synchronization Pipeline</h2>
            <span
              className={`inline-flex items-center gap-1 text-[11px] px-2.5 py-0.5 rounded-full font-medium border ${
                isSyncing
                  ? 'bg-amber-500/10 text-amber-500 border-amber-500/20 animate-pulse'
                  : currentStage === 'COMPLETED'
                  ? 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20'
                  : currentStage === 'FAILED'
                  ? 'bg-rose-500/10 text-rose-500 border-rose-500/20'
                  : 'bg-muted text-muted-foreground border-border/60'
              }`}
            >
              {isSyncing && <RefreshCw className="size-3 animate-spin" />}
              {currentStage === 'COMPLETED' && <CheckCircle2 className="size-3" />}
              {currentStage === 'FAILED' && <AlertCircle className="size-3" />}
              {currentStage}
            </span>
          </div>
        </div>

        <button
          onClick={onTriggerSync}
          disabled={isSyncing}
          className="inline-flex items-center justify-center gap-2 h-9 px-4 text-xs font-medium rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-sm shrink-0"
        >
          <RefreshCw className={`size-3.5 ${isSyncing ? 'animate-spin' : ''}`} />
          {isSyncing ? 'Synchronizing...' : 'Sync Repositories'}
        </button>
      </div>

      {/* Pipeline Stages Visualizer */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 relative">
        {steps.map((step, idx) => {
          const state = getStepState(step.id);
          const Icon = step.icon;

          return (
            <div
              key={step.id}
              className={`p-4 rounded-xl border transition-all duration-300 relative ${
                state === 'active'
                  ? 'border-primary bg-primary/5 ring-1 ring-primary/20 shadow-md'
                  : state === 'completed'
                  ? 'border-emerald-500/40 bg-emerald-500/5'
                  : state === 'failed'
                  ? 'border-rose-500/40 bg-rose-500/5'
                  : 'border-border/50 bg-background/40 opacity-70'
              }`}
            >
              <div className="flex items-center justify-between mb-2">
                <div
                  className={`size-8 rounded-lg flex items-center justify-center ${
                    state === 'active'
                      ? 'bg-primary text-primary-foreground animate-pulse'
                      : state === 'completed'
                      ? 'bg-emerald-500/20 text-emerald-500'
                      : state === 'failed'
                      ? 'bg-rose-500/20 text-rose-500'
                      : 'bg-muted text-muted-foreground'
                  }`}
                >
                  <Icon className="size-4" />
                </div>
                <span className="text-[10px] font-mono text-muted-foreground">Step 0{idx + 1}</span>
              </div>

              <div className="space-y-0.5">
                <h3 className="text-xs font-semibold text-foreground flex items-center gap-1.5">
                  {step.title}
                  {state === 'completed' && <CheckCircle2 className="size-3 text-emerald-500 inline" />}
                </h3>
                <p className="text-[11px] text-muted-foreground">{step.description}</p>
              </div>

              {idx < steps.length - 1 && (
                <div className="hidden md:block absolute -right-3 top-1/2 -translate-y-1/2 z-10 text-muted-foreground/40">
                  <ArrowRight className="size-4" />
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Progress & Error Diagnostics */}
      {errorMessage && (
        <div className="p-3 rounded-lg border border-rose-500/30 bg-rose-500/10 text-rose-500 text-xs flex items-center gap-2">
          <AlertCircle className="size-4 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      {lastSyncedAt && (
        <div className="flex items-center justify-between text-[11px] text-muted-foreground pt-1 border-t border-border/40">
          <span className="flex items-center gap-1">
            <Clock className="size-3" /> Last synchronized: {lastSyncedAt.toLocaleTimeString()} ({lastSyncedAt.toLocaleDateString()})
          </span>
          <span className="font-mono text-[10px]">SSE_CHANNEL_ACTIVE</span>
        </div>
      )}
    </div>
  );
};
