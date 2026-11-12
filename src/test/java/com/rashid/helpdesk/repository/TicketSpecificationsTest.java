package com.rashid.helpdesk.repository;

import com.rashid.helpdesk.dto.TicketFilter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketSpecificationsTest {

    @Test
    void matching_nullTenant_isRejectedInsteadOfBuildingAnUnscopedQuery() {
        TicketFilter noFilters = new TicketFilter(null, null, null, null);

        assertThatThrownBy(() -> TicketSpecifications.matching(null, noFilters))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tenantId");
    }
}