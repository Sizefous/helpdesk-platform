package com.rashid.helpdesk.service.comment;

import com.rashid.helpdesk.common.NotFoundException;
import com.rashid.helpdesk.dto.CommentCreateRequest;
import com.rashid.helpdesk.dto.CommentResponse;
import com.rashid.helpdesk.entity.Comment;
import com.rashid.helpdesk.entity.Tenant;
import com.rashid.helpdesk.entity.Ticket;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.enums.Role;
import com.rashid.helpdesk.mapper.CommentMapper;
import com.rashid.helpdesk.repository.CommentRepository;
import com.rashid.helpdesk.repository.TicketRepository;
import com.rashid.helpdesk.repository.UserRepository;
import com.rashid.helpdesk.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl service;

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

    private Comment comment(User author, String body, boolean internal) {
        return Comment.builder()
                .id(UUID.randomUUID())
                .ticket(ticket)
                .author(author)
                .body(body)
                .internal(internal)
                .createdAt(Instant.now())
                .build();
    }

    private CommentResponse responseFor(Comment c) {
        return new CommentResponse(
                c.getId(), c.getTicket().getId(), c.getAuthor().getId(), c.getAuthor().getFullName(),
                c.getBody(), c.isInternal(), c.getCreatedAt());
    }

    // ---- addComment ----

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void addComment_validRequest_savesCommentWithAuthorAndInternalFlag(boolean internal) {
        CommentCreateRequest request = new CommentCreateRequest("Looking into it", internal);
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.of(ticket));
        when(userRepository.findById(agent.getId())).thenReturn(Optional.of(agent));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(commentMapper.toResponse(any(Comment.class))).thenAnswer(inv -> responseFor(inv.getArgument(0)));

        CommentResponse result = service.addComment(tenant.getId(), ticket.getId(), agent.getId(), request);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        Comment saved = captor.getValue();
        assertThat(saved.getTicket()).isSameAs(ticket);
        assertThat(saved.getAuthor()).isSameAs(agent);
        assertThat(saved.getBody()).isEqualTo("Looking into it");
        assertThat(saved.isInternal()).isEqualTo(internal);

        assertThat(result.internal()).isEqualTo(internal);
        assertThat(result.authorId()).isEqualTo(agent.getId());
    }

    @Test
    void addComment_ticketOutsideTenant_throwsNotFoundAndTouchesNothingElse() {
        CommentCreateRequest request = new CommentCreateRequest("hello", false);
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addComment(tenant.getId(), ticket.getId(), agent.getId(), request))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(userRepository, commentRepository);
    }

    @Test
    void addComment_unknownAuthor_throwsNotFoundAndSavesNothing() {
        CommentCreateRequest request = new CommentCreateRequest("hello", false);
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.of(ticket));
        when(userRepository.findById(agent.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addComment(tenant.getId(), ticket.getId(), agent.getId(), request))
                .isInstanceOf(NotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    // ---- listForTicket ----

    @Test
    void listForTicket_ticketInTenant_returnsMappedCommentsInOrder() {
        Comment first = comment(customer, "It is broken", false);
        Comment second = comment(agent, "Investigating", true);
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.of(ticket));
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId())).thenReturn(List.of(first, second));
        when(commentMapper.toResponse(first)).thenReturn(responseFor(first));
        when(commentMapper.toResponse(second)).thenReturn(responseFor(second));

        List<CommentResponse> result = service.listForTicket(tenant.getId(), ticket.getId());

        assertThat(result)
                .extracting(CommentResponse::body)
                .containsExactly("It is broken", "Investigating");
    }

    @Test
    void listForTicket_ticketOutsideTenant_throwsNotFoundWithoutReadingComments() {
        when(ticketRepository.findByIdAndTenantId(ticket.getId(), tenant.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listForTicket(tenant.getId(), ticket.getId()))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(commentRepository, commentMapper);
    }
}