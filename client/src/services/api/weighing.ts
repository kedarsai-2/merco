import { apiFetch } from './http';

const BASE = '/weighing-sessions';

export interface BagWeightEntryDTO {
  bagNumber: number;
  weight: number;
  timestamp: string;
}

export interface WeighingSessionDTO {
  id?: number;
  session_id: string;
  lot_id: number;
  bid_number: number;
  buyer_mark?: string;
  buyer_name?: string;
  seller_name?: string;
  lot_name?: string;
  total_bags: number;
  original_weight: number;
  net_weight: number;
  manual_entry?: boolean;
  deductions?: number;
  govt_deduction_applied?: boolean;
  round_off_applied?: boolean;
  bag_weights?: BagWeightEntryDTO[];
  created_at?: string;
}

export interface WeighingSessionCreateRequest {
  session_id: string;
  lot_id: number;
  bid_number: number;
  buyer_mark?: string;
  buyer_name?: string;
  seller_name?: string;
  lot_name?: string;
  total_bags: number;
  original_weight: number;
  net_weight: number;
  manual_entry?: boolean;
  deductions?: number;
  govt_deduction_applied?: boolean;
  round_off_applied?: boolean;
  bag_weights?: BagWeightEntryDTO[];
}

async function parseJsonOrThrow(res: Response, defaultMessage: string): Promise<never> {
  try {
    const b = await res.json().catch(() => ({}));
    throw new Error((b as { message?: string })?.message || defaultMessage);
  } catch (e) {
    if (e instanceof Error && e.message !== defaultMessage) throw e;
    throw new Error(defaultMessage);
  }
}

export const weighingApi = {
  async create(body: WeighingSessionCreateRequest): Promise<WeighingSessionDTO> {
    const res = await apiFetch(BASE, { method: 'POST', body: JSON.stringify(body) });
    if (!res.ok) await parseJsonOrThrow(res, 'Failed to save weighing session');
    return res.json();
  },
  async list(params: { page?: number; size?: number } = {}): Promise<WeighingSessionDTO[]> {
    const q = new URLSearchParams();
    if (params.page != null) q.set('page', String(params.page));
    if (params.size != null) q.set('size', String(params.size));
    const res = await apiFetch(`${BASE}?${q}`, { method: 'GET' });
    if (!res.ok) await parseJsonOrThrow(res, 'Failed to load weighing sessions');
    return res.json();
  },
  async getByBidNumber(bidNumber: number): Promise<WeighingSessionDTO | null> {
    const res = await apiFetch(`${BASE}/by-bid/${bidNumber}`, { method: 'GET' });
    if (res.status === 404) return null;
    if (!res.ok) await parseJsonOrThrow(res, 'Failed to load weighing session');
    return res.json();
  },
};
