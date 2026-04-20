package com.futuremedia.futureclientformapi.controllers;

import com.futuremedia.futureclientformapi.dto.form.FormRequest;
import com.futuremedia.futureclientformapi.dto.form.FormResponse;
import com.futuremedia.futureclientformapi.models.FormStatus;
import com.futuremedia.futureclientformapi.services.FormService;
import com.futuremedia.futureclientformapi.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
public class FormController {
    private final FormService formService;
    private final UserService userService;

    public FormController(FormService formService, UserService userService) {
        this.formService = formService;
        this.userService = userService;
    }

    @GetMapping
    public List<FormResponse> list(Authentication auth) {
        return formService.getForms(userService.getByEmail(auth.getName()));
    }

    @PostMapping
    public FormResponse create(Authentication auth, @Valid @RequestBody FormRequest request) {
        return formService.createForm(userService.getByEmail(auth.getName()), request);
    }

    @PutMapping("/{id}")
    public FormResponse update(Authentication auth, @PathVariable Long id, @Valid @RequestBody FormRequest request) {
        return formService.updateForm(userService.getByEmail(auth.getName()), id, request);
    }

    @PatchMapping("/{id}/status")
    public FormResponse updateStatus(Authentication auth, @PathVariable Long id, @RequestParam FormStatus status) {
        return formService.updateStatus(userService.getByEmail(auth.getName()), id, status);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(Authentication auth, @PathVariable Long id) {
        byte[] data = formService.generatePdf(userService.getByEmail(auth.getName()), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=form-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}
