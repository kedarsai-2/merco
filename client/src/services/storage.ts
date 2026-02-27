export const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms));

export const id = () => crypto.randomUUID();

export const now = () => new Date().toISOString();

export function getStore<T>(key: string): T[] {
  try {
    return JSON.parse(localStorage.getItem(key) || '[]');
  } catch {
    return [];
  }
}

export function setStore<T>(key: string, data: T[]) {
  localStorage.setItem(key, JSON.stringify(data));
}
