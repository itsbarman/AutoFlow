import { useState } from 'react';
import { Layout, type View } from './components/Layout';
import { ToastProvider } from './components/ToastProvider';
import { AuthProvider, useAuth } from './auth/AuthContext';
import { CustomersPage } from './pages/CustomersPage';
import { VehiclesPage } from './pages/VehiclesPage';
import { WorkOrdersPage } from './pages/WorkOrdersPage';
import { LoginPage } from './pages/LoginPage';

const pages: Record<View, JSX.Element> = {
  customers: <CustomersPage />,
  vehicles: <VehiclesPage />,
  workorders: <WorkOrdersPage />,
};

function AppShell() {
  const { user, loading } = useAuth();
  const [view, setView] = useState<View>('customers');

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50">
        <span className="h-8 w-8 animate-spin rounded-full border-2 border-slate-300 border-t-brand-600" />
      </div>
    );
  }

  if (!user) {
    return <LoginPage />;
  }

  return (
    <Layout view={view} onNavigate={setView}>
      {pages[view]}
    </Layout>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <AppShell />
      </ToastProvider>
    </AuthProvider>
  );
}
