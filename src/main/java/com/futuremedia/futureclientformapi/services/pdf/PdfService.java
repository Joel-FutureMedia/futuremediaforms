package com.futuremedia.futureclientformapi.services.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futuremedia.futureclientformapi.models.Form;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {
    private final ObjectMapper objectMapper;

    public PdfService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] generateFormPdf(Form form) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(out));
        Document document = new Document(pdfDocument);

        document.add(new Paragraph("Future Media Client Discovery Report")
                .setBold().setFontSize(22).setFontColor(ColorConstants.BLUE));
        document.add(new Paragraph(form.getCompanyName()).setFontSize(16));
        document.add(new Paragraph("Contact: " + form.getContactPerson()));
        document.add(new Paragraph("Email: " + form.getCompanyEmail()));
        document.add(new Paragraph("Status: " + form.getStatus().name()));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Structured Responses").setBold().setFontSize(14));
        document.add(new Paragraph(prettyJson(form.getFormPayloadJson())).setTextAlignment(TextAlignment.LEFT));

        document.close();
        return out.toByteArray();
    }

    private String prettyJson(String json) {
        try {
            Object raw = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(raw);
        } catch (Exception ex) {
            return json;
        }
    }
}
