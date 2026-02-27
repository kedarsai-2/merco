import type { Role as RbacRole, Profile as RbacProfile, UserRole } from '@/types/rbac';
import { delay, id, now, getStore, setStore } from '../storage';

const ROLES_KEY = 'mkt_rbac_roles';
const PROFILES_KEY = 'mkt_rbac_profiles';
const USER_ROLES_KEY = 'mkt_rbac_user_roles';

// LocalStorage-backed RBAC mock API (for development only, until real backend exists)

export const rbacMockApi = {
  // Roles --------------------------------------------------------
  async listRoles(): Promise<RbacRole[]> {
    await delay(150);
    return getStore<RbacRole>(ROLES_KEY);
  },

  async createRole(data: {
    name: string;
    description: string;
    permissions: RbacRole['permissions'];
  }): Promise<RbacRole> {
    await delay();
    const roles = getStore<RbacRole>(ROLES_KEY);
    const role: RbacRole = {
      id: id(),
      name: data.name,
      description: data.description,
      permissions: data.permissions,
      created_at: now(),
      updated_at: now(),
    };
    roles.push(role);
    setStore(ROLES_KEY, roles);
    return role;
  },

  async updateRole(
    roleId: string,
    data: { name: string; description: string; permissions: RbacRole['permissions'] },
  ): Promise<RbacRole> {
    await delay();
    const roles = getStore<RbacRole>(ROLES_KEY);
    const idx = roles.findIndex(r => r.id === roleId);
    if (idx === -1) throw new Error('Role not found');
    roles[idx] = {
      ...roles[idx],
      name: data.name,
      description: data.description,
      permissions: data.permissions,
      updated_at: now(),
    };
    setStore(ROLES_KEY, roles);
    return roles[idx];
  },

  async deleteRole(roleId: string): Promise<void> {
    await delay();
    const roles = getStore<RbacRole>(ROLES_KEY).filter(r => r.id !== roleId);
    setStore(ROLES_KEY, roles);

    // Also clean up any user-role assignments for this role
    const userRoles = getStore<UserRole>(USER_ROLES_KEY).filter(ur => ur.role_id !== roleId);
    setStore(USER_ROLES_KEY, userRoles);
  },

  // Profiles (admin/staff users) --------------------------------
  async listProfiles(): Promise<RbacProfile[]> {
    await delay(150);
    return getStore<RbacProfile>(PROFILES_KEY);
  },

  async createProfile(data: {
    full_name: string;
    email: string;
    mobile: string | null;
  }): Promise<RbacProfile> {
    await delay();
    const profiles = getStore<RbacProfile>(PROFILES_KEY);
    const profile: RbacProfile = {
      id: id(),
      user_id: id(), // separate identifier, not used for auth in mock layer
      full_name: data.full_name,
      email: data.email,
      mobile: data.mobile,
      status: 'active',
      created_at: now(),
      updated_at: now(),
    };
    profiles.push(profile);
    setStore(PROFILES_KEY, profiles);
    return profile;
  },

  async updateProfile(
    profileId: string,
    data: { full_name: string; email: string; mobile: string | null },
  ): Promise<RbacProfile> {
    await delay();
    const profiles = getStore<RbacProfile>(PROFILES_KEY);
    const idx = profiles.findIndex(p => p.id === profileId);
    if (idx === -1) throw new Error('Profile not found');
    profiles[idx] = {
      ...profiles[idx],
      full_name: data.full_name,
      email: data.email,
      mobile: data.mobile,
      updated_at: now(),
    };
    setStore(PROFILES_KEY, profiles);
    return profiles[idx];
  },

  async setProfileStatus(profileId: string, status: RbacProfile['status']): Promise<void> {
    await delay();
    const profiles = getStore<RbacProfile>(PROFILES_KEY);
    const idx = profiles.findIndex(p => p.id === profileId);
    if (idx === -1) throw new Error('Profile not found');
    profiles[idx] = { ...profiles[idx], status, updated_at: now() };
    setStore(PROFILES_KEY, profiles);
  },

  // User ↔ Role assignments -------------------------------------
  async listUserRoles(): Promise<UserRole[]> {
    await delay(150);
    return getStore<UserRole>(USER_ROLES_KEY);
  },

  async setUserRoles(userId: string, roleIds: string[]): Promise<void> {
    await delay();
    const existing = getStore<UserRole>(USER_ROLES_KEY);
    const remaining = existing.filter(ur => ur.user_id !== userId);
    const nextAssignments: UserRole[] = [
      ...remaining,
      ...roleIds.map(roleId => ({
        id: id(),
        user_id: userId,
        role_id: roleId,
        assigned_by: null,
        created_at: now(),
      })),
    ];
    setStore(USER_ROLES_KEY, nextAssignments);
  },
};

