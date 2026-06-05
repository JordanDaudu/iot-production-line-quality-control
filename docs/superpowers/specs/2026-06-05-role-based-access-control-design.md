# Role-Based Access Control — Design

Date: 2026-06-05

## Goal

Enforce a role-based permission matrix across the app. The backend already has
partial enforcement via `@PreAuthorize`; the frontend currently has **no** role
gating (every signed-in user sees all nav items and can open every route). This
work aligns backend rules to the matrix and adds matching frontend gating, with a
single source of truth for permissions.

## Roles

`QUALITY_MANAGER`, `OPERATOR`, `MAINTENANCE_TECHNICIAN`, `ADMINISTRATOR`
(Spring authority is `ROLE_<name>`).

## Access matrix

| Page / Feature | QUALITY_MANAGER | OPERATOR | MAINT_TECH | ADMIN |
|---|:---:|:---:|:---:|:---:|
| Dashboard, Products, Reports | ✓ | ✓ | ✓ | ✓ |
| Quality | ✓ | ✓ | — | ✓ |
| Alerts (view) | ✓ | ✓ | ✓ | ✓ |
| Alerts (acknowledge / resolve) | ✓ | — | ✓ | ✓ |
| Simulation page + start/pause/stop/reset | — | — | — | ✓ |
| Fault injection (panel on Dashboard) | — | — | — | ✓ |
| Settings (read-only threshold view) | view | view | view | view |
| Settings (edit thresholds / manage users) | — | — | — | ✓ |

Notes:
- Settings is not in the original spec. Decision: keep it **visible to all
  (read-only)**, editing stays **admin-only** (already enforced). This preserves
  current behavior.
- Operators are **view-only** on Alerts (their only Alerts capability beyond the
  shared pages).
- Simulation control becomes **admin-only** — this removes the Operator's current
  ability to start/pause/stop/reset simulations.

## Approach (chosen: A — central permission module)

A single permission module is the source of truth; nav, routes, and in-page
feature checks all read from it. Replaces the scattered role literals currently
duplicated across pages (`MANAGE_ROLES` in AlertsPage, `isAdmin` in SettingsPage).

### Frontend

- **New `frontend/src/auth/permissions.ts`**
  - `ROUTE_ACCESS`: map of route path → allowed roles (omit/`null` = all roles).
  - Feature predicates: `canManageAlerts(role)`, `canInjectFaults(role)`,
    `canControlSimulation(role)`, `canEditThresholds(role)`.
  - `canAccessRoute(role, path)` helper.
- **`AppLayout.tsx`** — filter `NAV_ITEMS` by `canAccessRoute(user.role, item.to)`.
- **`AppRoutes.tsx`** — wrap restricted routes so an unauthorized role is
  redirected to `/` (Dashboard). Implemented via a small `<RequireAccess>` guard
  or by filtering the route list against `ROUTE_ACCESS`.
- **`DashboardPage.tsx`** — render `FaultInjectionPanel` only when
  `canInjectFaults(role)`.
- **`AlertsPage.tsx`** — replace local `MANAGE_ROLES` with `canManageAlerts(role)`
  (now includes `QUALITY_MANAGER`).
- **`SimulationControlPage.tsx`** — page is admin-only via route guard; controls
  also gated with `canControlSimulation(role)` for defense in depth.

### Backend (`@PreAuthorize` adjustments — backend remains source of truth)

- **`SimulationController`** — `start`, `pause`, `stop`, `reset`: change
  `hasAnyRole('OPERATOR','ADMINISTRATOR')` → `hasRole('ADMINISTRATOR')`.
  `faults` already `hasRole('ADMINISTRATOR')` (unchanged).
- **`AlertController`** — `acknowledge`, `resolve`: change
  `hasAnyRole('MAINTENANCE_TECHNICIAN','ADMINISTRATOR')` →
  `hasAnyRole('MAINTENANCE_TECHNICIAN','ADMINISTRATOR','QUALITY_MANAGER')`.
- **`DashboardController`** — add
  `@PreAuthorize("hasAnyRole('ADMINISTRATOR','OPERATOR','QUALITY_MANAGER')")` to
  `spc()` and `defectPareto()` only (these back the Quality page and are not used
  by the Dashboard page, so the Dashboard stays open to all).

## Testing / verification

- Check existing controller/security tests for assertions that an Operator can
  start a simulation; update to reflect admin-only.
- Manual: sign in as each role and confirm nav, route redirects, and hidden
  actions match the matrix; confirm forbidden API calls return 403.

## Out of scope

- No new roles, no changes to authentication (HTTP Basic), no changes to user
  seeding or the role enum.
