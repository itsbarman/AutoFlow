import { FUEL_TYPE_LABELS, type Vehicle } from '../types/vehicle';
import { CarIcon, EditIcon, TrashIcon } from './icons';

interface VehicleTableProps {
  vehicles: Vehicle[];
  onEdit: (vehicle: Vehicle) => void;
  onDelete: (vehicle: Vehicle) => void;
}

function formatMileage(mileage: number | null): string {
  if (mileage == null) return '\u2014';
  return `${mileage.toLocaleString()} km`;
}

export function VehicleTable({ vehicles, onEdit, onDelete }: VehicleTableProps) {
  return (
    <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
      <table className="w-full text-left text-sm">
        <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
          <tr>
            <th className="px-4 py-3 font-medium sm:px-6">Kjøretøy</th>
            <th className="px-4 py-3 font-medium">Reg.nr</th>
            <th className="hidden px-4 py-3 font-medium md:table-cell">År</th>
            <th className="hidden px-4 py-3 font-medium lg:table-cell">Kilometerstand</th>
            <th className="hidden px-4 py-3 font-medium sm:table-cell">Drivstoff</th>
            <th className="px-4 py-3 font-medium">Kunde</th>
            <th className="px-4 py-3 text-right font-medium sm:px-6">Handlinger</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {vehicles.map((vehicle) => (
            <tr key={vehicle.id} className="transition-colors hover:bg-slate-50">
              <td className="px-4 py-3 sm:px-6">
                <div className="flex items-center gap-3">
                  <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand-100 text-brand-700">
                    <CarIcon width={18} height={18} />
                  </span>
                  <div className="leading-tight">
                    <p className="font-medium text-slate-800">
                      {vehicle.make} {vehicle.model}
                    </p>
                    <p className="text-xs text-slate-400">
                      {vehicle.vin ? `VIN ${vehicle.vin}` : 'Ingen VIN'}
                    </p>
                  </div>
                </div>
              </td>
              <td className="px-4 py-3">
                <span className="inline-block rounded border border-slate-300 bg-slate-50 px-2 py-0.5 font-mono text-xs font-semibold tracking-wide text-slate-700">
                  {vehicle.registrationNumber}
                </span>
              </td>
              <td className="hidden px-4 py-3 text-slate-600 md:table-cell">
                {vehicle.modelYear ?? <span className="text-slate-300">&mdash;</span>}
              </td>
              <td className="hidden px-4 py-3 text-slate-600 lg:table-cell">
                {formatMileage(vehicle.mileage)}
              </td>
              <td className="hidden px-4 py-3 sm:table-cell">
                <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
                  {FUEL_TYPE_LABELS[vehicle.fuelType]}
                </span>
              </td>
              <td className="px-4 py-3 text-slate-600">{vehicle.customerName}</td>
              <td className="px-4 py-3 sm:px-6">
                <div className="flex justify-end gap-1">
                  <button
                    onClick={() => onEdit(vehicle)}
                    aria-label={`Rediger ${vehicle.registrationNumber}`}
                    className="rounded-lg p-2 text-slate-400 transition-colors hover:bg-brand-50 hover:text-brand-600"
                  >
                    <EditIcon width={18} height={18} />
                  </button>
                  <button
                    onClick={() => onDelete(vehicle)}
                    aria-label={`Slett ${vehicle.registrationNumber}`}
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

      {vehicles.length === 0 && (
        <div className="flex flex-col items-center gap-2 px-6 py-16 text-center">
          <span className="flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-slate-400">
            <CarIcon />
          </span>
          <p className="font-medium text-slate-600">Ingen kjøretøy funnet</p>
          <p className="text-sm text-slate-400">
            Registrer et kjøretøy på en kunde for å se det her.
          </p>
        </div>
      )}
    </div>
  );
}
