import type { ReactNode } from 'react';
import { CarIcon, LogoutIcon, UsersIcon, WrenchIcon } from './icons';
import { useAuth } from '../auth/AuthContext';

export type View = 'customers' | 'vehicles' | 'workorders';

interface LayoutProps {
  view: View;
  onNavigate: (view: View) => void;
  children: ReactNode;
}

const tabs: { id: View; label: string; icon: typeof UsersIcon }[] = [
  { id: 'customers', label: 'Kunder', icon: UsersIcon },
  { id: 'vehicles', label: 'Kjøretøy', icon: CarIcon },
  { id: 'workorders', label: 'Arbeidsordre', icon: WrenchIcon },
];

/** App shell: top bar with branding, tab navigation and the logged-in user. */
export function Layout({ view, onNavigate, children }: LayoutProps) {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center gap-6 px-4 py-3 sm:px-6">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-white">
              <span className="text-lg font-bold">A</span>
            </div>
            <div className="leading-tight">
              <p className="text-sm font-semibold text-slate-800">AutoFlow</p>
              <p className="text-xs text-slate-400">Verkstedsystem</p>
            </div>
          </div>

          <nav className="flex items-center gap-1">
            {tabs.map(({ id, label, icon: Icon }) => {
              const active = view === id;
              return (
                <button
                  key={id}
                  onClick={() => onNavigate(id)}
                  className={`inline-flex items-center gap-2 rounded-lg px-3 py-1.5 text-sm font-medium transition-colors
                    ${
                      active
                        ? 'bg-brand-50 text-brand-700'
                        : 'text-slate-500 hover:bg-slate-100 hover:text-slate-700'
                    }`}
                >
                  <Icon width={16} height={16} />
                  {label}
                </button>
              );
            })}
          </nav>

          <div className="ml-auto flex items-center gap-3">
            {user && (
              <span className="hidden text-sm text-slate-500 sm:inline">
                {user.fullName}
              </span>
            )}
            <button
              onClick={logout}
              className="inline-flex items-center gap-2 rounded-lg px-3 py-1.5 text-sm font-medium text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700"
            >
              <LogoutIcon width={16} height={16} />
              <span className="hidden sm:inline">Logg ut</span>
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  );
}
