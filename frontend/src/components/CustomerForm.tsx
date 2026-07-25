import { useState, type FormEvent } from 'react';
import { TextField } from './TextField';
import { Button } from './Button';
import type { Customer, CustomerInput, ValidationError } from '../types/customer';

interface CustomerFormProps {
  initial?: Customer | null;
  submitting: boolean;
  serverErrors: ValidationError[];
  onSubmit: (input: CustomerInput) => void;
  onCancel: () => void;
}

const EMPTY: CustomerInput = {
  firstName: '',
  lastName: '',
  phoneNumber: '',
  email: '',
  address: '',
  postalCode: '',
  city: '',
};

function fromCustomer(customer: Customer): CustomerInput {
  return {
    firstName: customer.firstName,
    lastName: customer.lastName,
    phoneNumber: customer.phoneNumber,
    email: customer.email ?? '',
    address: customer.address ?? '',
    postalCode: customer.postalCode ?? '',
    city: customer.city ?? '',
  };
}

export function CustomerForm({
  initial,
  submitting,
  serverErrors,
  onSubmit,
  onCancel,
}: CustomerFormProps) {
  const [values, setValues] = useState<CustomerInput>(initial ? fromCustomer(initial) : EMPTY);
  const [touched, setTouched] = useState(false);

  // Client-side checks that mirror the backend's Jakarta Validation rules.
  const clientErrors: Partial<Record<keyof CustomerInput, string>> = {};
  if (!values.firstName.trim()) clientErrors.firstName = 'Fornavn er påkrevd';
  if (!values.lastName.trim()) clientErrors.lastName = 'Etternavn er påkrevd';
  if (!values.phoneNumber.trim()) clientErrors.phoneNumber = 'Telefonnummer er påkrevd';
  if (values.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email.trim())) {
    clientErrors.email = 'Oppgi en gyldig e-postadresse';
  }

  const errorFor = (field: keyof CustomerInput): string | undefined => {
    const server = serverErrors.find((e) => e.field === field);
    if (server) return server.message;
    if (touched) return clientErrors[field];
    return undefined;
  };

  const set = (field: keyof CustomerInput) => (e: { target: { value: string } }) =>
    setValues((v) => ({ ...v, [field]: e.target.value }));

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setTouched(true);
    if (Object.keys(clientErrors).length > 0) return;
    onSubmit(values);
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <TextField
          id="firstName"
          label="Fornavn"
          required
          value={values.firstName}
          onChange={set('firstName')}
          error={errorFor('firstName')}
        />
        <TextField
          id="lastName"
          label="Etternavn"
          required
          value={values.lastName}
          onChange={set('lastName')}
          error={errorFor('lastName')}
        />
        <TextField
          id="phoneNumber"
          label="Telefonnummer"
          required
          value={values.phoneNumber}
          onChange={set('phoneNumber')}
          error={errorFor('phoneNumber')}
        />
        <TextField
          id="email"
          label="E-post"
          type="email"
          value={values.email}
          onChange={set('email')}
          error={errorFor('email')}
        />
        <TextField
          id="address"
          label="Adresse"
          className="sm:col-span-2"
          value={values.address}
          onChange={set('address')}
          error={errorFor('address')}
        />
        <TextField
          id="postalCode"
          label="Postnummer"
          value={values.postalCode}
          onChange={set('postalCode')}
          error={errorFor('postalCode')}
        />
        <TextField
          id="city"
          label="By"
          value={values.city}
          onChange={set('city')}
          error={errorFor('city')}
        />
      </div>

      <div className="mt-6 flex justify-end gap-3">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={submitting}>
          Avbryt
        </Button>
        <Button type="submit" loading={submitting}>
          {initial ? 'Lagre endringer' : 'Opprett kunde'}
        </Button>
      </div>
    </form>
  );
}
