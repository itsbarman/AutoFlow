const STORAGE_KEY = 'autoflow.token';

/** Small wrapper around localStorage for the JWT. */
export const tokenStore = {
  get: (): string | null => localStorage.getItem(STORAGE_KEY),
  set: (token: string) => localStorage.setItem(STORAGE_KEY, token),
  clear: () => localStorage.removeItem(STORAGE_KEY),
};

/** Fired by the API client when the server rejects the token (401). */
export const UNAUTHORIZED_EVENT = 'auth:unauthorized';
