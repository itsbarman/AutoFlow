import {
  PRIORITY_LABELS,
  PRIORITY_STYLES,
  STATUS_LABELS,
  STATUS_STYLES,
  type WorkOrder,
  type WorkOrderStatus,
} from '../types/workOrder';
import { EditIcon, ListIcon, TrashIcon, WrenchIcon } from './icons';

interface WorkOrderTableProps {
  workOrders: WorkOrder[];
  onView: (workOrder: WorkOrder) => void;
  onEdit: (workOrder: WorkOrder) => void;
  onDelete: (workOrder: WorkOrder) => void;
  onStatusChange: (workOrder: WorkOrder, status: WorkOrderStatus) => void;
}

const ALL_STATUSES = Object.keys(STATUS_LABELS) as WorkOrderStatus[];

export function WorkOrderTable({
  workOrders,
  onView,
  onEdit,
  onDelete,
  onStatusChange,
}: WorkOrderTableProps) {
  return (
    <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
      <table className="w-full text-left text-sm">
        <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
          <tr>
            <th className="px-4 py-3 font-medium sm:px-6">Arbeidsordre</th>
            <th className="hidden px-4 py-3 font-medium lg:table-cell">Kjøretøy</th>
            <th className="hidden px-4 py-3 font-medium md:table-cell">Prioritet</th>
            <th className="px-4 py-3 font-medium">Status</th>
            <th className="px-4 py-3 text-right font-medium sm:px-6">Handlinger</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {workOrders.map((workOrder) => (
            <tr key={workOrder.id} className="transition-colors hover:bg-slate-50">
              <td className="px-4 py-3 sm:px-6">
                <div className="flex items-center gap-3">
                  <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand-100 text-brand-700">
                    <WrenchIcon width={18} height={18} />
                  </span>
                  <div className="leading-tight">
                    <p className="font-medium text-slate-800">{workOrder.title}</p>
                    <p className="font-mono text-xs text-slate-400">{workOrder.workOrderNumber}</p>
                  </div>
                </div>
              </td>
              <td className="hidden px-4 py-3 lg:table-cell">
                <p className="text-slate-700">{workOrder.vehicleRegistrationNumber}</p>
                <p className="text-xs text-slate-400">{workOrder.customerName}</p>
              </td>
              <td className="hidden px-4 py-3 md:table-cell">
                <span className={PRIORITY_STYLES[workOrder.priority]}>
                  {PRIORITY_LABELS[workOrder.priority]}
                </span>
              </td>
              <td className="px-4 py-3">
                <div className="flex items-center gap-2">
                  <span
                    className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLES[workOrder.status]}`}
                  >
                    {STATUS_LABELS[workOrder.status]}
                  </span>
                  <select
                    aria-label={`Endre status for ${workOrder.workOrderNumber}`}
                    value={workOrder.status}
                    onChange={(e) => onStatusChange(workOrder, e.target.value as WorkOrderStatus)}
                    className="rounded-md border border-slate-200 bg-white px-1.5 py-1 text-xs text-slate-500
                      focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-200"
                  >
                    {ALL_STATUSES.map((status) => (
                      <option key={status} value={status}>
                        {STATUS_LABELS[status]}
                      </option>
                    ))}
                  </select>
                </div>
              </td>
              <td className="px-4 py-3 sm:px-6">
                <div className="flex justify-end gap-1">
                  <button
                    onClick={() => onView(workOrder)}
                    aria-label={`Detaljer ${workOrder.workOrderNumber}`}
                    className="rounded-lg p-2 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-700"
                  >
                    <ListIcon width={18} height={18} />
                  </button>
                  <button
                    onClick={() => onEdit(workOrder)}
                    aria-label={`Rediger ${workOrder.workOrderNumber}`}
                    className="rounded-lg p-2 text-slate-400 transition-colors hover:bg-brand-50 hover:text-brand-600"
                  >
                    <EditIcon width={18} height={18} />
                  </button>
                  <button
                    onClick={() => onDelete(workOrder)}
                    aria-label={`Slett ${workOrder.workOrderNumber}`}
                    className="rounded-lg p-2 text-slate-400 transition-colors hover:bg-red-50 hover:text-red-600"
                  >
                    <TrashIcon width={18} height={18} />
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {workOrders.length === 0 && (
        <div className="flex flex-col items-center gap-2 px-6 py-16 text-center">
          <span className="flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-slate-400">
            <WrenchIcon />
          </span>
          <p className="font-medium text-slate-600">Ingen arbeidsordre funnet</p>
          <p className="text-sm text-slate-400">Opprett en arbeidsordre på et kjøretøy for å se den her.</p>
        </div>
      )}
    </div>
  );
}
