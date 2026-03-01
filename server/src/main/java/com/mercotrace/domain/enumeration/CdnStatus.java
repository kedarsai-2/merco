package com.mercotrace.domain.enumeration;

/**
 * CDN lifecycle status (REQ-CDN-001). Frontend ACTIVE maps to DISPATCHED.
 */
public enum CdnStatus {
    DRAFT,
    DISPATCHED,
    RECEIVED,
    TRANSFERRED,
}
