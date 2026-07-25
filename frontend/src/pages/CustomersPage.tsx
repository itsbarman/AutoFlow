import { useEffect, useMemo, useState } from 'react';
import { customerApi } from '../api/customers';
import { ApiRequestError } from '../api/client';
import type { Customer, CustomerInput, ValidationError } from '../types/customer';
import { Button } from '../components/Button';
import { Modal } from '../components/Modal';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { CustomerForm } from '../components/CustomerForm';
import { CustomerTable } from '../components/CustomerTable';
import { PlusIcon, SearchIcon } from '../components/icons';
import { useToast } from '../components/ToastProvider';

export function CustomersPage() {
  const toast = useToast();

  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [search, setSearch] = useState('');

  // Create/edit modal state.
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<Customer | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [serverErrors, setServerErrors] = useState<ValidationError[]>([]);

  // Delete confirmation state.
  const [deleting, setDeleting] = useState<Customer | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  async function loadCustomers() {
    setLoading(true);
    setLoadError(null);
    try {
      setCustomers(await customerApi.list());
    } catch (err) {
      setLoadError(err instanceof Error ? err.message : 'Failed to load customers');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadCustomers();
  }, []);

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return customers;
    return customers.filter((c) =>
      [c.firstName, c.lastName, c.email ?? '', c.phoneNumber, c.city ?? '']
        .join(' ')
        .toLowerCase()
        .includes(term),
    );
  }, [customers, search]);

  function openCreate() {
    setEditing(null);
    setServerErrors([]);
    setFormOpen(true);
  }

  function openEdit(customer: Customer) {
    setEditing(customer);
    setServerErrors([]);
    setFormOpen(true);
  }

  async function handleSubmit(input: CustomerInput) {
    setSubmitting(true);
    setServerErrors([]);
    try {
      if (editing) {
        const updated = await customerApi.update(editing.id, input);
        setCustomers((list) => list.map((c) => (c.id === updated.id ? updated : c)));
        toast.success('Customer updated');
      } else {
        const created = await customerApi.create(input);
        setCustomers((list) => [...list, created]);
        toast.success('Customer created');
      }
      setFormOpen(false);
    } catch (err) {
      if (err instanceof ApiRequestError && err.validationErrors.length > 0) {
        setServerErrors(err.validationErrors);
      } else {
        toast.error(err instanceof Error ? err.message : 'Something went wrong');
      }
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete() {
    if (!deleting) return;
    setDeleteLoading(true);
    try {
      await customerApi.remove(deleting.id);
      setCustomers((list) => list.filter((c) => c.id !== deleting.id));
      toast.success('Customer deleted');
      setDeleting(null);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Failed to delete customer');
    } finally {
      setDeleteLoading(false);
    }
  }

  return (
    <>
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800">Customers</h1>
          <p className="text-sm text-slate-500">
            {customers.length} {customers.length === 1 ? 'customer' : 'customers'} registered
          </p>
        </div>
        <Button onClick={openCreate}>
          <PlusIcon width={18} height={18} />
          New customer
        </Button>
      </div>

      <div className="mb-4 relative max-w-sm">
        <SearchIcon
          width={18}
          height={18}
          className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
        />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search customers..."
          className="w-full rounded-lg border border-slate-300 py-2 pl-10 pr-3 text-sm shadow-sm
            focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
        />
      </div>

      {loading ? (
        <div className="flex items-center justify-center rounded-xl border border-slate-200 bg-white py-20 text-slate-400">
          <span className="h-6 w-6 animate-spin rounded-full border-2 border-slate-300 border-t-brand-600" />
        </div>
      ) : loadError ? (
        <div className="rounded-xl border border-red-200 bg-red-50 px-6 py-12 text-center">
          <p className="font-medium text-red-700">{loadError}</p>
          <p className="mb-4 mt-1 text-sm text-red-500">
            Make sure the API is running on the configured address.
          </p>
          <Button variant="secondary" onClick={() => void loadCustomers()}>
            Try again
          </Button>
        </div>
      ) : (
        <CustomerTable customers={filtered} onEdit={openEdit} onDelete={setDeleting} />
      )}

      <Modal
        open={formOpen}
        title={editing ? 'Edit customer' : 'New customer'}
        onClose={() => setFormOpen(false)}
      >
        <CustomerForm
          initial={editing}
          submitting={submitting}
          serverErrors={serverErrors}
          onSubmit={handleSubmit}
          onCancel={() => setFormOpen(false)}
        />
      </Modal>

      <ConfirmDialog
        open={deleting !== null}
        title="Delete customer"
        message={
          deleting
            ? `Are you sure you want to delete ${deleting.firstName} ${deleting.lastName}? This cannot be undone.`
            : ''
        }
        loading={deleteLoading}
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </>
  );
}
