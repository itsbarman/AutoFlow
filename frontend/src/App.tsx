import { useState } from 'react';
import { Layout, type View } from './components/Layout';
import { ToastProvider } from './components/ToastProvider';
import { CustomersPage } from './pages/CustomersPage';
import { VehiclesPage } from './pages/VehiclesPage';

export default function App() {
  const [view, setView] = useState<View>('customers');

  return (
    <ToastProvider>
      <Layout view={view} onNavigate={setView}>
        {view === 'customers' ? <CustomersPage /> : <VehiclesPage />}
      </Layout>
    </ToastProvider>
  );
}
