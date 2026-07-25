import type { ReactNode } from 'react';
import { UsersIcon } from './icons';

/** App shell: top bar with branding and a centered content area. */
export function Layout({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen bg-slate-50">
      <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center gap-3 px-4 py-3 sm:px-6">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-white">
            <span className="text-lg font-bold">A</span>
          </div>
          <div className="leading-tight">
            <p className="text-sm font-semibold text-slate-800">AutoFlow</p>
            <p className="text-xs text-slate-400">Workshop management</p>
          </div>
          <nav className="ml-8 hidden sm:block">
            <span className="inline-flex items-center gap-2 rounded-lg bg-brand-50 px-3 py-1.5 text-sm font-medium text-brand-700">
              <UsersIcon width={16} height={16} />
              Customers
            </span>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  );
}
