package com.rashid.helpdesk.service.user;

import com.rashid.helpdesk.dto.UserCreateRequest;
import com.rashid.helpdesk.dto.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse create(UUID tenantId, UserCreateRequest request);
}