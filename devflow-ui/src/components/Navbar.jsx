import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { useDarkMode } from '../hooks/useDarkMode';

export default function Navbar() {
    const {user, logout} = useAuth();
    const navigate = useNavigate();
    const [isDark, setIsDark] = useDarkMode();

    const handleLogout = () => {
        logout();
        toast.success('Signed out');
        navigate('/login');
    };

    const initials = user?.name?.split(' ').map(n => n[0]).join('')
                     .toUpperCase().slice(0, 2);

     return (
    <nav className="h-14 bg-[var(--surface)] border-b border-[var(--border)] flex items-center justify-between px-6 sticky top-0 z-40">
      <Link to="/projects" className="flex items-center gap-2">
        <div className="w-7 h-7 bg-primary-500 rounded-md flex items-center justify-center">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="white">
            <path d="M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"/>
          </svg>
        </div>
        <span className="text-sm font-semibold text-[var(--text)]">DevFlow</span>
      </Link>

      <div className="flex items-center gap-2">
        {/* Dark mode toggle */}
        <button
          onClick={() => setIsDark(!isDark)}
          className="p-1.5 rounded-lg hover:bg-[var(--border)] text-[var(--text-secondary)] transition-colors"
        >
          {isDark ? (
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1zM5.99 4.58c-.39-.39-1.03-.39-1.41 0-.39.39-.39 1.03 0 1.41l1.06 1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41L5.99 4.58zm12.37 12.37c-.39-.39-1.03-.39-1.41 0-.39.39-.39 1.03 0 1.41l1.06 1.06c.39.39 1.03.39 1.41 0 .39-.39.39-1.03 0-1.41l-1.06-1.06zm1.06-12.37l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06c.39-.39.39-1.03 0-1.41s-1.03-.39-1.41 0zM7.05 18.36l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06c.39-.39.39-1.03 0-1.41s-1.03-.39-1.41 0z"/>
            </svg>
          ) : (
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z"/>
            </svg>
          )}
        </button>

        <span className="text-xs text-[var(--text-secondary)] hidden sm:block">{user?.email}</span>

        <div className="w-8 h-8 bg-primary-500 rounded-full flex items-center justify-center text-white text-xs font-semibold">
          {initials}
        </div>

        <button
          onClick={handleLogout}
          className="text-xs text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors px-2 py-1 rounded hover:bg-[var(--border)]"
        >
          Sign out
        </button>
      </div>
    </nav>
  );
}