import { Layout } from './components/Layout';
import { ToastProvider } from './components/ToastProvider';
import { CustomersPage } from './pages/CustomersPage';

export default function App() {
  return (
    <ToastProvider>
      <Layout>
        <CustomersPage />
      </Layout>
    </ToastProvider>
  );
}
