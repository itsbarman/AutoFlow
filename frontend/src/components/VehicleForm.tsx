import { useState, type FormEvent } from 'react';
import { TextField } from './TextField';
import { Select } from './Select';
import { Button } from './Button';
import type { Customer } from '../types/customer';
import { FUEL_TYPE_LABELS, type FuelType, type Vehicle, type VehicleInput } from '../types/vehicle';
import type { ValidationError } from '../types/customer';

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

  const clientErrors: Partial<Record<string, string>> = {};
  if (!values.registrationNumber.trim()) clientErrors.registrationNumber = 'Registration number is required';
  if (!values.make.trim()) clientErrors.make = 'Make is required';
  if (!values.model.trim()) clientErrors.model = 'Model is required';
  if (!initial && !customerId) clientErrors.customerId = 'Please choose a customer';
  if (values.modelYear && (Number(values.modelYear) < 1900 || Number(values.modelYear) > currentYear + 1)) {
    clientErrors.modelYear = `Enter a year between 1900 and ${currentYear + 1}`;
  }
  if (values.mileage && Number(values.mileage) < 0) clientErrors.mileage = 'Mileage cannot be negative';

  const errorFor = (field: string): string | undefined => {
    const server = serverErrors.find((e) => e.field === field);
    if (server) return server.message;
    if (touched) return clientErrors[field];
    return undefined;
  };

  const set = (field: keyof VehicleInput) => (e: { target: { value: string } }) =>
    setValues((v) => ({ ...v, [field]: e.target.value }));

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
          label="Customer"
          required
          className="sm:col-span-2"
          value={customerId}
          onChange={(e) => setCustomerId(e.target.value)}
          error={errorFor('customerId')}
          disabled={Boolean(initial)}
        >
          <option value="">Select a customer...</option>
          {customers.map((c) => (
            <option key={c.id} value={c.id}>
              {c.firstName} {c.lastName}
            </option>
          ))}
        </Select>

        <TextField
          id="registrationNumber"
          label="Registration number"
          required
          value={values.registrationNumber}
          onChange={set('registrationNumber')}
          error={errorFor('registrationNumber')}
        />
        <TextField
          id="vin"
          label="VIN"
          value={values.vin}
          onChange={set('vin')}
          error={errorFor('vin')}
        />
        <TextField
          id="make"
          label="Make"
          required
          value={values.make}
          onChange={set('make')}
          error={errorFor('make')}
        />
        <TextField
          id="model"
          label="Model"
          required
          value={values.model}
          onChange={set('model')}
          error={errorFor('model')}
        />
        <TextField
          id="modelYear"
          label="Model year"
          type="number"
          value={values.modelYear}
          onChange={set('modelYear')}
          error={errorFor('modelYear')}
        />
        <TextField
          id="mileage"
          label="Mileage (km)"
          type="number"
          value={values.mileage}
          onChange={set('mileage')}
          error={errorFor('mileage')}
        />
        <Select
          id="fuelType"
          label="Fuel type"
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
          Cancel
        </Button>
        <Button type="submit" loading={submitting}>
          {initial ? 'Save changes' : 'Register vehicle'}
        </Button>
      </div>
    </form>
  );
}
