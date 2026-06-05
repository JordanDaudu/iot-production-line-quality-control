import type { UserRole } from '../types/auth';

/**
 * Single source of truth for role-based access in the UI. The backend enforces the same
 * rules with @PreAuthorize (it is authoritative); this module exists so the UI can hide
 * nav items, guard routes, and disable actions a role cannot use.
 *
 * Route access: a path maps to the roles allowed to open it. A path absent from this map
 * is open to every authenticated role. Feature predicates gate in-page actions that are
 * narrower than whole-page access (e.g. fault injection lives on the Dashboard).
 */
const ALL_ROLES: UserRole[] = ['QUALITY_MANAGER', 'OPERATOR', 'MAINTENANCE_TECHNICIAN', 'ADMINISTRATOR'];

const ROUTE_ACCESS: Record<string, UserRole[]> = {
  '/quality': ['ADMINISTRATOR', 'OPERATOR', 'QUALITY_MANAGER'],
  '/simulation': ['ADMINISTRATOR'],
};

/** True if the role may open the given route path. Unlisted paths are open to all roles. */
export function canAccessRoute(role: UserRole, path: string): boolean {
  const allowed = ROUTE_ACCESS[path];
  return allowed ? allowed.includes(role) : ALL_ROLES.includes(role);
}

/** Acknowledge / resolve alerts. Operators are view-only. */
export function canManageAlerts(role: UserRole): boolean {
  return role === 'QUALITY_MANAGER' || role === 'MAINTENANCE_TECHNICIAN' || role === 'ADMINISTRATOR';
}

/** Inject faults (panel on the Dashboard). Administrators only. */
export function canInjectFaults(role: UserRole): boolean {
  return role === 'ADMINISTRATOR';
}

/** Start / pause / stop / reset the simulation. Administrators only. */
export function canControlSimulation(role: UserRole): boolean {
  return role === 'ADMINISTRATOR';
}

/** Edit quality thresholds / manage users (Settings). Administrators only. */
export function canEditThresholds(role: UserRole): boolean {
  return role === 'ADMINISTRATOR';
}
