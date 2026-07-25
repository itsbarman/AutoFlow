import { useState, type FormEvent } from 'react';
import { TextField } from './TextField';
import { Select } from './Select';
import { Button } from './Button';
import type { Customer } from '../types/customer';
import { FUEL_TYPE_LABELS, type FuelType, type Vehicle, type VehicleInput } from '../types/vehicle';
import type { ValidationError } from '../types/customer';
import { vehicleApi } from '../api/vehicles';
import { ApiRequestError } from '../api/client';

interface VehicleFormProps {
  initial?: Vehicle | null;
  customers: Customer[];
  submitting: boolean;
  serverErrors: ValidationError[];
  /** customerId is only needed when creating (editing keeps the existing owner). */
  onSubmit: (input: VehicleInput, customerId: number) => void;
  onCancel: () => void;
}

const currentYear = new Date().getFullYear();

function emptyInput(): VehicleInput {
  return {
    registrationNumber: '',
    vin: '',
    make: '',
    model: '',
    modelYear: '',
    mileage: '',
    fuelType: 'PETROL',
  };
}

function fromVehicle(vehicle: Vehicle): VehicleInput {
  return {
    registrationNumber: vehicle.registrationNumber,
    vin: vehicle.vin ?? '',
    make: vehicle.make,
    model: vehicle.model,
    modelYear: vehicle.modelYear?.toString() ?? '',
    mileage: vehicle.mileage?.toString() ?? '',
    fuelType: vehicle.fuelType,
  };
}

