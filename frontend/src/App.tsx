import { useState } from 'react';
import { Layout, type View } from './components/Layout';
import { ToastProvider } from './components/ToastProvider';
import { CustomersPage } from './pages/CustomersPage';
import { VehiclesPage } from './pages/VehiclesPage';
import { WorkOrdersPage } from './pages/WorkOrdersPage';

const pages: Record<View, JSX.Element> = {
  customers: <CustomersPage />,
  vehicles: <VehiclesPage />,
  workorders: <WorkOrdersPage />,
};

export default function App() {
  const [view, setView] = useState<View>('customers');

  return (
    <ToastProvider>
      <Layout view={view} onNavigate={setView}>
        {pages[view]}
      </Layout>
    </ToastProvider>
  );
}
