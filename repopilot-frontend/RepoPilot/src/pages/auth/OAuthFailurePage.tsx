import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { AlertCircle, RefreshCw, Home } from 'lucide-react';
import { Button } from '../../components/ui/button';
import { authService } from '../../services/api/authService';
import { BackgroundAnimation } from '../../components/landing/BackgroundAnimation';

export const OAuthFailurePage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const errorCode = searchParams.get('error') || 'oauth2_authentication_failed';
  const errorMessage = searchParams.get('message') || 'GitHub OAuth2 authentication was cancelled or failed.';

  const handleRetry = () => {
    window.location.href = authService.getLoginUrl();
  };

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col items-center justify-center p-6 font-sans relative selection:bg-red-500/20">
      <BackgroundAnimation />

      <div className="relative z-10 max-w-md w-full rounded-2xl border border-red-500/30 bg-card/90 backdrop-blur-md p-8 text-center shadow-xl space-y-6 animate-in fade-in zoom-in-95">
        <div className="inline-flex items-center justify-center size-16 rounded-full bg-red-500/10 border border-red-500/30 text-red-500 mb-2 mx-auto">
          <AlertCircle className="size-8 text-red-500" />
        </div>

        <div className="space-y-2">
          <h2 className="text-2xl font-bold text-foreground font-heading">Authentication Failed</h2>
          <p className="text-xs text-muted-foreground font-normal">
            We were unable to authenticate your GitHub account.
          </p>
        </div>

        <div className="p-3 rounded-xl bg-background/80 border border-border text-left font-mono text-xs space-y-1">
          <div className="text-[10px] text-muted-foreground uppercase font-bold tracking-wider">Error Code:</div>
          <div className="text-red-400 font-semibold">{errorCode}</div>
          <div className="text-[10px] text-muted-foreground uppercase font-bold tracking-wider pt-1">Reason:</div>
          <div className="text-foreground text-[11px] font-normal">{decodeURIComponent(errorMessage)}</div>
        </div>

        <div className="flex items-center gap-3 pt-2">
          <Button
            variant="outline"
            onClick={() => navigate('/', { replace: true })}
            className="flex-1 gap-2 text-xs font-semibold cursor-pointer"
          >
            <Home className="size-4" />
            <span>Home</span>
          </Button>

          <Button
            variant="default"
            onClick={handleRetry}
            className="flex-1 gap-2 text-xs font-semibold cursor-pointer"
          >
            <RefreshCw className="size-4" />
            <span>Retry Login</span>
          </Button>
        </div>
      </div>
    </div>
  );
};
