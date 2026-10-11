package com.socommerce.app.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around Spring's JavaMailSender. Sending is entirely environment-driven (see
 * spring.mail.* in application.yml) -- swap SMTP providers purely via environment variables, no
 * code change or rebuild. If no real SMTP server is configured yet (placeholder credentials, or
 * dev/local testing), sending fails gracefully: it's logged as a warning rather than thrown, and
 * the caller (PasswordResetService) always logs the actual reset link server-side too -- so the
 * whole password-reset flow stays fully testable before any mail server is set up.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink, int expiryMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(toEmail);
            helper.setFrom((fromAddress == null || fromAddress.isBlank()) ? "no-reply@trendly.app" : fromAddress);
            helper.setSubject("Reset your Trendly password");
            helper.setText(buildHtml(userName, resetLink, expiryMinutes), true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            // Never let an email-sending failure break the request -- the link is already logged
            // by the caller, so the user can still be helped manually if SMTP isn't set up yet.
            log.warn("Could not send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildHtml(String userName, String resetLink, int expiryMinutes) {
        return "<div style=\"font-family:sans-serif;max-width:480px;margin:0 auto;padding:24px\">"
                + "<h2 style=\"color:#15121c\">Reset your password</h2>"
                + "<p>Hi " + escape(userName) + ",</p>"
                + "<p>We received a request to reset your Trendly password. Click the button below to choose a new one:</p>"
                + "<p style=\"margin:28px 0\"><a href=\"" + resetLink + "\" "
                + "style=\"background:#ff4b2e;color:#fff;padding:12px 24px;border-radius:999px;text-decoration:none;font-weight:bold\">"
                + "Reset Password</a></p>"
                + "<p style=\"color:#5b5568;font-size:13px\">This link expires in " + expiryMinutes + " minutes. "
                + "If you didn't request this, you can safely ignore this email.</p>"
                + "</div>";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("<", "&lt;").replace(">", "&gt;");
    }
}
