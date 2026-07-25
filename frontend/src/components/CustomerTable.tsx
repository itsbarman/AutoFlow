import type { Customer } from '../types/customer';
import { EditIcon, TrashIcon, UsersIcon } from './icons';

interface CustomerTableProps {
  customers: Customer[];
  onEdit: (customer: Customer) => void;
  onDelete: (customer: Customer) => void;
}

function initials(customer: Customer): string {
  return `${customer.firstName[0] ?? ''}${customer.lastName[0] ?? ''}`.toUpperCase();
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

export function CustomerTable({ customers, onEdit, onDelete }: CustomerTableProps) {
  return (
    <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
      <table className="w-full text-left text-sm">
        <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
          <tr>
            <th className="px-4 py-3 font-medium sm:px-6">Customer</th>
            <th className="px-4 py-3 font-medium">Phone</th>
            <th className="hidden px-4 py-3 font-medium md:table-cell">Email</th>
            <th className="hidden px-4 py-3 font-medium lg:table-cell">City</th>
            <th className="hidden px-4 py-3 font-medium lg:table-cell">Created</th>
            <th className="px-4 py-3 text-right font-medium sm:px-6">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {customers.map((customer) => (
            <tr key={customer.id} className="transition-colors hover:bg-slate-50">
              <td className="px-4 py-3 sm:px-6">
                <div className="flex items-center gap-3">
                  <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand-100 text-xs font-semibold text-brand-700">
                    {initials(customer)}
                  </span>
                  <div className="leading-tight">
                    <p className="font-medium text-slate-800">
                      {customer.firstName} {customer.lastName}
                    </p>
                    <p className="text-xs text-slate-400 md:hidden">
                      {customer.email ?? 'No email'}
                    </p>
                  </div>
                </div>
              </td>
              <td className="px-4 py-3 text-slate-600">{customer.phoneNumber}</td>
              <td className="hidden px-4 py-3 text-slate-600 md:table-cell">
                {customer.email ?? <span className="text-slate-300">&mdash;</span>}
              </td>
              <td className="hidden px-4 py-3 text-slate-600 lg:table-cell">
                {customer.city ?? <span className="text-slate-300">&mdash;</span>}
              </td>
              <td className="hidden px-4 py-3 text-slate-500 lg:table-cell">
                {formatDate(customer.createdAt)}
              </td>
              <td className="px-4 py-3 sm:px-6">
                <div className="flex justify-end gap-1">
                  <button
                    onClick={() => onEdit(customer)}
                    aria-label={`Edit ${customer.firstName}`}
                    className="rounded-lg p-2 text-slate-400 transition-colors hover:bg-brand-50 hover:text-brand-600"
                  >
                    <EditIcon width={18} height={18} />
                  </button>
                  <button
                    onClick={() => onDelete(customer)}
                    aria-label={`Delete ${customer.firstName}`}
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

      {customers.length === 0 && (
        <div className="flex flex-col items-center gap-2 px-6 py-16 text-center">
          <span className="flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-slate-400">
            <UsersIcon />
          </span>
          <p className="font-medium text-slate-600">No customers found</p>
          <p className="text-sm text-slate-400">Try adjusting your search or add a new customer.</p>
        </div>
      )}
    </div>
  );
}
