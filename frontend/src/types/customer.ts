/** Shape of a customer as returned by the API. */
export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  email: string | null;
  address: string | null;
  postalCode: string | null;
  city: string | null;
  createdAt: string;
  updatedAt: string;
}

/** Payload for creating or updating a customer. */
export interface CustomerInput {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  email: string;
  address: string;
  postalCode: string;
  city: string;
}

/** A single field validation error returned by the backend. */
export interface ValidationError {
  field: string;
  message: string;
}

/** The consistent error body returned by the backend's GlobalExceptionHandler. */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors: ValidationError[];
}
