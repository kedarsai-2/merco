package com.mercotrace.service;

/**
 * Resolves the current trader id for request-scoped operations.
 * Default implementation returns the dev seed trader (101). In production this can be
 * replaced with an implementation that resolves the trader from the authenticated user
 * (e.g. SecurityUtils.getCurrentUserLogin() and User.traderId when that column exists).
 */
public interface TraderContextService {

    /**
     * @return the current trader id (e.g. 101 in dev; from JWT/User in production).
     */
    Long getCurrentTraderId();
}
