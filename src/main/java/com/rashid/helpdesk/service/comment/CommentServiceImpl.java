package com.rashid.helpdesk.service.comment;

import com.rashid.helpdesk.common.NotFoundException;
import com.rashid.helpdesk.dto.CommentCreateRequest;
import com.rashid.helpdesk.dto.CommentResponse;
import com.rashid.helpdesk.entity.Comment;
import com.rashid.helpdesk.entity.Ticket;
import com.rashid.helpdesk.entity.User;
import com.rashid.helpdesk.mapper.CommentMapper;
import com.rashid.helpdesk.repository.CommentRepository;
import com.rashid.helpdesk.repository.TicketRepository;
import com.rashid.helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentResponse addComment(UUID tenantId, UUID ticketId, CommentCreateRequest request) {
        Ticket ticket = ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        User author = userRepository.findById(request.authorUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Comment comment = Comment.builder()
                .ticket(ticket)
                .author(author)
                .body(request.body())
                .internal(request.internal())
                .build();

        comment = commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> listForTicket(UUID tenantId, UUID ticketId) {
        // confirms the ticket belongs to this tenant before returning its comments
        ticketRepository.findByIdAndTenantId(ticketId, tenantId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(commentMapper::toResponse)
                .toList();
    }
}