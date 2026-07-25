import { apiFetch } from './client';
import type { WorkOrderLine, WorkOrderLineInput } from '../types/workOrderLine';

function toPayload(input: WorkOrderLineInput) {
  const clean = (value: string) => (value.trim() === '' ? null : value.trim());
  return {
    description: input.description.trim(),
    partNumber: clean(input.partNumber),
    quantity: Number(input.quantity),
    unitPrice: Number(input.unitPrice),
  };
}

export const workOrderLineApi = {
  list(workOrderId: number): Promise<WorkOrderLine[]> {
    return apiFetch<WorkOrderLine[]>(`/api/v1/work-orders/${workOrderId}/lines`);
  },

  create(workOrderId: number, input: WorkOrderLineInput): Promise<WorkOrderLine> {
    return apiFetch<WorkOrderLine>(`/api/v1/work-orders/${workOrderId}/lines`, {
      method: 'POST',
      body: JSON.stringify(toPayload(input)),
    });
  },

  remove(workOrderId: number, lineId: number): Promise<void> {
    return apiFetch<void>(`/api/v1/work-orders/${workOrderId}/lines/${lineId}`, {
      method: 'DELETE',
    });
  },
};
