export type FuelType =
  | 'PETROL'
  | 'DIESEL'
  | 'ELECTRIC'
  | 'HYBRID'
  | 'PLUG_IN_HYBRID'
  | 'OTHER';

/** Human-readable labels for the fuel type enum. */
export const FUEL_TYPE_LABELS: Record<FuelType, string> = {
  PETROL: 'Bensin',
  DIESEL: 'Diesel',
  ELECTRIC: 'Elektrisk',
  HYBRID: 'Hybrid',
  PLUG_IN_HYBRID: 'Ladbar hybrid',
  OTHER: 'Annet',
};

/** Shape of a vehicle as returned by the API. */
export interface Vehicle {
  id: number;
  customerId: number;
  customerName: string;
  registrationNumber: string;
  vin: string | null;
  make: string;
  model: string;
  modelYear: number | null;
  mileage: number | null;
  fuelType: FuelType;
  createdAt: string;
  updatedAt: string;
}

/** Form/payload shape for creating or updating a vehicle. */
export interface VehicleInput {
  registrationNumber: string;
  vin: string;
  make: string;
  model: string;
  modelYear: string;
  mileage: string;
  fuelType: FuelType;
}
