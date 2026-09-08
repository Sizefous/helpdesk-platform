package com.rashid.helpdesk.service.comment;

import com.rashid.helpdesk.dto.CommentCreateRequest;
import com.rashid.helpdesk.dto.CommentResponse;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    CommentResponse addComment(UUID tenantId, UUID ticketId, CommentCreateRequest request);

    List<CommentResponse> listForTicket(UUID tenantId, UUID ticketId);
}