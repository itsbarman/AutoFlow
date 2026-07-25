export type WorkOrderStatus =
  | 'CREATED'
  | 'BOOKED'
  | 'IN_PROGRESS'
  | 'WAITING_FOR_PARTS'
  | 'COMPLETED'
  | 'CANCELLED';

export type WorkOrderPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';

export const STATUS_LABELS: Record<WorkOrderStatus, string> = {
  CREATED: 'Created',
  BOOKED: 'Booked',
  IN_PROGRESS: 'In progress',
  WAITING_FOR_PARTS: 'Waiting for parts',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
};

/** Tailwind classes for each status badge. */
export const STATUS_STYLES: Record<WorkOrderStatus, string> = {
  CREATED: 'bg-slate-100 text-slate-600',
  BOOKED: 'bg-blue-100 text-blue-700',
  IN_PROGRESS: 'bg-amber-100 text-amber-700',
  WAITING_FOR_PARTS: 'bg-purple-100 text-purple-700',
  COMPLETED: 'bg-emerald-100 text-emerald-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

export const PRIORITY_LABELS: Record<WorkOrderPriority, string> = {
  LOW: 'Low',
  NORMAL: 'Normal',
  HIGH: 'High',
  URGENT: 'Urgent',
};

export const PRIORITY_STYLES: Record<WorkOrderPriority, string> = {
  LOW: 'text-slate-500',
  NORMAL: 'text-slate-600',
  HIGH: 'text-orange-600',
  URGENT: 'text-red-600 font-semibold',
};

export interface WorkOrder {
  id: number;
  workOrderNumber: string;
  title: string;
  description: string | null;
  status: WorkOrderStatus;
  priority: WorkOrderPriority;
  mileageAtArrival: number | null;
  customerComplaint: string | null;
  technicianNotes: string | null;
  estimatedCompletionDate: string | null;
  customerId: number;
  customerName: string;
  vehicleId: number;
  vehicleRegistrationNumber: string;
  vehicleDescription: string;
  createdAt: string;
  updatedAt: string;
  completedAt: string | null;
}

/** Form shape for creating/updating a work order (string-based fields). */
export interface WorkOrderInput {
  title: string;
  description: string;
  priority: WorkOrderPriority;
  mileageAtArrival: string;
  customerComplaint: string;
  technicianNotes: string;
  estimatedCompletionDate: string;
}
