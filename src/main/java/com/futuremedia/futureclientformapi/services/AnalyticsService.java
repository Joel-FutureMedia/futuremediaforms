package com.futuremedia.futureclientformapi.services;

import com.futuremedia.futureclientformapi.models.FormStatus;
import com.futuremedia.futureclientformapi.models.User;
import com.futuremedia.futureclientformapi.repositories.FormRepository;
import com.futuremedia.futureclientformapi.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AnalyticsService {
    private final FormRepository formRepository;
    private final UserRepository userRepository;

    public AnalyticsService(FormRepository formRepository, UserRepository userRepository) {
        this.formRepository = formRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> userStats(User user) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalForms", formRepository.countByUser(user));
        stats.put("pendingForms", formRepository.countByUserAndStatus(user, FormStatus.Pending));
        stats.put("completedForms", formRepository.countByUserAndStatus(user, FormStatus.Completed));
        return stats;
    }

    public Map<String, Object> adminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalForms", formRepository.count());
        stats.put("pendingForms", formRepository.countByStatus(FormStatus.Pending));
        stats.put("completedForms", formRepository.countByStatus(FormStatus.Completed));
        stats.put("perUser", userRepository.findAll().stream().map(this::perUserStats).toList());
        return stats;
    }

    private Map<String, Object> perUserStats(User user) {
        Map<String, Object> x = new HashMap<>();
        x.put("userId", user.getId());
        x.put("name", user.getName());
        x.put("email", user.getEmail());
        x.put("totalForms", formRepository.countByUser(user));
        x.put("pendingForms", formRepository.countByUserAndStatus(user, FormStatus.Pending));
        x.put("completedForms", formRepository.countByUserAndStatus(user, FormStatus.Completed));
        return x;
    }
}
