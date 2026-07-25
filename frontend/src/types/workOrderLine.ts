export interface WorkOrderLine {
  id: number;
  workOrderId: number;
  description: string;
  partNumber: string | null;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface WorkOrderLineInput {
  description: string;
  partNumber: string;
  quantity: string;
  unitPrice: string;
}
