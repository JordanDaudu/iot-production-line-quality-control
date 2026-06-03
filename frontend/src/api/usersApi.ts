import { httpClient } from './httpClient';
import type { UserRole } from '../types/auth';

export interface UserAccount {
  id: number;
  username: string;
  displayName: string;
  role: UserRole;
}

export async function getUsers(): Promise<UserAccount[]> {
  const res = await httpClient.get<UserAccount[]>('/users');
  return res.data;
}

export async function updateUserRole(id: number, role: UserRole): Promise<UserAccount> {
  const res = await httpClient.put<UserAccount>(`/users/${id}/role`, { role });
  return res.data;
}