export function VehicleForm({
  initial,
  customers,
  submitting,
  serverErrors,
  onSubmit,
  onCancel,
}: VehicleFormProps) {
  const [values, setValues] = useState<VehicleInput>(initial ? fromVehicle(initial) : emptyInput());
  const [customerId, setCustomerId] = useState<string>(initial ? String(initial.customerId) : '');
  const [touched, setTouched] = useState(false);
  const [lookupLoading, setLookupLoading] = useState(false);
  const [lookupError, setLookupError] = useState<string | null>(null);
  const [lookupSuccess, setLookupSuccess] = useState<string | null>(null);

  const clientErrors: Partial<Record<string, string>> = {};
  if (!values.registrationNumber.trim()) clientErrors.registrationNumber = 'Registreringsnummer er påkrevd';
  if (!values.make.trim()) clientErrors.make = 'Merke er påkrevd';
  if (!values.model.trim()) clientErrors.model = 'Modell er påkrevd';
  if (!initial && !customerId) clientErrors.customerId = 'Velg en kunde';
  if (values.modelYear && (Number(values.modelYear) < 1900 || Number(values.modelYear) > currentYear + 1)) {
    clientErrors.modelYear = `Oppgi et år mellom 1900 og ${currentYear + 1}`;
  }
  if (values.mileage && Number(values.mileage) < 0) clientErrors.mileage = 'Kilometerstand kan ikke være negativ';

  const errorFor = (field: string): string | undefined => {
    const server = serverErrors.find((e) => e.field === field);
    if (server) return server.message;
    if (touched) return clientErrors[field];
    return undefined;
  };

  const set = (field: keyof VehicleInput) => (e: { target: { value: string } }) =>
    setValues((v) => ({ ...v, [field]: e.target.value }));

  async function handleLookup() {
    const regnr = values.registrationNumber.trim();
    if (!regnr) {
      setLookupError('Fyll inn registreringsnummeret først.');
      return;
    }
    setLookupLoading(true);
    setLookupError(null);
    setLookupSuccess(null);
    try {
      const data = await vehicleApi.lookup(regnr);
      const filled: string[] = [];
      setValues((prev) => {
        const next = { ...prev };
        if (data.make) { next.make = data.make; filled.push('merke'); }
        if (data.model) { next.model = data.model; filled.push('modell'); }
        if (data.modelYear) { next.modelYear = String(data.modelYear); filled.push('årsmodell'); }
        if (data.fuelType) { next.fuelType = data.fuelType; filled.push('drivstoff'); }
        if (data.vin) { next.vin = data.vin; filled.push('VIN'); }
        if (data.registrationNumber) { next.registrationNumber = data.registrationNumber; }
        return next;
      });
      const summary = filled.length > 0
        ? `Hentet: ${filled.join(', ')}.`
        : 'Kjøretøyet ble funnet, men ingen nye felt ble fylt ut.';
      if (data.color) {
        setLookupSuccess(`${summary} Farge: ${data.color}.`);
      } else {
        setLookupSuccess(summary);
      }
    } catch (err) {
      const msg = err instanceof ApiRequestError ? err.message : 'Kunne ikke hente data fra Vegvesen.';
      setLookupError(msg);
    } finally {
      setLookupLoading(false);
    }
  }

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setTouched(true);
    if (Object.keys(clientErrors).length > 0) return;
    onSubmit(values, Number(customerId));
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Select
          id="customerId"
          label="Kunde"
          required
          className="sm:col-span-2"
          value={customerId}
          onChange={(e) => setCustomerId(e.target.value)}
          error={errorFor('customerId')}
          disabled={Boolean(initial)}
        >
          <option value="">Velg en kunde...</option>
          {customers.map((c) => (
            <option key={c.id} value={c.id}>
              {c.firstName} {c.lastName}
            </option>
          ))}
        </Select>

        <TextField
          id="registrationNumber"
          label="Registreringsnummer"
          required
          value={values.registrationNumber}
          onChange={(e) => {
            set('registrationNumber')(e);
            setLookupError(null);
            setLookupSuccess(null);
          }}
          error={errorFor('registrationNumber')}
        />

        {/* Vegvesen-oppslag */}
        <div className="flex flex-col justify-end gap-2 sm:col-span-1">
          <Button
            type="button"
            variant="secondary"
            loading={lookupLoading}
            onClick={handleLookup}
            disabled={!values.registrationNumber.trim() || lookupLoading}
            className="w-full"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width={16}
              height={16}
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth={2}
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.3-4.3" />
            </svg>
            Hent data fra regnr
          </Button>
        </div>

        {/* Tilbakemelding fra oppslaget */}
        {lookupError && (
          <div className="sm:col-span-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {lookupError}
          </div>
        )}
        {lookupSuccess && (
          <div className="sm:col-span-2 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
            ✓ {lookupSuccess}
          </div>
        )}

        <TextField
          id="vin"
          label="VIN"
          value={values.vin}
          onChange={set('vin')}
          error={errorFor('vin')}
        />
        <TextField
          id="make"
          label="Merke"
          required
          value={values.make}
          onChange={set('make')}
          error={errorFor('make')}
        />
        <TextField
          id="model"
          label="Modell"
          required
          value={values.model}
          onChange={set('model')}
          error={errorFor('model')}
        />
        <TextField
          id="modelYear"
          label="Årsmodell"
          type="number"
          value={values.modelYear}
          onChange={set('modelYear')}
          error={errorFor('modelYear')}
        />
        <TextField
          id="mileage"
          label="Kilometerstand (km)"
          type="number"
          value={values.mileage}
          onChange={set('mileage')}
          error={errorFor('mileage')}
        />
        <Select
          id="fuelType"
          label="Drivstoff"
          required
          className="sm:col-span-2"
          value={values.fuelType}
          onChange={(e) => setValues((v) => ({ ...v, fuelType: e.target.value as FuelType }))}
          error={errorFor('fuelType')}
        >
          {(Object.keys(FUEL_TYPE_LABELS) as FuelType[]).map((key) => (
            <option key={key} value={key}>
              {FUEL_TYPE_LABELS[key]}
            </option>
          ))}
        </Select>
      </div>

      <div className="mt-6 flex justify-end gap-3">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={submitting}>
          Avbryt
        </Button>
        <Button type="submit" loading={submitting}>
          {initial ? 'Lagre endringer' : 'Registrer kjøretøy'}
        </Button>
      </div>
    </form>
  );
}
