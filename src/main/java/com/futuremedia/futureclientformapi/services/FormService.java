package com.futuremedia.futureclientformapi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.futuremedia.futureclientformapi.dto.form.FormRequest;
import com.futuremedia.futureclientformapi.dto.form.FormResponse;
import com.futuremedia.futureclientformapi.models.Form;
import com.futuremedia.futureclientformapi.models.FormStatus;
import com.futuremedia.futureclientformapi.models.Role;
import com.futuremedia.futureclientformapi.models.User;
import com.futuremedia.futureclientformapi.repositories.FormRepository;
import com.futuremedia.futureclientformapi.services.email.EmailService;
import com.futuremedia.futureclientformapi.services.pdf.PdfService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FormService {
    private final FormRepository formRepository;
    private final EmailService emailService;
    private final PdfService pdfService;
    private final ObjectMapper objectMapper;

    public FormService(FormRepository formRepository, EmailService emailService, PdfService pdfService, ObjectMapper objectMapper) {
        this.formRepository = formRepository;
        this.emailService = emailService;
        this.pdfService = pdfService;
        this.objectMapper = objectMapper;
    }

    public FormResponse createForm(User owner, FormRequest request) {
        Form form = new Form();
        form.setUser(owner);
        form.setCompanyName(request.companyName());
        form.setCompanyEmail(request.companyEmail());
        form.setContactPerson(request.contactPerson());
        form.setFormPayloadJson(toPayloadJson(validateAndNormalize(request.formPayload())));
        Form saved = formRepository.save(form);
        emailService.sendSubmissionEmail(saved);
        return map(saved);
    }

    public List<FormResponse> getForms(User requester) {
        if (requester.getRole() == Role.ROLE_SUPER_ADMIN) {
            return formRepository.findAll().stream().map(this::map).toList();
        }
        return formRepository.findByUser(requester).stream().map(this::map).toList();
    }

    public FormResponse updateForm(User requester, Long id, FormRequest request) {
        Form form = getAccessibleForm(requester, id);
        form.setCompanyName(request.companyName());
        form.setCompanyEmail(request.companyEmail());
        form.setContactPerson(request.contactPerson());
        form.setFormPayloadJson(toPayloadJson(validateAndNormalize(request.formPayload())));
        form.setUpdatedAt(Instant.now());
        return map(formRepository.save(form));
    }

    public FormResponse updateStatus(User requester, Long id, FormStatus status) {
        Form form = getAccessibleForm(requester, id);
        form.setStatus(status);
        form.setUpdatedAt(Instant.now());
        return map(formRepository.save(form));
    }

    public byte[] generatePdf(User requester, Long id) {
        Form form = getAccessibleForm(requester, id);
        return pdfService.generateFormPdf(form);
    }

    private Form getAccessibleForm(User requester, Long formId) {
        Form form = formRepository.findById(formId).orElseThrow();
        if (requester.getRole() == Role.ROLE_SUPER_ADMIN || form.getUser().getId().equals(requester.getId())) {
            return form;
        }
        throw new IllegalArgumentException("Forbidden");
    }

    private FormResponse map(Form form) {
        return new FormResponse(
                form.getId(),
                form.getUser().getId(),
                form.getUser().getName(),
                form.getCompanyName(),
                form.getCompanyEmail(),
                form.getContactPerson(),
                parsePayload(form.getFormPayloadJson()),
                form.getStatus(),
                form.getCreatedAt(),
                form.getUpdatedAt()
        );
    }

    private JsonNode validateAndNormalize(JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            throw new IllegalArgumentException("formPayload must be a JSON object");
        }
        ObjectNode root = (ObjectNode) payload.deepCopy();
        JsonNode section1 = root.path("section1");
        JsonNode section3 = root.path("section3");
        JsonNode section4 = root.path("section4");
        JsonNode section5 = root.path("section5");
        if (!section1.isObject() || !section3.isArray() || !section4.isObject() || !section5.isObject()) {
            throw new IllegalArgumentException("formPayload must include section1, section3, section4, and section5");
        }
        String campaignType = section1.path("campaignType").asText("COMBINED");
        int brandsCount = section1.path("brands").isArray() ? section1.path("brands").size() : 0;
        int expectedSection3Count = "PER_BRAND".equals(campaignType) ? Math.max(brandsCount, 1) : 1;
        if (section3.size() != expectedSection3Count) {
            throw new IllegalArgumentException("section3 entries must match campaign logic (COMBINED=1, PER_BRAND=brand count)");
        }
        ensureQuestionKeys(section4, new String[]{"longTermPartnership", "multiPlatform", "opportunities"});
        ensureQuestionKeys(section5, new String[]{"decisionMakers", "approvalProcess", "timelines"});
        return root;
    }

    private void ensureQuestionKeys(JsonNode section, String[] keys) {
        for (String key : keys) {
            if (section.get(key) == null) {
                throw new IllegalArgumentException("Missing required field: " + key);
            }
        }
    }

    private String toPayloadJson(JsonNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid formPayload JSON");
        }
    }

    private JsonNode parsePayload(String payloadJson) {
        try {
            return objectMapper.readTree(payloadJson);
        } catch (Exception ex) {
            return objectMapper.createObjectNode()
                    .set("raw", objectMapper.convertValue(payloadJson, JsonNode.class));
        }
    }
}
