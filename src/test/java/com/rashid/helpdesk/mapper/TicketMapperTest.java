package com.rashid.helpdesk.mapper;

import com.rashid.helpdesk.dto.TicketResponse;
import com.rashid.helpdesk.entity.Tenant;
import com.rashid.helpdesk.entity.Ticket;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.enums.Role;
import com.rashid.helpdesk.support.TestData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketMapperTest {

    private final TicketMapper mapper = new TicketMapper();

    @Test
    void toResponse_unassignedTicket_hasNullAgentFields() {
        Tenant tenant = TestData.tenant();
        User customer = TestData.user(tenant, Role.CUSTOMER);
        Ticket ticket = TestData.ticket(tenant, customer);

        TicketResponse response = mapper.toResponse(ticket);

        assertThat(response.id()).isEqualTo(ticket.getId());
        assertThat(response.subject()).isEqualTo(ticket.getSubject());
        assertThat(response.status()).isEqualTo(ticket.getStatus());
        assertThat(response.priority()).isEqualTo(ticket.getPriority());
        assertThat(response.createdByUserId()).isEqualTo(customer.getId());
        assertThat(response.createdByName()).isEqualTo(customer.getFullName());
        assertThat(response.assignedAgentId()).isNull();
        assertThat(response.assignedAgentName()).isNull();
    }

    @Test
    void toResponse_assignedTicket_includesAgentIdAndName() {
        Tenant tenant = TestData.tenant();
        User customer = TestData.user(tenant, Role.CUSTOMER);
        User agent = TestData.user(tenant, Role.AGENT);
        Ticket ticket = TestData.ticket(tenant, customer);
        ticket.setAssignedAgent(agent);

        TicketResponse response = mapper.toResponse(ticket);

        assertThat(response.assignedAgentId()).isEqualTo(agent.getId());
        assertThat(response.assignedAgentName()).isEqualTo(agent.getFullName());
    }
}