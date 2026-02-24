// ============================================================
// Mercotrace — Mock API Service Layer (localStorage-backed)
// Mirrors the RESTful API spec for seamless backend swap
// ============================================================

import type {
  Trader, User, Commodity, Contact, Vehicle,
  DynamicCharge, Voucher, BusinessCategory, AuthState
} from '@/types/models';

// ── Helpers ────────────────────────────────────────────────

const delay = (ms = 300) => new Promise(r => setTimeout(r, ms));
const id = () => crypto.randomUUID();
const now = () => new Date().toISOString();

function getStore<T>(key: string): T[] {
  try { return JSON.parse(localStorage.getItem(key) || '[]'); }
  catch { return []; }
}
function setStore<T>(key: string, data: T[]) {
  localStorage.setItem(key, JSON.stringify(data));
}

// ── Auth Module ────────────────────────────────────────────

const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

export const authApi = {
  async register(data: {
    business_name: string; owner_name: string; mobile: string; email: string;
    password: string; address: string; city: string; state: string;
    pin_code: string; category: string;
  }): Promise<{ trader: Trader; user: User; token: string }> {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });

    if (!res.ok) {
      let message = 'Registration failed. Please try again.';
      try {
        const contentType = res.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
          const problem = await res.json();
          if (typeof problem.detail === 'string' && problem.detail.includes('login already used')) {
            message = 'Email is already registered';
          } else if (typeof problem.detail === 'string' && problem.detail.includes('email address already used')) {
            message = 'Email is already registered';
          } else if (typeof problem.detail === 'string' && problem.detail.includes('Password must be at least 6 characters')) {
            message = 'Password must be at least 6 characters';
          } else if (typeof problem.detail === 'string' && problem.detail.trim().length > 0) {
            message = problem.detail;
          } else if (typeof problem.title === 'string' && problem.title.trim().length > 0) {
            message = problem.title;
          }
        } else {
          const text = await res.text();
          if (text && text.length < 200) {
            message = text;
          }
        }
      } catch {
        // ignore parse errors and keep default message
      }
      throw new Error(message);
    }

    const dataRes = await res.json();

    const user: User = {
      user_id: dataRes.user.user_id,
      trader_id: dataRes.user.trader_id,
      username: dataRes.user.username,
      is_active: dataRes.user.is_active,
      created_at: dataRes.user.created_at ?? new Date().toISOString(),
      name: dataRes.user.name,
      role: dataRes.user.role,
    };

    const trader: Trader = {
      trader_id: dataRes.trader.trader_id,
      business_name: dataRes.trader.business_name,
      owner_name: dataRes.trader.owner_name,
      address: dataRes.trader.address ?? '',
      category: dataRes.trader.category ?? '',
      approval_status: dataRes.trader.approval_status ?? 'PENDING',
      bill_prefix: dataRes.trader.bill_prefix ?? '',
      created_at: dataRes.trader.created_at ?? new Date().toISOString(),
      updated_at: dataRes.trader.updated_at ?? new Date().toISOString(),
      mobile: data.mobile,
      email: data.email,
      city: data.city,
      state: data.state,
      pin_code: data.pin_code,
      shop_photos: dataRes.trader.shop_photos ?? [],
    };

    const token: string = dataRes.token;

    return { trader, user, token };
  },

  async login(email: string, password: string): Promise<{ trader: Trader; user: User; token: string }> {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: email, password }),
    });

    if (!res.ok) {
      // Try to parse JHipster Problem JSON and surface a clean, user-friendly message
      let message = 'Login failed. Please try again.';
      try {
        const contentType = res.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
          const problem = await res.json();
          // Prefer our backend detail when available
          if (typeof problem.detail === 'string' && problem.detail.includes('Invalid email or password')) {
            message = 'Invalid email or password';
          } else if (typeof problem.detail === 'string' && problem.detail.includes('Password must be at least 6 characters')) {
            message = 'Password must be at least 6 characters';
          } else if (typeof problem.detail === 'string' && problem.detail.trim().length > 0) {
            message = problem.detail;
          } else if (typeof problem.title === 'string' && problem.title.trim().length > 0) {
            message = problem.title;
          }
        } else {
          const text = await res.text();
          if (text && text.length < 200) {
            message = text;
          }
        }
      } catch {
        // ignore parse errors and keep default message
      }
      throw new Error(message);
    }

    const data = await res.json();

    const user: User = {
      user_id: data.user.user_id,
      trader_id: data.user.trader_id,
      username: data.user.username,
      is_active: data.user.is_active,
      created_at: data.user.created_at ?? new Date().toISOString(),
      name: data.user.name,
      role: data.user.role,
    };

    const trader: Trader = {
      trader_id: data.trader.trader_id,
      business_name: data.trader.business_name,
      owner_name: data.trader.owner_name,
      address: data.trader.address ?? '',
      category: data.trader.category ?? '',
      approval_status: data.trader.approval_status ?? 'PENDING',
      bill_prefix: data.trader.bill_prefix ?? '',
      created_at: data.trader.created_at ?? new Date().toISOString(),
      updated_at: data.trader.updated_at ?? new Date().toISOString(),
      mobile: data.trader.mobile,
      email: data.trader.email,
      city: data.trader.city,
      state: data.trader.state,
      pin_code: data.trader.pin_code,
      shop_photos: data.trader.shop_photos ?? [],
    };

    const token: string = data.token;

    return { trader, user, token };
  },

  async getProfile(): Promise<{ trader: Trader; user: User } | null> {
    const saved = localStorage.getItem('mkt_auth');
    if (!saved) return null;
    const auth: AuthState = JSON.parse(saved);
    if (!auth.user || !auth.trader) return null;
    return { trader: auth.trader, user: auth.user };
  },
};

