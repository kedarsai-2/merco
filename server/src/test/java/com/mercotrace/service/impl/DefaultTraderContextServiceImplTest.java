package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DefaultTraderContextServiceImplTest {

    @Test
    void getCurrentTraderIdReturnsDefaultTraderId() {
        DefaultTraderContextServiceImpl service = new DefaultTraderContextServiceImpl();
        assertThat(service.getCurrentTraderId()).isEqualTo(101L);
    }
}
