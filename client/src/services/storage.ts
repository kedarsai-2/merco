export const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms));

export const id = () => crypto.randomUUID();

export const now = () => new Date().toISOString();

/**
 * Legacy store: array from localStorage by key.
 * Do NOT use for business data: weighing, print logs, auction.
 * Those use backend APIs only (weighingApi, printLogApi, useAuctionResults/auctionApi).
 * OK for optional UI prefs or legacy keys until migrated (e.g. arrivals until API is primary).
 */
export function getStore<T>(key: string): T[] {
  try {
    return JSON.parse(localStorage.getItem(key) || '[]');
  } catch {
    return [];
  }
}

/**
 * Legacy store: persist array to localStorage.
 * Do NOT use for weighing, print logs, or auction data; use backend APIs instead.
 */
export function setStore<T>(key: string, data: T[]) {
  localStorage.setItem(key, JSON.stringify(data));
}