// ── Commodity Module ───────────────────────────────────────

export const commodityApi = {
  async list(): Promise<Commodity[]> {
    await delay(150);
    return getStore<Commodity>('mkt_commodities');
  },
  async create(data: Partial<Commodity>): Promise<Commodity> {
    await delay();
    const item: Commodity = { ...data, commodity_id: id(), created_at: now() } as Commodity;
    const list = getStore<Commodity>('mkt_commodities');
    list.push(item);
    setStore('mkt_commodities', list);
    return item;
  },
  async update(itemId: string, data: Partial<Commodity>): Promise<Commodity> {
    await delay();
    const list = getStore<Commodity>('mkt_commodities');
    const idx = list.findIndex(c => c.commodity_id === itemId);
    if (idx === -1) throw new Error('Commodity not found');
    list[idx] = { ...list[idx], ...data };
    setStore('mkt_commodities', list);
    return list[idx];
  },
  async remove(itemId: string): Promise<void> {
    await delay();
    const list = getStore<Commodity>('mkt_commodities').filter(c => c.commodity_id !== itemId);
    setStore('mkt_commodities', list);
  },
};

// ── Contact Module ─────────────────────────────────────────

export const contactApi = {
  async list(): Promise<Contact[]> {
    await delay(150);
    return getStore<Contact>('mkt_contacts');
  },
  async create(data: Partial<Contact>): Promise<Contact> {
    await delay();
    const item: Contact = { ...data, contact_id: id(), created_at: now(), opening_balance: 0, current_balance: 0 } as Contact;
    const list = getStore<Contact>('mkt_contacts');
    list.push(item);
    setStore('mkt_contacts', list);
    return item;
  },
  async update(itemId: string, data: Partial<Contact>): Promise<Contact> {
    await delay();
    const list = getStore<Contact>('mkt_contacts');
    const idx = list.findIndex(c => c.contact_id === itemId);
    if (idx === -1) throw new Error('Contact not found');
    list[idx] = { ...list[idx], ...data };
    setStore('mkt_contacts', list);
    return list[idx];
  },
  async search(mark: string): Promise<Contact[]> {
    await delay(100);
    return getStore<Contact>('mkt_contacts').filter(c => c.mark?.toLowerCase().includes(mark.toLowerCase()));
  },
};

// ── Vehicle / Arrivals Module ──────────────────────────────

export const vehicleApi = {
  async list(): Promise<Vehicle[]> {
    await delay(150);
    return getStore<Vehicle>('mkt_vehicles');
  },
  async create(data: Partial<Vehicle>): Promise<Vehicle> {
    await delay();
    const item: Vehicle = { ...data, vehicle_id: id(), created_at: now() } as Vehicle;
    const list = getStore<Vehicle>('mkt_vehicles');
    list.push(item);
    setStore('mkt_vehicles', list);
    return item;
  },
};

// ── Categories ─────────────────────────────────────────────

export const categoryApi = {
  async list(): Promise<BusinessCategory[]> {
    await delay(100);
    return getStore<BusinessCategory>('mkt_categories');
  },
};

// ── Trader Photos ────────────────────────────────────────────

export const traderApi = {
  async uploadPhotos(traderId: string, files: File[]): Promise<string[]> {
    const form = new FormData();
    files.forEach(f => form.append('files', f));

    const res = await fetch(`${API_BASE}/traders/${traderId}/photos`, {
      method: 'POST',
      body: form,
    });

    if (!res.ok) {
      throw new Error('Failed to upload photos');
    }
    return await res.json();
  },
};
