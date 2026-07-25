import { useEffect, useMemo, useState } from 'react';
import { vehicleApi } from '../api/vehicles';
import { customerApi } from '../api/customers';
import { ApiRequestError } from '../api/client';
import type { Customer, ValidationError } from '../types/customer';
import type { Vehicle, VehicleInput } from '../types/vehicle';
import { FUEL_TYPE_LABELS } from '../types/vehicle';
import { Button } from '../components/Button';
import { Modal } from '../components/Modal';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { VehicleForm } from '../components/VehicleForm';
import { VehicleTable } from '../components/VehicleTable';
import { PlusIcon, SearchIcon } from '../components/icons';
import { useToast } from '../components/ToastProvider';

export function VehiclesPage() {
  const toast = useToast();

  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [search, setSearch] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<Vehicle | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [serverErrors, setServerErrors] = useState<ValidationError[]>([]);

  const [deleting, setDeleting] = useState<Vehicle | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  async function loadData() {
    setLoading(true);
    setLoadError(null);
    try {
      const [vehicleList, customerList] = await Promise.all([
        vehicleApi.list(),
        customerApi.list(),
      ]);
      setVehicles(vehicleList);
      setCustomers(customerList);
    } catch (err) {
      setLoadError(err instanceof Error ? err.message : 'Failed to load vehicles');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadData();
  }, []);

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return vehicles;
    return vehicles.filter((v) =>
      [v.registrationNumber, v.make, v.model, v.vin ?? '', v.customerName, FUEL_TYPE_LABELS[v.fuelType]]
        .join(' ')
        .toLowerCase()
        .includes(term),
    );
  }, [vehicles, search]);

  function openCreate() {
    setEditing(null);
    setServerErrors([]);
    setFormOpen(true);
  }

  function openEdit(vehicle: Vehicle) {
    setEditing(vehicle);
    setServerErrors([]);
    setFormOpen(true);
  }

  async function handleSubmit(input: VehicleInput, customerId: number) {
    setSubmitting(true);
    setServerErrors([]);
    try {
      if (editing) {
        const updated = await vehicleApi.update(editing.id, input);
        setVehicles((list) => list.map((v) => (v.id === updated.id ? updated : v)));
        toast.success('Vehicle updated');
      } else {
        const created = await vehicleApi.create(customerId, input);
        setVehicles((list) => [...list, created]);
        toast.success('Vehicle registered');
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
      await vehicleApi.remove(deleting.id);
      setVehicles((list) => list.filter((v) => v.id !== deleting.id));
      toast.success('Vehicle deleted');
      setDeleting(null);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Failed to delete vehicle');
    } finally {
      setDeleteLoading(false);
    }
  }

  const noCustomers = !loading && customers.length === 0;

  return (
    <>
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800">Vehicles</h1>
          <p className="text-sm text-slate-500">
            {vehicles.length} {vehicles.length === 1 ? 'vehicle' : 'vehicles'} registered
          </p>
        </div>
        <Button onClick={openCreate} disabled={noCustomers} title={noCustomers ? 'Add a customer first' : undefined}>
          <PlusIcon width={18} height={18} />
          New vehicle
        </Button>
      </div>

      {noCustomers && (
        <div className="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
          You need at least one customer before you can register a vehicle.
        </div>
      )}

      <div className="mb-4 relative max-w-sm">
        <SearchIcon
          width={18}
          height={18}
          className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
        />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search vehicles..."
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
          <Button variant="secondary" onClick={() => void loadData()}>
            Try again
          </Button>
        </div>
      ) : (
        <VehicleTable vehicles={filtered} onEdit={openEdit} onDelete={setDeleting} />
      )}

      <Modal
        open={formOpen}
        title={editing ? 'Edit vehicle' : 'New vehicle'}
        onClose={() => setFormOpen(false)}
      >
        <VehicleForm
          initial={editing}
          customers={customers}
          submitting={submitting}
          serverErrors={serverErrors}
          onSubmit={handleSubmit}
          onCancel={() => setFormOpen(false)}
        />
      </Modal>

      <ConfirmDialog
        open={deleting !== null}
        title="Delete vehicle"
        message={
          deleting
            ? `Are you sure you want to delete ${deleting.make} ${deleting.model} (${deleting.registrationNumber})? This cannot be undone.`
            : ''
        }
        loading={deleteLoading}
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </>
  );
}
