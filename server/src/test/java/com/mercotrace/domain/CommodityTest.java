package com.mercotrace.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mercotrace.web.rest.TestUtil;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CommodityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Commodity.class);
        Commodity c1 = createCommodity(1L, "Wheat");
        Commodity c2 = new Commodity();
        assertThat(c1).isNotEqualTo(c2);

        c2.setId(c1.getId());
        assertThat(c1).isEqualTo(c2);

        c2.setId(2L);
        assertThat(c1).isNotEqualTo(c2);
    }

    private static Commodity createCommodity(Long id, String name) {
        Commodity c = new Commodity();
        c.setId(id);
        c.setTraderId(1L);
        c.setCommodityName(name);
        c.setCreatedAt(Instant.now());
        return c;
    }
}
