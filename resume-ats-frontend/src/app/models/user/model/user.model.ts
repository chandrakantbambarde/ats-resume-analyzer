export interface User {
  id?: number;
  email: string;
  password?: string;
  name: string;
  createdAt?: Date;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  email: string;
  name: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
}
