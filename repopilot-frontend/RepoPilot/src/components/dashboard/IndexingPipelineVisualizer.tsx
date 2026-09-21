import React, { useEffect, useState, useRef } from 'react';
import { indexingService } from '../../services/api/indexingService';
import {
  Cpu,
  CheckCircle2,
  XCircle,
  Loader2,
  Sparkles,
  Layers,
  FileCode,
  X,
  RefreshCw
} from 'lucide-react';
import { Button } from '../ui/button';

interface IndexingPipelineVisualizerProps {
  repositoryId: string | null;
  repositoryName: string | null;
  isOpen: boolean;
  onClose: () => void;
  onIndexingComplete?: () => void;
}

export type IndexingStage = 'IDLE' | 'CONNECTED' | 'INITIATED' | 'INDEXING' | 'CHUNKING' | 'INDEXED' | 'FAILED';

export const IndexingPipelineVisualizer: React.FC<IndexingPipelineVisualizerProps> = ({
  repositoryId,
  repositoryName,
  isOpen,
  onClose,
  onIndexingComplete,
}) => {
  const [stage, setStage] = useState<IndexingStage>('IDLE');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const eventSourceRef = useRef<EventSource | null>(null);
  const hasTriggeredRef = useRef<boolean>(false);

  const startPipeline = async () => {
    if (!repositoryId) return;

    setErrorMessage(null);
    setStage('CONNECTED');

    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    try {
      // Subscribe to SSE for real-time status stream
      eventSourceRef.current = indexingService.subscribeToIndexingEvents(
        repositoryId,
        (data) => {
          const currentStatus = data.status;

          if (currentStatus === 'CONNECTED') {
            setStage('CONNECTED');
          } else if (currentStatus === 'INITIATED' || currentStatus === 'PENDING') {
            setStage('INITIATED');
          } else if (currentStatus === 'INDEXING') {
            setStage('INDEXING');
          } else if (currentStatus === 'CHUNKING') {
            setStage('CHUNKING');
          } else if (currentStatus === 'INDEXED' || currentStatus === 'COMPLETED') {
            setStage('INDEXED');
            onIndexingComplete?.();
            if (eventSourceRef.current) {
              eventSourceRef.current.close();
            }
          } else if (currentStatus === 'FAILED') {
            setStage('FAILED');
            setErrorMessage(data.message || 'Indexing pipeline encountered an error.');
            if (eventSourceRef.current) {
              eventSourceRef.current.close();
            }
          }
        },
        (err) => {
          console.warn('SSE notice (browser auto-reconnecting):', err);
        }
      );

      // Trigger backend indexing
      await indexingService.startIndexing(repositoryId);
      setStage((prev) => (prev === 'CONNECTED' ? 'INITIATED' : prev));
    } catch (err: any) {
      if (err.message && err.message.includes('already being indexed')) {
        setStage('INDEXING');
      } else {
        console.error('Failed to start indexing:', err);
        setStage('FAILED');
        setErrorMessage(err.message || 'Failed to trigger repository indexing.');
      }
    }
  };

  useEffect(() => {
    if (isOpen && repositoryId && !hasTriggeredRef.current) {
      hasTriggeredRef.current = true;
      startPipeline();
    }
    if (!isOpen) {
      hasTriggeredRef.current = false;
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    }

    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, [isOpen, repositoryId]);

  const handleClose = () => {
    if (stage === 'INDEXED') {
      onIndexingComplete?.();
    }
    onClose();
  };

  if (!isOpen) return null;

  const steps = [
    { key: 'CONNECTED', label: 'Connect', icon: Cpu, desc: 'Establish SSE Stream' },
    { key: 'INITIATED', label: 'Initiate', icon: RefreshCw, desc: 'Queue Repository Ingestion' },
    { key: 'INDEXING', label: 'Ingest & Parse', icon: FileCode, desc: 'Extract Code AST & Chunks' },
    { key: 'CHUNKING', label: 'Vector Store', icon: Layers, desc: 'Generate Vector Embeddings' },
    { key: 'INDEXED', label: 'Ready', icon: Sparkles, desc: 'Enable RepoPilot AI Chat' },
  ];

  const getStepIndex = (st: IndexingStage) => {
    switch (st) {
      case 'CONNECTED': return 0;
      case 'INITIATED': return 1;
      case 'INDEXING': return 2;
      case 'CHUNKING': return 3;
      case 'INDEXED': return 4;
      case 'FAILED': return -1;
      default: return 0;
    }
  };

  const activeIndex = getStepIndex(stage);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4 animate-in fade-in duration-200">
      <div className="relative w-full max-w-2xl rounded-2xl border border-border/80 bg-card p-6 shadow-2xl space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-border/40 pb-4">
          <div className="flex items-center gap-2.5">
            <div className="size-9 rounded-xl bg-primary/10 border border-primary/20 flex items-center justify-center text-primary">
              <Cpu className="size-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-foreground flex items-center gap-2">
                Real-Time Indexing Pipeline
                <span className="text-xs px-2 py-0.5 rounded-full bg-primary/10 text-primary border border-primary/20 font-mono">
                  {repositoryName}
                </span>
              </h2>
              <p className="text-xs text-muted-foreground">
                Parsing code, generating AST, creating vector embeddings, and populating Qdrant.
              </p>
            </div>
          </div>
          <button
            onClick={handleClose}
            className="p-1.5 rounded-lg hover:bg-muted text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
          >
            <X className="size-4" />
          </button>
        </div>

        {/* Pipeline Stepper Visualizer */}
        <div className="grid grid-cols-5 gap-2 relative">
          {steps.map((step, idx) => {
            const Icon = step.icon;
            const isCompleted = activeIndex > idx || stage === 'INDEXED';
            const isCurrent = activeIndex === idx && stage !== 'INDEXED' && stage !== 'FAILED';
            const isFailed = stage === 'FAILED' && activeIndex === idx;

            return (
              <div
                key={step.key}
                className={`p-3 rounded-xl border flex flex-col items-center text-center transition-all ${
                  isCompleted
                    ? 'border-emerald-500/40 bg-emerald-500/5 text-emerald-500'
                    : isCurrent
                    ? 'border-primary bg-primary/10 text-primary shadow-sm ring-1 ring-primary/30'
                    : isFailed
                    ? 'border-rose-500/40 bg-rose-500/5 text-rose-500'
                    : 'border-border/40 bg-muted/20 text-muted-foreground'
                }`}
              >
                <div className="size-8 rounded-lg flex items-center justify-center mb-1.5 font-bold">
                  {isCompleted ? (
                    <CheckCircle2 className="size-5 text-emerald-500" />
                  ) : isCurrent ? (
                    <Loader2 className="size-5 animate-spin text-primary" />
                  ) : isFailed ? (
                    <XCircle className="size-5 text-rose-500" />
                  ) : (
                    <Icon className="size-4" />
                  )}
                </div>
                <span className="text-xs font-bold truncate max-w-full">{step.label}</span>
                <span className="text-[10px] text-muted-foreground/80 hidden sm:block truncate max-w-full">
                  {step.desc}
                </span>
              </div>
            );
          })}
        </div>

        {/* Error Banner if pipeline fails */}
        {errorMessage && (
          <div className="p-3.5 rounded-xl border border-rose-500/30 bg-rose-500/10 text-rose-500 text-xs flex items-center gap-2.5 font-medium">
            <XCircle className="size-4 shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}


        {/* Footer Actions */}
        <div className="flex items-center justify-end gap-2.5 pt-2 border-t border-border/40">
          {stage === 'FAILED' && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                hasTriggeredRef.current = false;
                startPipeline();
              }}
              className="gap-2 text-xs font-semibold cursor-pointer"
            >
              <RefreshCw className="size-3.5 text-primary" /> Retry Indexing
            </Button>
          )}

          <Button
            onClick={handleClose}
            variant={stage === 'INDEXED' ? 'default' : 'outline'}
            size="sm"
            className="text-xs font-semibold cursor-pointer"
          >
            {stage === 'INDEXED' ? 'Done' : 'Close'}
          </Button>
        </div>
      </div>
    </div>
  );
};
