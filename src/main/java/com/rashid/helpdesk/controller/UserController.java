package com.rashid.helpdesk.controller;

import com.rashid.helpdesk.dto.UserCreateRequest;
import com.rashid.helpdesk.dto.UserResponse;
import com.rashid.helpdesk.security.AuthenticatedUser;
import com.rashid.helpdesk.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.create(principal.tenantId(), request));
    }
}