import { useEffect, useMemo, useState } from 'react';
import { workOrderApi } from '../api/workOrders';
import { vehicleApi } from '../api/vehicles';
import { ApiRequestError } from '../api/client';
import type { ValidationError } from '../types/customer';
import type { Vehicle } from '../types/vehicle';
import {
  STATUS_LABELS,
  PRIORITY_LABELS,
  type WorkOrder,
  type WorkOrderInput,
  type WorkOrderStatus,
} from '../types/workOrder';
import { Button } from '../components/Button';
import { Modal } from '../components/Modal';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { WorkOrderForm } from '../components/WorkOrderForm';
import { WorkOrderTable } from '../components/WorkOrderTable';
import { WorkOrderDetail } from '../components/WorkOrderDetail';
import { PlusIcon, SearchIcon } from '../components/icons';
import { useToast } from '../components/ToastProvider';

export function WorkOrdersPage() {
  const toast = useToast();

  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [search, setSearch] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<WorkOrder | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [serverErrors, setServerErrors] = useState<ValidationError[]>([]);

  const [deleting, setDeleting] = useState<WorkOrder | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const [viewing, setViewing] = useState<WorkOrder | null>(null);

  async function loadData() {
    setLoading(true);
    setLoadError(null);
    try {
      const [orderList, vehicleList] = await Promise.all([
        workOrderApi.list(),
        vehicleApi.list(),
      ]);
      setWorkOrders(orderList);
      setVehicles(vehicleList);
    } catch (err) {
      setLoadError(err instanceof Error ? err.message : 'Kunne ikke laste arbeidsordre');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadData();
  }, []);

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return workOrders;
    return workOrders.filter((w) =>
      [
        w.workOrderNumber,
        w.title,
        w.vehicleRegistrationNumber,
        w.customerName,
        STATUS_LABELS[w.status],
        PRIORITY_LABELS[w.priority],
      ]
        .join(' ')
        .toLowerCase()
        .includes(term),
    );
  }, [workOrders, search]);

  function openCreate() {
    setEditing(null);
    setServerErrors([]);
    setFormOpen(true);
  }

  function openEdit(workOrder: WorkOrder) {
    setEditing(workOrder);
    setServerErrors([]);
    setFormOpen(true);
  }

  async function handleSubmit(input: WorkOrderInput, vehicleId: number) {
    setSubmitting(true);
    setServerErrors([]);
    try {
      if (editing) {
        const updated = await workOrderApi.update(editing.id, input);
        setWorkOrders((list) => list.map((w) => (w.id === updated.id ? updated : w)));
        toast.success('Arbeidsordre oppdatert');
      } else {
        const created = await workOrderApi.create(vehicleId, input);
        setWorkOrders((list) => [...list, created]);
        toast.success('Arbeidsordre opprettet');
      }
      setFormOpen(false);
    } catch (err) {
      if (err instanceof ApiRequestError && err.validationErrors.length > 0) {
        setServerErrors(err.validationErrors);
      } else {
        toast.error(err instanceof Error ? err.message : 'Noe gikk galt');
      }
    } finally {
      setSubmitting(false);
    }
  }

  async function handleStatusChange(workOrder: WorkOrder, status: WorkOrderStatus) {
    if (workOrder.status === status) return;
    try {
      const updated = await workOrderApi.updateStatus(workOrder.id, status);
      setWorkOrders((list) => list.map((w) => (w.id === updated.id ? updated : w)));
      toast.success(`Status satt til ${STATUS_LABELS[status]}`);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Kunne ikke endre status');
    }
  }

  async function handleDelete() {
    if (!deleting) return;
    setDeleteLoading(true);
    try {
      await workOrderApi.remove(deleting.id);
      setWorkOrders((list) => list.filter((w) => w.id !== deleting.id));
      toast.success('Arbeidsordre slettet');
      setDeleting(null);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Kunne ikke slette arbeidsordre');
    } finally {
      setDeleteLoading(false);
    }
  }

  const noVehicles = !loading && vehicles.length === 0;

  return (
    <>
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800">Arbeidsordre</h1>
          <p className="text-sm text-slate-500">
            {workOrders.length} {workOrders.length === 1 ? 'arbeidsordre' : 'arbeidsordre'}
          </p>
        </div>
        <Button onClick={openCreate} disabled={noVehicles} title={noVehicles ? 'Registrer et kjøretøy først' : undefined}>
          <PlusIcon width={18} height={18} />
          Ny arbeidsordre
        </Button>
      </div>

      {noVehicles && (
        <div className="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
          Du må ha minst ett kjøretøy før du kan opprette en arbeidsordre.
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
          placeholder="Søk arbeidsordre..."
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
            Sjekk at API-et kjører på den konfigurerte adressen.
          </p>
          <Button variant="secondary" onClick={() => void loadData()}>
            Prøv igjen
          </Button>
        </div>
      ) : (
        <WorkOrderTable
          workOrders={filtered}
          onView={setViewing}
          onEdit={openEdit}
          onDelete={setDeleting}
          onStatusChange={handleStatusChange}
        />
      )}

      <Modal
        open={formOpen}
        title={editing ? `Rediger ${editing.workOrderNumber}` : 'Ny arbeidsordre'}
        onClose={() => setFormOpen(false)}
      >
        <WorkOrderForm
          initial={editing}
          vehicles={vehicles}
          submitting={submitting}
          serverErrors={serverErrors}
          onSubmit={handleSubmit}
          onCancel={() => setFormOpen(false)}
        />
      </Modal>

      <ConfirmDialog
        open={deleting !== null}
        title="Slett arbeidsordre"
        message={
          deleting
            ? `Er du sikker på at du vil slette ${deleting.workOrderNumber} (${deleting.title})? Dette kan ikke angres.`
            : ''
        }
        loading={deleteLoading}
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />

      {viewing && <WorkOrderDetail workOrder={viewing} onClose={() => setViewing(null)} />}
    </>
  );
}
