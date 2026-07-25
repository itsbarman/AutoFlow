import { apiFetch } from './client';
import type { Customer, CustomerInput } from '../types/customer';

const CUSTOMERS_PATH = '/api/v1/customers';

/**
 * The API rejects empty optional fields differently than empty strings, so we
 * convert blank optional values to null before sending them.
 */
function toPayload(input: CustomerInput) {
  const clean = (value: string) => (value.trim() === '' ? null : value.trim());
  return {
    firstName: input.firstName.trim(),
    lastName: input.lastName.trim(),
    phoneNumber: input.phoneNumber.trim(),
    email: clean(input.email),
    address: clean(input.address),
    postalCode: clean(input.postalCode),
    city: clean(input.city),
  };
}

export const customerApi = {
  list(): Promise<Customer[]> {
    return apiFetch<Customer[]>(CUSTOMERS_PATH);
  },

  getById(id: number): Promise<Customer> {
    return apiFetch<Customer>(`${CUSTOMERS_PATH}/${id}`);
  },

  create(input: CustomerInput): Promise<Customer> {
    return apiFetch<Customer>(CUSTOMERS_PATH, {
      method: 'POST',
      body: JSON.stringify(toPayload(input)),
    });
  },

  update(id: number, input: CustomerInput): Promise<Customer> {
    return apiFetch<Customer>(`${CUSTOMERS_PATH}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(toPayload(input)),
    });
  },

  remove(id: number): Promise<void> {
    return apiFetch<void>(`${CUSTOMERS_PATH}/${id}`, { method: 'DELETE' });
  },
};
