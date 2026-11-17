package com.rashid.helpdesk.mapper;

import com.rashid.helpdesk.dto.UserResponse;
import com.rashid.helpdesk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}