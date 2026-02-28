// Thin re-export layer so that production API files do not contain mock logic.
// Vehicle CRUD is stubbed in api/vehicles.ts (use arrivals API). RBAC mock below.

export { rbacMockApi as rbacApi } from '../mocks/rbac';
