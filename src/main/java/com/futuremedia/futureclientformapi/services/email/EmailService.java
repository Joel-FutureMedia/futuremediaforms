package com.futuremedia.futureclientformapi.services.email;

import com.futuremedia.futureclientformapi.models.Form;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSubmissionEmail(Form form) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(form.getCompanyEmail());
            helper.setSubject("Future Media - Discovery Form Received");
            String html = """
                    <div style="font-family:Arial,sans-serif;max-width:700px;margin:0 auto;color:#1F2937">
                      <img src="https://futuremedia.simplyfound.com.na/assets/logo-DQVfep2R.png" alt="Future Media Logo" style="height:70px;margin-bottom:24px;"/>
                      <p>Hi %s,</p>
                      <p>Our team will use the information provided to develop a customized marketing and media strategy aligned to your business objectives.</p>
                      <p>We look forward to partnering with you to deliver impactful, results-driven campaigns.</p>
                    </div>
                    """.formatted(form.getContactPerson());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception ignored) {
        }
    }
}
