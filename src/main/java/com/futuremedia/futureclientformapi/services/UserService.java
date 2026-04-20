package com.futuremedia.futureclientformapi.services;

import com.futuremedia.futureclientformapi.dto.user.UserRequest;
import com.futuremedia.futureclientformapi.dto.user.UserResponse;
import com.futuremedia.futureclientformapi.models.Role;
import com.futuremedia.futureclientformapi.models.User;
import com.futuremedia.futureclientformapi.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::map).toList();
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setLocked(request.isLocked());
        return map(userRepository.save(user));
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id).orElseThrow();
        if (isDefaultSuperAdmin(user)) {
            throw new IllegalArgumentException("Default super admin cannot be edited");
        }
        user.setName(request.name());
        user.setEmail(request.email());
        if (!request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user.setRole(request.role());
        user.setLocked(request.isLocked());
        return map(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        if (isDefaultSuperAdmin(user)) {
            throw new IllegalArgumentException("Default super admin cannot be deleted");
        }
        userRepository.delete(user);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    private boolean isDefaultSuperAdmin(User user) {
        return user.getRole() == Role.ROLE_SUPER_ADMIN && "joel@simplyfound.com.na".equalsIgnoreCase(user.getEmail());
    }

    private UserResponse map(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isLocked(), user.getCreatedAt());
    }
}
