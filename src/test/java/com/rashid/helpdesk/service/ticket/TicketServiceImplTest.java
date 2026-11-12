package com.rashid.helpdesk.service.ticket;

import com.rashid.helpdesk.common.BadRequestException;
import com.rashid.helpdesk.common.NotFoundException;
import com.rashid.helpdesk.dto.TicketCreateRequest;
import com.rashid.helpdesk.dto.TicketResponse;
import com.rashid.helpdesk.entity.Tenant;
import com.rashid.helpdesk.entity.Ticket;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.enums.Role;
import com.rashid.helpdesk.enums.TicketPriority;
import com.rashid.helpdesk.enums.TicketStatus;
import com.rashid.helpdesk.mapper.TicketMapper;
import com.rashid.helpdesk.repository.TenantRepository;
import com.rashid.helpdesk.repository.TicketRepository;
import com.rashid.helpdesk.repository.UserRepository;
import com.rashid.helpdesk.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rashid.helpdesk.dto.PageResponse;
import com.rashid.helpdesk.dto.TicketFilter;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.mockito.ArgumentMatchers.eq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketServiceImpl service;

    private Tenant tenant;
    private User customer;
    private User agent;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        tenant = TestData.tenant();
        customer = TestData.user(tenant, Role.CUSTOMER);
        agent = TestData.user(tenant, Role.AGENT);
        ticket = TestData.ticket(tenant, customer);
    }

    private TicketResponse responseFor(Ticket t) {
        User assigned = t.getAssignedAgent();
        return new TicketResponse(
                t.getId(), t.getSubject(), t.getDescription(), t.getStatus(), t.getPriority(),
                t.getCreatedBy().getId(), t.getCreatedBy().getFullName(),
                assigned != null ? assigned.getId() : null,
                assigned != null ? assigned.getFullName() : null,
                t.getCreatedAt(), t.getUpdatedAt());
    }

    // ---- create ----

    @Test
    void create_validRequest_savesOpenTicketOwnedByCreator() {
        TicketCreateRequest request = new TicketCreateRequest(
                "Printer down", "Floor 2 printer is offline", TicketPriority.URGENT);
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(userRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class))).thenAnswer(inv -> responseFor(inv.getArgument(0)));

        TicketResponse result = service.create(tenant.getId(), customer.getId(), request);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        Ticket saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(saved.getPriority()).isEqualTo(TicketPriority.URGENT);
        assertThat(saved.getSubject()).isEqualTo("Printer down");
        assertThat(saved.getTenant()).isSameAs(tenant);
        assertThat(saved.getCreatedBy()).isSameAs(customer);
        assertThat(saved.getAssignedAgent()).isNull();

        assertThat(result.subject()).isEqualTo("Printer down");
        assertThat(result.status()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void create_unknownTenant_throwsNotFoundAndSavesNothing() {
        TicketCreateRequest request = new TicketCreateRequest("s", "d", TicketPriority.LOW);
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(tenant.getId(), customer.getId(), request))
                .isInstanceOf(NotFoundException.class);

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void create_unknownUser_throwsNotFoundAndSavesNothing() {
        TicketCreateRequest request = new TicketCreateRequest("s", "d", TicketPriority.LOW);
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(userRepository.findById(customer.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(tenant.getId(), customer.getId(), request))
                .isInstanceOf(NotFoundException.class);

        verify(ticketRepository, never()).save(any());
    }

    // ---- search ----

    @Test
    void search_returnsMappedPageWithPagingMetadata() {
        Pageable pageable = PageRequest.of(0, 20);
        Ticket second = TestData.ticket(tenant, customer);
        TicketFilter filter = new TicketFilter(TicketStatus.OPEN, null, null, null);
        when(ticketRepository.findAll(ArgumentMatchers.<Specification<Ticket>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(ticket, second), pageable, 45));
        when(ticketMapper.toResponse(ticket)).thenReturn(responseFor(ticket));
        when(ticketMapper.toResponse(second)).thenReturn(responseFor(second));

        PageResponse<TicketResponse> result = service.search(tenant.getId(), filter, pageable);

        assertThat(result.content())
                .extracting(TicketResponse::id)
                .containsExactly(ticket.getId(), second.getId());
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(45);
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    void search_noMatches_returnsEmptyPageWithoutMapping() {
        Pageable pageable = PageRequest.of(0, 20);
        when(ticketRepository.findAll(ArgumentMatchers.<Specification<Ticket>>any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        PageResponse<TicketResponse> result =
                service.search(tenant.getId(), new TicketFilter(null, null, null, null), pageable);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
        verifyNoInteractions(ticketMapper);
    }

    // ---- getForTenant ----

    @Test
    void getForTenant_ticketInTenant_returnsMappedTicket() {
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(responseFor(ticket));

        TicketResponse result = service.getForTenant(tenant.getId(), ticket.getId());

        assertThat(result.id()).isEqualTo(ticket.getId());
    }

    @Test
    void getForTenant_ticketOutsideTenant_throwsNotFound() {
        UUID otherTenantId = UUID.randomUUID();
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), otherTenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getForTenant(otherTenantId, ticket.getId()))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(ticketMapper);
    }

    // ---- assign ----

    @Test
    void assign_validTicketAndAgent_setsAssignedAgent() {
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.of(ticket));
          when(userRepository.findByIdAndTenantId(agent.getId(), tenant.getId())).thenReturn(Optional.of(agent));
        when(ticketMapper.toResponse(ticket)).thenAnswer(inv -> responseFor(ticket));

        TicketResponse result = service.assign(tenant.getId(), ticket.getId(), agent.getId());

        // assign() relies on JPA dirty checking, so assert on the entity state, not on save().
        assertThat(ticket.getAssignedAgent()).isSameAs(agent);
        assertThat(result.assignedAgentId()).isEqualTo(agent.getId());
    }

    @Test
    void assign_ticketOutsideTenant_throwsNotFoundWithoutLookingUpAgent() {
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign(tenant.getId(), ticket.getId(), agent.getId()))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void assign_unknownAgent_throwsNotFoundAndLeavesTicketUnassigned() {
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.of(ticket));
        when(userRepository.findByIdAndTenantId(agent.getId(), tenant.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign(tenant.getId(), ticket.getId(), agent.getId()))
                .isInstanceOf(NotFoundException.class);

        assertThat(ticket.getAssignedAgent()).isNull();
    }

    // ---- updateStatus ----

    @Test
    void updateStatus_validTicket_changesStatus() {
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenAnswer(inv -> responseFor(ticket));

        TicketResponse result = service.updateStatus(tenant.getId(), ticket.getId(), TicketStatus.IN_PROGRESS);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(result.status()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void updateStatus_ticketOutsideTenant_throwsNotFound() {
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(tenant.getId(), ticket.getId(), TicketStatus.CLOSED))
                .isInstanceOf(NotFoundException.class);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
    }


    @Test
    void assign_agentFromAnotherTenant_throwsNotFoundUsingTenantScopedLookup() {
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.of(ticket));
        // a scoped lookup finds nothing for a user who belongs to a different tenant
        when(userRepository.findByIdAndTenantId(agent.getId(), tenant.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign(tenant.getId(), ticket.getId(), agent.getId()))
                .isInstanceOf(NotFoundException.class);

        assertThat(ticket.getAssignedAgent()).isNull();
        // guards against regressing to the unscoped lookup
        verify(userRepository, never()).findById(any());
    }

    @Test
    void assign_customerAsAssignee_throwsBadRequestAndLeavesTicketUnassigned() {
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.of(ticket));
        when(userRepository.findByIdAndTenantId(customer.getId(), tenant.getId())).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> service.assign(tenant.getId(), ticket.getId(), customer.getId()))
                .isInstanceOf(BadRequestException.class);

        assertThat(ticket.getAssignedAgent()).isNull();
    }
}