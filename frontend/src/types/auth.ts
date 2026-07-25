export interface AuthUser {
  id: number;
  username: string;
  fullName: string;
  roles: string[];
}

export interface LoginResponse {
  token: string;
  expiresInMs: number;
  username: string;
  fullName: string;
  roles: string[];
}
