import React from 'react';
import { useTheme } from '../../context/ThemeContext';
import { Button } from './button';
import { Sun, Moon } from 'lucide-react';

interface ThemeToggleProps {
  className?: string;
  size?: 'default' | 'xs' | 'sm' | 'lg' | 'icon' | 'icon-xs' | 'icon-sm' | 'icon-lg';
}

export const ThemeToggle: React.FC<ThemeToggleProps> = ({
  className = '',
  size = 'icon-sm',
}) => {
  const { resolvedTheme, toggleTheme } = useTheme();

  return (
    <Button
      variant="outline"
      size={size}
      onClick={toggleTheme}
      className={`relative rounded-md transition-colors cursor-pointer ${className}`}
      title={`Switch to ${resolvedTheme === 'dark' ? 'light' : 'dark'} mode`}
      aria-label="Toggle theme"
    >
      <Sun className="size-4 rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0 text-amber-500" />
      <Moon className="absolute size-4 rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100 text-blue-400" />
    </Button>
  );
};
