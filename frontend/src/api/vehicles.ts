import { apiFetch } from './client';
import type { Vehicle, VehicleInput } from '../types/vehicle';

const VEHICLES_PATH = '/api/v1/vehicles';

/** Convert the string-based form fields into the API's expected JSON payload. */
function toPayload(input: VehicleInput) {
  const clean = (value: string) => (value.trim() === '' ? null : value.trim());
  const toNumber = (value: string) => (value.trim() === '' ? null : Number(value));
  return {
    registrationNumber: input.registrationNumber.trim(),
    vin: clean(input.vin),
    make: input.make.trim(),
    model: input.model.trim(),
    modelYear: toNumber(input.modelYear),
    mileage: toNumber(input.mileage),
    fuelType: input.fuelType,
  };
}

export const vehicleApi = {
  list(): Promise<Vehicle[]> {
    return apiFetch<Vehicle[]>(VEHICLES_PATH);
  },

  create(customerId: number, input: VehicleInput): Promise<Vehicle> {
    return apiFetch<Vehicle>(`/api/v1/customers/${customerId}/vehicles`, {
      method: 'POST',
      body: JSON.stringify(toPayload(input)),
    });
  },

  update(id: number, input: VehicleInput): Promise<Vehicle> {
    return apiFetch<Vehicle>(`${VEHICLES_PATH}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(toPayload(input)),
    });
  },

  remove(id: number): Promise<void> {
    return apiFetch<void>(`${VEHICLES_PATH}/${id}`, { method: 'DELETE' });
  },
};
