import { ArrowLeft } from 'lucide-react';
import { cn } from '@/lib/utils';

interface MobileBackButtonProps {
  onClick: () => void;
  className?: string;
}

const MobileBackButton = ({ onClick, className }: MobileBackButtonProps) => (
  <button
    onClick={onClick}
    aria-label="Go back"
    className={cn(
      "w-12 h-12 min-w-[48px] min-h-[48px] rounded-full bg-white/20 backdrop-blur flex items-center justify-center",
      className
    )}
  >
    <ArrowLeft className="w-6 h-6 text-white" />
  </button>
);

export default MobileBackButton;
