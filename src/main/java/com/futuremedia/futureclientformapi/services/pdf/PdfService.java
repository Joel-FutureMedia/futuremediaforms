package com.futuremedia.futureclientformapi.services.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futuremedia.futureclientformapi.models.Form;
import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {
    private static final Color PRIMARY_GRAY = new DeviceRgb(79, 79, 79);
    private static final Color SOFT_GRAY = new DeviceRgb(117, 117, 117);
    private static final Color LIGHT_GRAY = new DeviceRgb(245, 245, 245);
    private static final Color BORDER_GRAY = new DeviceRgb(217, 217, 217);
    private static final String LOGO_URL = "https://futuremedia.com.na/wp-content/uploads/2024/04/FM-Logo-Full-Blue-Grey.png";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private final ObjectMapper objectMapper;

    public PdfService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] generateFormPdf(Form form) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(out));
        Document document = new Document(pdfDocument);

        addLogo(document);
        document.add(new Paragraph("Client Discovery Report")
                .setBold()
                .setFontSize(21)
                .setFontColor(PRIMARY_GRAY)
                .setMarginTop(4)
                .setMarginBottom(8));
        document.add(new Paragraph("Future Media")
                .setFontSize(11)
                .setFontColor(SOFT_GRAY)
                .setMarginBottom(20));

        document.add(buildMetaTable(form));
        document.add(new Paragraph("Client Responses")
                .setBold()
                .setFontSize(14)
                .setFontColor(PRIMARY_GRAY)
                .setMarginTop(14)
                .setMarginBottom(8));
        document.add(buildResponsesTable(form));

        document.close();
        return out.toByteArray();
    }

    private void addLogo(Document document) {
        try {
            Image logo = new Image(ImageDataFactory.create(LOGO_URL))
                    .setHeight(44)
                    .setAutoScaleWidth(true)
                    .setMarginBottom(6);
            document.add(logo);
        } catch (Exception ex) {
            // PDF should still be generated even if remote logo is unreachable.
        }
    }

    private Table buildMetaTable(Form form) {
        Table meta = new Table(UnitValue.createPercentArray(new float[]{25, 75}))
                .useAllAvailableWidth();

        Style labelStyle = new Style()
                .setBold()
                .setFontColor(PRIMARY_GRAY)
                .setBackgroundColor(LIGHT_GRAY)
                .setBorder(new SolidBorder(BORDER_GRAY, 1))
                .setPadding(8);

        Style valueStyle = new Style()
                .setFontColor(PRIMARY_GRAY)
                .setBorder(new SolidBorder(BORDER_GRAY, 1))
                .setPadding(8);

        addMetaRow(meta, "Company", form.getCompanyName(), labelStyle, valueStyle);
        addMetaRow(meta, "Contact Person", form.getContactPerson(), labelStyle, valueStyle);
        addMetaRow(meta, "Email", form.getCompanyEmail(), labelStyle, valueStyle);
        addMetaRow(meta, "Status", form.getStatus().name(), labelStyle, valueStyle);
        addMetaRow(meta, "Submitted", DATE_FORMATTER.format(form.getCreatedAt()), labelStyle, valueStyle);

        return meta;
    }

    private void addMetaRow(Table table, String key, String value, Style labelStyle, Style valueStyle) {
        table.addCell(new Cell().add(new Paragraph(key)).addStyle(labelStyle));
        table.addCell(new Cell().add(new Paragraph(value == null ? "-" : value)).addStyle(valueStyle));
    }

    private Table buildResponsesTable(Form form) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{46, 54}))
                .useAllAvailableWidth();

        table.addHeaderCell(new Cell().add(new Paragraph("Question").setBold().setFontColor(PRIMARY_GRAY))
                .setBackgroundColor(LIGHT_GRAY)
                .setBorder(new SolidBorder(BORDER_GRAY, 1))
                .setPadding(8));
        table.addHeaderCell(new Cell().add(new Paragraph("Client Answer").setBold().setFontColor(PRIMARY_GRAY))
                .setBackgroundColor(LIGHT_GRAY)
                .setBorder(new SolidBorder(BORDER_GRAY, 1))
                .setPadding(8));

        List<QuestionAnswerRow> rows = extractRows(form.getFormPayloadJson());
        if (rows.isEmpty()) {
            table.addCell(defaultCell("No structured responses were provided."));
            table.addCell(defaultCell("-"));
            return table;
        }

        for (QuestionAnswerRow row : rows) {
            table.addCell(defaultCell(row.question()));
            table.addCell(defaultCell(row.answer()));
        }

        return table;
    }

    private Cell defaultCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setFontColor(PRIMARY_GRAY)
                .setBorder(new SolidBorder(BORDER_GRAY, 1))
                .setPadding(8);
    }

    private List<QuestionAnswerRow> extractRows(String json) {
        List<QuestionAnswerRow> rows = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            flatten(root, "", "", rows);
        } catch (Exception ex) {
            rows.add(new QuestionAnswerRow("Raw Response", safeText(json)));
        }
        return rows;
    }

    private void flatten(JsonNode node, String sectionNumber, String titlePath, List<QuestionAnswerRow> rows) {
        if (node == null || node.isNull()) {
            if (!sectionNumber.isBlank()) {
                rows.add(new QuestionAnswerRow(formatSectionLabel(sectionNumber, titlePath), "-"));
            }
            return;
        }

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            int fieldIndex = 1;
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String childSection = sectionNumber.isBlank()
                        ? String.valueOf(fieldIndex)
                        : sectionNumber + "." + fieldIndex;
                String childTitlePath = titlePath.isBlank()
                        ? prettifyKey(field.getKey())
                        : titlePath + " - " + prettifyKey(field.getKey());
                flatten(field.getValue(), childSection, childTitlePath, rows);
                fieldIndex++;
            }
            return;
        }

        if (node.isArray()) {
            List<String> values = new ArrayList<>();
            boolean complex = false;
            for (JsonNode item : node) {
                if (item.isObject() || item.isArray()) {
                    complex = true;
                    break;
                }
                values.add(item.asText());
            }
            if (complex) {
                for (int i = 0; i < node.size(); i++) {
                    String childSection = sectionNumber.isBlank()
                            ? String.valueOf(i + 1)
                            : sectionNumber + "." + (i + 1);
                    String childTitlePath = titlePath + " (Item " + (i + 1) + ")";
                    flatten(node.get(i), childSection, childTitlePath, rows);
                }
            } else {
                rows.add(new QuestionAnswerRow(
                        formatSectionLabel(sectionNumber, titlePath),
                        values.isEmpty() ? "-" : String.join(", ", values)
                ));
            }
            return;
        }

        rows.add(new QuestionAnswerRow(
                formatSectionLabel(sectionNumber, titlePath),
                safeText(node.asText())
        ));
    }

    private String formatSectionLabel(String sectionNumber, String titlePath) {
        String section = sectionNumber == null || sectionNumber.isBlank() ? "-" : sectionNumber;
        String title = titlePath == null || titlePath.isBlank() ? "Field" : titlePath;
        return "Section " + section + ": " + title;
    }

    private String prettifyKey(String key) {
        String spaced = key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("[_\\-]+", " ")
                .trim();
        if (spaced.isBlank()) {
            return "Field";
        }
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    private String safeText(String text) {
        if (text == null || text.isBlank()) {
            return "-";
        }
        return text;
    }

    private record QuestionAnswerRow(String question, String answer) {
    }
}
