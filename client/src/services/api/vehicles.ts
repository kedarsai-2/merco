/**
 * Vehicles are created as part of Arrival (POST /api/arrivals).
 * There is no standalone Vehicle CRUD API. Use arrivalsApi for arrival+vehicle flow.
 */
const NOT_SUPPORTED = 'Vehicle CRUD is not available. Use arrivals API to create arrivals (which include vehicle data).';

export const vehicleApi = {
  async list(): Promise<never> {
    throw new Error(NOT_SUPPORTED);
  },
  async create(_data: unknown): Promise<never> {
    throw new Error(NOT_SUPPORTED);
  },
};
