// Mirrors the backend UserRole enum and CurrentUserDto.
export type UserRole =
  | 'QUALITY_MANAGER'
  | 'OPERATOR'
  | 'MAINTENANCE_TECHNICIAN'
  | 'ADMINISTRATOR';

export interface CurrentUser {
  username: string;
  displayName: string;
  role: UserRole;
}
