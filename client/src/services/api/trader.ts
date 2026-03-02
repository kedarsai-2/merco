import type { Trader } from '@/types/models';
import { API_BASE } from './http';
import { apiFetch } from './http';

type TraderDTO = {
  id?: number;
  businessName?: string;
  ownerName?: string;
  address?: string;
  mobile?: string;
  email?: string;
  city?: string;
  state?: string;
  pinCode?: string;
  category?: string;
  approvalStatus?: string;
  billPrefix?: string;
  createdAt?: string;
  updatedAt?: string;
};

function mapDtoToTrader(dto: TraderDTO): Trader {
  return {
    trader_id: String(dto.id ?? ''),
    business_name: dto.businessName ?? '',
    owner_name: dto.ownerName ?? '',
    address: dto.address ?? '',
    mobile: dto.mobile ?? '',
    email: dto.email ?? '',
    city: dto.city ?? '',
    state: dto.state ?? '',
    pin_code: dto.pinCode ?? '',
    category: dto.category ?? '',
    approval_status: (dto.approvalStatus as Trader['approval_status']) ?? 'PENDING',
    bill_prefix: dto.billPrefix ?? '',
    shop_photos: [],
    created_at: dto.createdAt ?? new Date().toISOString(),
    updated_at: dto.updatedAt ?? new Date().toISOString(),
  };
}

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
    return res.json();
  },

  /** Admin: list traders (GET /api/admin/traders). */
  async listForAdmin(params: { page?: number; size?: number } = {}): Promise<Trader[]> {
    const searchParams = new URLSearchParams();
    searchParams.set('page', String(params.page ?? 0));
    searchParams.set('size', String(params.size ?? 100));
    const res = await apiFetch(`/admin/traders?${searchParams.toString()}`, { method: 'GET' });
    if (!res.ok) throw new Error('Failed to load traders');
    const data = (await res.json()) as TraderDTO[];
    return (Array.isArray(data) ? data : []).map(mapDtoToTrader);
  },

  /** Admin: approve trader (PATCH /api/admin/traders/{id}/approve). */
  async approve(traderId: string): Promise<Trader> {
    const res = await apiFetch(`/admin/traders/${encodeURIComponent(traderId)}/approve`, { method: 'PATCH' });
    if (!res.ok) throw new Error('Failed to approve trader');
    const dto = (await res.json()) as TraderDTO;
    return mapDtoToTrader(dto);
  },
};

