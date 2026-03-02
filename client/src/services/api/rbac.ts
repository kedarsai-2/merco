/**
 * RBAC API — backend only. No localStorage.
 * Roles: GET/POST/PUT/DELETE /api/roles.
 * Profiles / user-role assignments: use /api/admin/users where applicable; some endpoints may not exist yet (TODO).
 */
import type { Role as RbacRole, Profile as RbacProfile, UserRole } from '@/types/rbac';
import { apiFetch } from './http';

type RoleDTO = {
  id?: number;
  roleName?: string;
  description?: string;
  createdAt?: string;
  modulePermissions?: RbacRole['permissions'];
};
type BackendRoleList = RoleDTO[];

type AdminUserDTO = {
  id?: number;
  login?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  activated?: boolean;
};

function mapRoleDtoToRole(dto: RoleDTO): RbacRole {
  return {
    id: dto.id != null ? String(dto.id) : '',
    name: (dto as { roleName?: string }).roleName ?? '',
    description: (dto as { description?: string }).description ?? '',
    permissions: (dto.modulePermissions as RbacRole['permissions']) ?? {},
    created_at: (dto as { createdAt?: string }).createdAt ?? new Date().toISOString(),
    updated_at: (dto as { updatedAt?: string }).updatedAt ?? new Date().toISOString(),
  };
}

async function handleRes<T>(res: Response, msg: string): Promise<T> {
  if (res.ok) return res.json() as Promise<T>;
  let detail = msg;
  try {
    const ct = res.headers.get('content-type') || '';
    if (ct.includes('application/json')) {
      const j = await res.json() as { detail?: string; message?: string };
      if (typeof j.detail === 'string') detail = j.detail;
      else if (typeof j.message === 'string') detail = j.message;
    }
  } catch {
    // ignore
  }
  throw new Error(detail);
}

export const rbacApi = {
  async listRoles(): Promise<RbacRole[]> {
    const params = new URLSearchParams({ page: '0', size: '500' });
    const res = await apiFetch(`/roles?${params.toString()}`, { method: 'GET' });
    const data = await handleRes<BackendRoleList>(res, 'Failed to load roles');
    return Array.isArray(data) ? data.map(mapRoleDtoToRole) : [];
  },

  async createRole(data: { name: string; description: string; permissions: RbacRole['permissions'] }): Promise<RbacRole> {
    const res = await apiFetch('/roles', {
      method: 'POST',
      body: JSON.stringify({ roleName: data.name, description: data.description, modulePermissions: data.permissions }),
    });
    const dto = await handleRes<RoleDTO>(res, 'Failed to create role');
    return mapRoleDtoToRole(dto);
  },

  async updateRole(
    roleId: string,
    data: { name: string; description: string; permissions: RbacRole['permissions'] },
  ): Promise<RbacRole> {
    const res = await apiFetch(`/roles/${encodeURIComponent(roleId)}`, {
      method: 'PUT',
      body: JSON.stringify({
        id: Number(roleId),
        roleName: data.name,
        description: data.description,
        modulePermissions: data.permissions,
      }),
    });
    const dto = await handleRes<RoleDTO>(res, 'Failed to update role');
    return mapRoleDtoToRole(dto);
  },

  async deleteRole(roleId: string): Promise<void> {
    const res = await apiFetch(`/roles/${encodeURIComponent(roleId)}`, { method: 'DELETE' });
    if (!res.ok) await handleRes<unknown>(res, 'Failed to delete role');
  },

  async listProfiles(): Promise<RbacProfile[]> {
    try {
      const params = new URLSearchParams({ page: '0', size: '500' });
      const res = await apiFetch(`/admin/users?${params.toString()}`, { method: 'GET' });
      const data = await handleRes<AdminUserDTO[] | { content?: AdminUserDTO[] }>(res, 'Failed to load users');
      const users: AdminUserDTO[] = Array.isArray(data) ? data : data.content ?? [];
      return users.map((u) => ({
        id: String(u.id ?? ''),
        user_id: String(u.id ?? ''),
        full_name: ([u.firstName, u.lastName].filter(Boolean).join(' ') || u.login) ?? '',
        email: u.email ?? '',
        mobile: null,
        status: u.activated ? 'active' : 'inactive',
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString(),
      }));
    } catch {
      return [];
    }
  },

  async createProfile(data: { full_name: string; email: string; mobile: string | null }): Promise<RbacProfile> {
    const [first, ...rest] = (data.full_name || '').trim().split(/\s+/);
    const res = await apiFetch('/admin/users', {
      method: 'POST',
      body: JSON.stringify({
        login: data.email?.split('@')[0] ?? data.email,
        firstName: first ?? '',
        lastName: rest.join(' ') ?? '',
        email: data.email,
        activated: true,
      }),
    });
    const u = await handleRes<{ id?: number; login?: string; firstName?: string; lastName?: string; email?: string; activated?: boolean }>(res, 'Failed to create user');
    return {
      id: String(u.id ?? ''),
      user_id: String(u.id ?? ''),
      full_name: data.full_name,
      email: data.email,
      mobile: data.mobile,
      status: u.activated ? 'active' : 'inactive',
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString(),
    };
  },

  async updateProfile(profileId: string, data: { full_name?: string; email?: string; mobile?: string | null }): Promise<RbacProfile> {
    const [first, ...rest] = (data.full_name ?? '').trim().split(/\s+/);
    const res = await apiFetch(`/admin/users`, {
      method: 'PUT',
      body: JSON.stringify({
        id: Number(profileId),
        firstName: first ?? '',
        lastName: rest.join(' ') ?? '',
        email: data.email,
      }),
    });
    const u = await handleRes<{ id?: number; login?: string; firstName?: string; lastName?: string; email?: string; activated?: boolean }>(res, 'Failed to update user');
    return {
      id: String(u.id ?? profileId),
      user_id: String(u.id ?? profileId),
      full_name: ([u.firstName, u.lastName].filter(Boolean).join(' ') || u.login) ?? '',
      email: u.email ?? '',
      mobile: data.mobile ?? null,
      status: u.activated ? 'active' : 'inactive',
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString(),
    };
  },

  async setProfileStatus(profileId: string, _status: 'active' | 'inactive'): Promise<void> {
    await apiFetch(`/admin/users/${encodeURIComponent(profileId)}`, {
      method: 'PATCH',
      body: JSON.stringify({ activated: _status === 'active' }),
    });
  },

  async listUserRoles(): Promise<UserRole[]> {
    const params = new URLSearchParams({ page: '0', size: '500' });
    const res = await apiFetch(`/admin/user-roles?${params.toString()}`, { method: 'GET' });
    const data = await handleRes<Array<{ id?: number; userId?: number; roleId?: number; assignedBy?: string | null; createdAt?: string }>>(
      res,
      'Failed to load user roles',
    );
    if (!Array.isArray(data)) return [];
    return data.map((ur) => ({
      id: String(ur.id ?? ''),
      user_id: String(ur.userId ?? ''),
      role_id: String(ur.roleId ?? ''),
      assigned_by: ur.assignedBy ?? null,
      created_at: ur.createdAt ?? new Date().toISOString(),
    }));
  },

  async setUserRoles(profileId: string, roleIds: string[]): Promise<void> {
    const numericIds = roleIds.map((r) => Number(r)).filter((n) => !Number.isNaN(n));
    await apiFetch(`/admin/users/${encodeURIComponent(profileId)}/roles`, {
      method: 'PUT',
      body: JSON.stringify(numericIds),
    });
  },
};
