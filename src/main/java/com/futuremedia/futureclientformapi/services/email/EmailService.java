package com.futuremedia.futureclientformapi.services.email;

import com.futuremedia.futureclientformapi.models.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final String mailFromAddress;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String mailFromAddress) {
        this.mailSender = mailSender;
        this.mailFromAddress = mailFromAddress;
    }

    @Async
    public void sendSubmissionEmail(Form form) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFromAddress);
            helper.setTo(form.getCompanyEmail());
            helper.setSubject("Future Media - Discovery Form Received");
            String html = """
                    <div style="margin:0;padding:24px;background-color:#f4f4f5;font-family:Arial,sans-serif;color:#444444;">
                      <div style="max-width:700px;margin:0 auto;background-color:#ffffff;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden;">
                        <div style="padding:24px 28px;border-bottom:1px solid #ededed;background-color:#fafafa;">
                          <img src="https://futuremedia.com.na/wp-content/uploads/2024/04/FM-Logo-Full-Blue-Grey.png" alt="Future Media Logo" style="height:56px;max-width:100%%;display:block;" />
                        </div>
                        <div style="padding:28px;">
                          <h2 style="margin:0 0 12px 0;color:#4b5563;font-size:24px;font-weight:700;">Discovery Form Received</h2>
                          <p style="margin:0 0 14px 0;color:#52525b;font-size:15px;line-height:1.7;">Hi %s,</p>
                          <p style="margin:0 0 14px 0;color:#52525b;font-size:15px;line-height:1.7;">
                            Thank you for completing your discovery form for <strong>%s</strong>. We have received your submission and our team is now reviewing it.
                          </p>
                          <p style="margin:0 0 20px 0;color:#52525b;font-size:15px;line-height:1.7;">
                            Our team will use the information provided to develop a customised marketing and media strategy aligned to your business objectives.
                          </p>
                          <div style="padding:14px 16px;background-color:#f3f4f6;border-left:4px solid #9ca3af;border-radius:8px;color:#4b5563;font-size:14px;line-height:1.6;">
                            We look forward to partnering with you to deliver impactful, results-driven campaigns.
                          </div>
                          <p style="margin:24px 0 0 0;color:#52525b;font-size:15px;line-height:1.7;">
                            Regards,<br/>
                            <strong>Future Media Team</strong>
                          </p>
                        </div>
                        <div style="padding:16px 28px;background-color:#fafafa;border-top:1px solid #ededed;color:#71717a;font-size:12px;">
                          Future Media | Building results-driven campaigns
                        </div>
                      </div>
                    </div>
                    """.formatted(form.getContactPerson(), form.getCompanyName());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send submission email to {}", form.getCompanyEmail(), ex);
        }
    }
}
