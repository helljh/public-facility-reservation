package com.helljh.publicfacility.user.controller;

import com.helljh.publicfacility.user.dto.UserMeResponse;
import com.helljh.publicfacility.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserService userService;

    @GetMapping("/me")
    public UserMeResponse getMyInfo(
            @AuthenticationPrincipal Long userId
    ) {
        return userService.getMyInfo(userId);
    }
}