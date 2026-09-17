import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { BackgroundAnimation } from '../../components/landing/BackgroundAnimation';
import { CheckCircle2, ArrowRight, Loader2 } from 'lucide-react';

export const OAuthSuccessPage: React.FC = () => {
  const { checkAuth } = useAuth();
  const [verifying, setVerifying] = useState<boolean>(true);
  const navigate = useNavigate();

  useEffect(() => {
    let isMounted = true;
    const verifySession = async () => {
      try {
        await checkAuth();
      } catch {
        // Ignore auth failure
      } finally {
        if (isMounted) {
          setVerifying(false);
        }
      }
    };

    verifySession();
    return () => {
      isMounted = false;
    };
  }, [checkAuth]);

  return (
    <div className="min-h-screen flex flex-col justify-center items-center bg-background text-foreground relative px-4 selection:bg-emerald-500/20">
      <BackgroundAnimation />

      <div className="relative z-10 w-full max-w-md p-8 rounded-2xl border border-border/80 bg-card/90 backdrop-blur-md shadow-2xl text-center space-y-6 animate-in fade-in zoom-in-95">
        {/* Top Green Circular Check Icon */}
        <div className="size-16 rounded-full bg-emerald-950/60 border border-emerald-500/40 flex items-center justify-center text-emerald-400 mx-auto">
          {verifying ? (
            <Loader2 className="size-8 animate-spin text-emerald-400" />
          ) : (
            <CheckCircle2 className="size-8 text-emerald-400" />
          )}
        </div>

        {/* Title and Subtitle */}
        <div className="space-y-2">
          <h1 className="text-2xl font-bold text-white tracking-tight">
            {verifying ? 'Verifying Session...' : 'Authentication Successful'}
          </h1>
          <p className="text-xs text-muted-foreground leading-relaxed px-2">
            {verifying
              ? 'Validating your security session with server...'
              : 'Your GitHub account has been authenticated. Redirecting to your Repo Pilot workspace...'}
          </p>
        </div>

        {/* Divider */}
        <div className="border-t border-border/40 pt-2" />

        {/* Full-width Proceed to Workspace Button */}
        <button
          onClick={() => navigate('/dashboard')}
          disabled={verifying}
          className="w-full h-11 bg-white hover:bg-neutral-200 text-black font-bold text-sm rounded-xl flex items-center justify-center gap-2 transition-all cursor-pointer shadow-md disabled:opacity-50 disabled:cursor-not-allowed active:scale-[0.98]"
        >
          <span>Proceed to Workspace</span>
          <ArrowRight className="size-4" />
        </button>
      </div>
    </div>
  );
};
