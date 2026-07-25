import { useState, type FormEvent } from 'react';
import { TextField } from './TextField';
import { Select } from './Select';
import { Button } from './Button';
import type { Vehicle } from '../types/vehicle';
import type { ValidationError } from '../types/customer';
import {
  PRIORITY_LABELS,
  type WorkOrder,
  type WorkOrderInput,
  type WorkOrderPriority,
} from '../types/workOrder';

interface WorkOrderFormProps {
  initial?: WorkOrder | null;
  vehicles: Vehicle[];
  submitting: boolean;
  serverErrors: ValidationError[];
  onSubmit: (input: WorkOrderInput, vehicleId: number) => void;
  onCancel: () => void;
}

function emptyInput(): WorkOrderInput {
  return {
    title: '',
    description: '',
    priority: 'NORMAL',
    mileageAtArrival: '',
    customerComplaint: '',
    technicianNotes: '',
    estimatedCompletionDate: '',
  };
}

function fromWorkOrder(workOrder: WorkOrder): WorkOrderInput {
  return {
    title: workOrder.title,
    description: workOrder.description ?? '',
    priority: workOrder.priority,
    mileageAtArrival: workOrder.mileageAtArrival?.toString() ?? '',
    customerComplaint: workOrder.customerComplaint ?? '',
    technicianNotes: workOrder.technicianNotes ?? '',
    estimatedCompletionDate: workOrder.estimatedCompletionDate ?? '',
  };
}

export function WorkOrderForm({
  initial,
  vehicles,
  submitting,
  serverErrors,
  onSubmit,
  onCancel,
}: WorkOrderFormProps) {
  const [values, setValues] = useState<WorkOrderInput>(
    initial ? fromWorkOrder(initial) : emptyInput(),
  );
  const [vehicleId, setVehicleId] = useState<string>(initial ? String(initial.vehicleId) : '');
  const [touched, setTouched] = useState(false);

  const clientErrors: Partial<Record<string, string>> = {};
  if (!values.title.trim()) clientErrors.title = 'Tittel er påkrevd';
  if (!initial && !vehicleId) clientErrors.vehicleId = 'Velg et kjøretøy';
  if (values.mileageAtArrival && Number(values.mileageAtArrival) < 0) {
    clientErrors.mileageAtArrival = 'Kilometerstand kan ikke være negativ';
  }

  const errorFor = (field: string): string | undefined => {
    const server = serverErrors.find((e) => e.field === field);
    if (server) return server.message;
    if (touched) return clientErrors[field];
    return undefined;
  };

  const set = (field: keyof WorkOrderInput) => (e: { target: { value: string } }) =>
    setValues((v) => ({ ...v, [field]: e.target.value }));

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setTouched(true);
    if (Object.keys(clientErrors).length > 0) return;
    onSubmit(values, Number(vehicleId));
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Select
          id="vehicleId"
          label="Kjøretøy"
          required
          className="sm:col-span-2"
          value={vehicleId}
          onChange={(e) => setVehicleId(e.target.value)}
          error={errorFor('vehicleId')}
          disabled={Boolean(initial)}
        >
          <option value="">Velg et kjøretøy...</option>
          {vehicles.map((v) => (
            <option key={v.id} value={v.id}>
              {v.registrationNumber} — {v.make} {v.model} ({v.customerName})
            </option>
          ))}
        </Select>

        <TextField
          id="title"
          label="Tittel"
          required
          className="sm:col-span-2"
          value={values.title}
          onChange={set('title')}
          error={errorFor('title')}
        />

        <Select
          id="priority"
          label="Prioritet"
          value={values.priority}
          onChange={(e) => setValues((v) => ({ ...v, priority: e.target.value as WorkOrderPriority }))}
          error={errorFor('priority')}
        >
          {(Object.keys(PRIORITY_LABELS) as WorkOrderPriority[]).map((key) => (
            <option key={key} value={key}>
              {PRIORITY_LABELS[key]}
            </option>
          ))}
        </Select>
        <TextField
          id="mileageAtArrival"
          label="Kilometerstand ved ankomst (km)"
          type="number"
          value={values.mileageAtArrival}
          onChange={set('mileageAtArrival')}
          error={errorFor('mileageAtArrival')}
        />

        <TextField
          id="estimatedCompletionDate"
          label="Estimert ferdigdato"
          type="date"
          className="sm:col-span-2"
          value={values.estimatedCompletionDate}
          onChange={set('estimatedCompletionDate')}
          error={errorFor('estimatedCompletionDate')}
        />

        <TextField
          id="customerComplaint"
          label="Kundens beskrivelse"
          className="sm:col-span-2"
          value={values.customerComplaint}
          onChange={set('customerComplaint')}
          error={errorFor('customerComplaint')}
        />
        <TextField
          id="description"
          label="Beskrivelse"
          className="sm:col-span-2"
          value={values.description}
          onChange={set('description')}
          error={errorFor('description')}
        />
        <TextField
          id="technicianNotes"
          label="Tekniker-notater"
          className="sm:col-span-2"
          value={values.technicianNotes}
          onChange={set('technicianNotes')}
          error={errorFor('technicianNotes')}
        />
      </div>

      <div className="mt-6 flex justify-end gap-3">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={submitting}>
          Avbryt
        </Button>
        <Button type="submit" loading={submitting}>
          {initial ? 'Lagre endringer' : 'Opprett arbeidsordre'}
        </Button>
      </div>
    </form>
  );
}
