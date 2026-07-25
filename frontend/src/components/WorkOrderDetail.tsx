import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { workOrderLineApi } from '../api/workOrderLines';
import { Modal } from './Modal';
import { Button } from './Button';
import { TextField } from './TextField';
import { TrashIcon } from './icons';
import { useToast } from './ToastProvider';
import {
  PRIORITY_LABELS,
  STATUS_LABELS,
  STATUS_STYLES,
  type WorkOrder,
} from '../types/workOrder';
import type { WorkOrderLine, WorkOrderLineInput } from '../types/workOrderLine';

interface WorkOrderDetailProps {
  workOrder: WorkOrder;
  onClose: () => void;
}

const currency = new Intl.NumberFormat('nb-NO', { style: 'currency', currency: 'NOK' });

function formatDate(iso: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('nb-NO', { year: 'numeric', month: 'short', day: 'numeric' });
}

const EMPTY_LINE: WorkOrderLineInput = { description: '', partNumber: '', quantity: '1', unitPrice: '' };

export function WorkOrderDetail({ workOrder, onClose }: WorkOrderDetailProps) {
  const toast = useToast();
  const [lines, setLines] = useState<WorkOrderLine[]>([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState<WorkOrderLineInput>(EMPTY_LINE);
  const [adding, setAdding] = useState(false);

  useEffect(() => {
    let active = true;
    workOrderLineApi
      .list(workOrder.id)
      .then((data) => {
        if (active) setLines(data);
      })
      .catch((err) => toast.error(err instanceof Error ? err.message : 'Kunne ikke laste varelinjer'))
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [workOrder.id, toast]);

  const total = useMemo(() => lines.reduce((sum, line) => sum + line.lineTotal, 0), [lines]);

  const canAdd =
    form.description.trim() !== '' &&
    form.quantity.trim() !== '' &&
    Number(form.quantity) > 0 &&
    form.unitPrice.trim() !== '' &&
    Number(form.unitPrice) >= 0;

  async function handleAdd(e: FormEvent) {
    e.preventDefault();
    if (!canAdd) return;
    setAdding(true);
    try {
      const created = await workOrderLineApi.create(workOrder.id, form);
      setLines((list) => [...list, created]);
      setForm(EMPTY_LINE);
      toast.success('Varelinje lagt til');
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Kunne ikke legge til varelinje');
    } finally {
      setAdding(false);
    }
  }

  async function handleRemove(line: WorkOrderLine) {
    try {
      await workOrderLineApi.remove(workOrder.id, line.id);
      setLines((list) => list.filter((l) => l.id !== line.id));
      toast.success('Varelinje fjernet');
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Kunne ikke fjerne varelinje');
    }
  }

  const set = (field: keyof WorkOrderLineInput) => (e: { target: { value: string } }) =>
    setForm((f) => ({ ...f, [field]: e.target.value }));

  return (
    <Modal
      open
      onClose={onClose}
      maxWidthClass="max-w-3xl"
      title={`${workOrder.workOrderNumber} — ${workOrder.title}`}
    >
      {/* Summary */}
      <div className="mb-6 grid grid-cols-2 gap-x-6 gap-y-3 text-sm sm:grid-cols-3">
        <Field label="Status">
          <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLES[workOrder.status]}`}>
            {STATUS_LABELS[workOrder.status]}
          </span>
        </Field>
        <Field label="Prioritet">{PRIORITY_LABELS[workOrder.priority]}</Field>
        <Field label="Kjøretøy">
          {workOrder.vehicleRegistrationNumber} · {workOrder.vehicleDescription}
        </Field>
        <Field label="Kunde">{workOrder.customerName}</Field>
        <Field label="Km ved ankomst">
          {workOrder.mileageAtArrival != null ? `${workOrder.mileageAtArrival.toLocaleString('nb-NO')} km` : '—'}
        </Field>
        <Field label="Estimert ferdig">{formatDate(workOrder.estimatedCompletionDate)}</Field>
        <Field label="Opprettet">{formatDate(workOrder.createdAt)}</Field>
        <Field label="Fullført">{formatDate(workOrder.completedAt)}</Field>
      </div>

      {(workOrder.customerComplaint || workOrder.description || workOrder.technicianNotes) && (
        <div className="mb-6 space-y-3 rounded-lg bg-slate-50 p-4 text-sm">
          {workOrder.customerComplaint && (
            <TextBlock label="Kundens beskrivelse">{workOrder.customerComplaint}</TextBlock>
          )}
          {workOrder.description && <TextBlock label="Beskrivelse">{workOrder.description}</TextBlock>}
          {workOrder.technicianNotes && (
            <TextBlock label="Tekniker-notater">{workOrder.technicianNotes}</TextBlock>
          )}
        </div>
      )}

      {/* Lines */}
      <h3 className="mb-2 text-sm font-semibold text-slate-700">Deler og arbeid</h3>
      <div className="overflow-hidden rounded-lg border border-slate-200">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th className="px-3 py-2 font-medium">Beskrivelse</th>
              <th className="px-3 py-2 font-medium">Delenr.</th>
              <th className="px-3 py-2 text-right font-medium">Antall</th>
              <th className="px-3 py-2 text-right font-medium">Enhetspris</th>
              <th className="px-3 py-2 text-right font-medium">Sum</th>
              <th className="px-3 py-2" />
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {lines.map((line) => (
              <tr key={line.id}>
                <td className="px-3 py-2 text-slate-800">{line.description}</td>
                <td className="px-3 py-2 text-slate-500">{line.partNumber ?? '—'}</td>
                <td className="px-3 py-2 text-right text-slate-600">{line.quantity}</td>
                <td className="px-3 py-2 text-right text-slate-600">{currency.format(line.unitPrice)}</td>
                <td className="px-3 py-2 text-right font-medium text-slate-800">
                  {currency.format(line.lineTotal)}
                </td>
                <td className="px-3 py-2 text-right">
                  <button
                    onClick={() => handleRemove(line)}
                    aria-label={`Fjern ${line.description}`}
                    className="rounded p-1 text-slate-400 transition-colors hover:bg-red-50 hover:text-red-600"
                  >
                    <TrashIcon width={16} height={16} />
                  </button>
                </td>
              </tr>
            ))}
            {!loading && lines.length === 0 && (
              <tr>
                <td colSpan={6} className="px-3 py-6 text-center text-sm text-slate-400">
                  Ingen varelinjer ennå.
                </td>
              </tr>
            )}
          </tbody>
          {lines.length > 0 && (
            <tfoot>
              <tr className="border-t border-slate-200 bg-slate-50">
                <td colSpan={4} className="px-3 py-2 text-right text-sm font-semibold text-slate-600">
                  Totalt
                </td>
                <td className="px-3 py-2 text-right text-sm font-bold text-slate-900">
                  {currency.format(total)}
                </td>
                <td />
              </tr>
            </tfoot>
          )}
        </table>
      </div>

      {/* Add line */}
      <form onSubmit={handleAdd} className="mt-4 grid grid-cols-1 items-end gap-3 sm:grid-cols-12">
        <TextField
          id="line-description"
          label="Beskrivelse"
          className="sm:col-span-4"
          value={form.description}
          onChange={set('description')}
        />
        <TextField
          id="line-partNumber"
          label="Delenr."
          className="sm:col-span-2"
          value={form.partNumber}
          onChange={set('partNumber')}
        />
        <TextField
          id="line-quantity"
          label="Antall"
          type="number"
          className="sm:col-span-2"
          value={form.quantity}
          onChange={set('quantity')}
        />
        <TextField
          id="line-unitPrice"
          label="Enhetspris"
          type="number"
          className="sm:col-span-2"
          value={form.unitPrice}
          onChange={set('unitPrice')}
        />
        <Button type="submit" loading={adding} disabled={!canAdd} className="sm:col-span-2">
          Legg til
        </Button>
      </form>
    </Modal>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wide text-slate-400">{label}</p>
      <div className="mt-0.5 text-slate-700">{children}</div>
    </div>
  );
}

function TextBlock({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <p className="text-xs font-medium uppercase tracking-wide text-slate-400">{label}</p>
      <p className="mt-0.5 whitespace-pre-wrap text-slate-700">{children}</p>
    </div>
  );
}
