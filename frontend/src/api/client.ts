import type { ApiError, ValidationError } from '../types/customer';
import { tokenStore, UNAUTHORIZED_EVENT } from '../auth/token';

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

/**
 * Error thrown for any non-2xx API response. Carries the parsed backend
 * message and any field validation errors so the UI can react to them.
 */
export class ApiRequestError extends Error {
  readonly status: number;
  readonly validationErrors: ValidationError[];

  constructor(message: string, status: number, validationErrors: ValidationError[] = []) {
    super(message);
    this.name = 'ApiRequestError';
    this.status = status;
    this.validationErrors = validationErrors;
  }
}

async function parseError(response: Response): Promise<ApiRequestError> {
  try {
    const body = (await response.json()) as ApiError;
    return new ApiRequestError(
      body.message || response.statusText,
      response.status,
      body.validationErrors ?? [],
    );
  } catch {
    return new ApiRequestError(response.statusText || 'Request failed', response.status);
  }
}

/** Thin fetch wrapper that adds JSON headers, the auth token and consistent error handling. */
export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = tokenStore.get();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  // A rejected/expired token: drop it and let the app fall back to the login screen.
  if (response.status === 401) {
    tokenStore.clear();
    window.dispatchEvent(new Event(UNAUTHORIZED_EVENT));
  }

  if (!response.ok) {
    throw await parseError(response);
  }

  // 204 No Content (e.g. DELETE) has no body to parse.
  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
