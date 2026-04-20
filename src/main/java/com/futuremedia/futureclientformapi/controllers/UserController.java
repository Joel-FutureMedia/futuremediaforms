package com.futuremedia.futureclientformapi.controllers;

import com.futuremedia.futureclientformapi.services.AnalyticsService;
import com.futuremedia.futureclientformapi.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final AnalyticsService analyticsService;

    //

    public UserController(UserService userService, AnalyticsService analyticsService) {
        this.userService = userService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics")
    public Map<String, Object> analytics(Authentication authentication) {
        return analyticsService.userStats(userService.getByEmail(authentication.getName()));
    }
}
