import { apiFetch } from './client';
import type { WorkOrder, WorkOrderInput, WorkOrderStatus } from '../types/workOrder';

const WORK_ORDERS_PATH = '/api/v1/work-orders';

function toPayload(input: WorkOrderInput) {
  const clean = (value: string) => (value.trim() === '' ? null : value.trim());
  const toNumber = (value: string) => (value.trim() === '' ? null : Number(value));
  return {
    title: input.title.trim(),
    description: clean(input.description),
    priority: input.priority,
    mileageAtArrival: toNumber(input.mileageAtArrival),
    customerComplaint: clean(input.customerComplaint),
    technicianNotes: clean(input.technicianNotes),
    estimatedCompletionDate: clean(input.estimatedCompletionDate),
  };
}

export const workOrderApi = {
  list(): Promise<WorkOrder[]> {
    return apiFetch<WorkOrder[]>(WORK_ORDERS_PATH);
  },

  create(vehicleId: number, input: WorkOrderInput): Promise<WorkOrder> {
    return apiFetch<WorkOrder>(`/api/v1/vehicles/${vehicleId}/work-orders`, {
      method: 'POST',
      body: JSON.stringify(toPayload(input)),
    });
  },

  update(id: number, input: WorkOrderInput): Promise<WorkOrder> {
    return apiFetch<WorkOrder>(`${WORK_ORDERS_PATH}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(toPayload(input)),
    });
  },

  updateStatus(id: number, status: WorkOrderStatus): Promise<WorkOrder> {
    return apiFetch<WorkOrder>(`${WORK_ORDERS_PATH}/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    });
  },

  remove(id: number): Promise<void> {
    return apiFetch<void>(`${WORK_ORDERS_PATH}/${id}`, { method: 'DELETE' });
  },
};
