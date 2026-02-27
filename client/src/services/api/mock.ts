// Thin re-export layer so that production API files do not contain mock logic.
// Actual mock implementations live under `client/src/services/mocks`.

export { vehicleMockApi as vehicleApi } from '../mocks/vehicles';
export { rbacMockApi as rbacApi } from '../mocks/rbac';
