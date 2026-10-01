package com.rashid.helpdesk.service.auth;

import com.rashid.helpdesk.dto.LoginRequest;
import com.rashid.helpdesk.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}