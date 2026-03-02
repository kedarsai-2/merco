package com.mercotrace.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO for the {@link com.mercotrace.domain.UserRole} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class UserRoleDTO implements Serializable {

    private Long id;

    private Long userId;

    private Long roleId;

    /**
     * Derived from createdBy audit field for frontend display (assigned_by).
     */
    private String assignedBy;

    /**
     * Derived from createdDate audit field for frontend display (created_at).
     */
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserRoleDTO)) {
            return false;
        }

        UserRoleDTO that = (UserRoleDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserRoleDTO{" +
            "id=" + getId() +
            ", userId=" + getUserId() +
            ", roleId=" + getRoleId() +
            ", assignedBy='" + getAssignedBy() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}

