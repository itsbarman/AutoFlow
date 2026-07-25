import { apiFetch } from './client';
import type { AuthUser, LoginResponse } from '../types/auth';

export const authApi = {
  login(username: string, password: string): Promise<LoginResponse> {
    return apiFetch<LoginResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
  },

  me(): Promise<AuthUser> {
    return apiFetch<AuthUser>('/api/v1/auth/me');
  },
};
