package com.rashid.helpdesk.mapper;

import com.rashid.helpdesk.dto.CommentResponse;
import com.rashid.helpdesk.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getFullName(),
                comment.getBody(),
                comment.isInternal(),
                comment.getCreatedAt()
        );
    }
}