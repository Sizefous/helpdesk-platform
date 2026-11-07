package com.rashid.helpdesk.mapper;

import com.rashid.helpdesk.dto.CommentResponse;
import com.rashid.helpdesk.entity.Comment;
import com.rashid.helpdesk.entity.Tenant;
import com.rashid.helpdesk.entity.Ticket;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.enums.Role;
import com.rashid.helpdesk.support.TestData;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    private final CommentMapper mapper = new CommentMapper();

    @Test
    void toResponse_mapsAllFields() {
        Tenant tenant = TestData.tenant();
        User agent = TestData.user(tenant, Role.AGENT);
        Ticket ticket = TestData.ticket(tenant, TestData.user(tenant, Role.CUSTOMER));
        Comment comment = Comment.builder()
                .id(UUID.randomUUID())
                .ticket(ticket)
                .author(agent)
                .body("Internal note")
                .internal(true)
                .createdAt(Instant.now())
                .build();

        CommentResponse response = mapper.toResponse(comment);

        assertThat(response.id()).isEqualTo(comment.getId());
        assertThat(response.ticketId()).isEqualTo(ticket.getId());
        assertThat(response.authorId()).isEqualTo(agent.getId());
        assertThat(response.authorName()).isEqualTo(agent.getFullName());
        assertThat(response.body()).isEqualTo("Internal note");
        assertThat(response.internal()).isTrue();
        assertThat(response.createdAt()).isEqualTo(comment.getCreatedAt());
    }
}