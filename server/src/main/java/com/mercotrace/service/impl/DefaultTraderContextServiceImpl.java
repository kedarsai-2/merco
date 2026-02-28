package com.mercotrace.service.impl;

import com.mercotrace.service.TraderContextService;
import org.springframework.stereotype.Service;

/**
 * Default implementation: returns the first seeded trader (101) so that dev seed data
 * and module-auctions/lots are aligned. In production, replace with an implementation
 * that resolves the trader from the authenticated user (e.g. User.traderId).
 */
@Service
public class DefaultTraderContextServiceImpl implements TraderContextService {

    private static final long DEFAULT_TRADER_ID = 101L;

    @Override
    public Long getCurrentTraderId() {
        return DEFAULT_TRADER_ID;
    }
}
