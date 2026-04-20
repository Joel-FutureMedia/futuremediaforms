package com.futuremedia.futureclientformapi.repositories;

import com.futuremedia.futureclientformapi.models.Form;
import com.futuremedia.futureclientformapi.models.FormStatus;
import com.futuremedia.futureclientformapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormRepository extends JpaRepository<Form, Long> {
    List<Form> findByUser(User user);
    long countByUser(User user);
    long countByUserAndStatus(User user, FormStatus status);
    long countByStatus(FormStatus status);
}
