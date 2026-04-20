package com.futuremedia.futureclientformapi.controllers;

import com.futuremedia.futureclientformapi.dto.user.UserRequest;
import com.futuremedia.futureclientformapi.dto.user.UserResponse;
import com.futuremedia.futureclientformapi.services.AnalyticsService;
import com.futuremedia.futureclientformapi.services.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public class AdminController {
    private final UserService userService;
    private final AnalyticsService analyticsService;

    public AdminController(UserService userService, AnalyticsService analyticsService) {
        this.userService = userService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return userService.getAllUsers();
    }

    @PostMapping("/users")
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/users/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/users/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/analytics")
    public Map<String, Object> analytics() {
        return analyticsService.adminStats();
    }
}
