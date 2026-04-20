package com.futuremedia.futureclientformapi.config;

import com.futuremedia.futureclientformapi.models.Role;
import com.futuremedia.futureclientformapi.models.User;
import com.futuremedia.futureclientformapi.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        userRepository.findByEmail("joel@simplyfound.com.na").orElseGet(() -> {
            User admin = new User();
            admin.setName("Default Super Admin");
            admin.setEmail("joel@simplyfound.com.na");
            admin.setPassword(passwordEncoder.encode("Kalimbwejoel@01"));
            admin.setRole(Role.ROLE_SUPER_ADMIN);
            admin.setLocked(false);
            return userRepository.save(admin);
        });
    }
}
