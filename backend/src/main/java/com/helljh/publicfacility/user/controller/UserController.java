package com.helljh.publicfacility.user.controller;

import com.helljh.publicfacility.user.dto.*;
import com.helljh.publicfacility.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(
            @Valid @RequestBody SignupRequest request
    ) {
        return userService.signup(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return userService.login(request);
    }

    @PostMapping("/refresh")
    public TokenRefreshResponse refresh(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        return userService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        userService.logout(request);
    }
}