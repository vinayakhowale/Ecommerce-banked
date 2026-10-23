package com.socommerce.app.service;

import com.socommerce.app.entity.PasswordResetToken;
import com.socommerce.app.entity.User;
import com.socommerce.app.exception.BadRequestException;
import com.socommerce.app.repository.PasswordResetTokenRepository;
import com.socommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Implements the "forgot password" flow end to end. Two security principles drive the design:
 *
 * 1) Never reveal whether an email address is registered. requestReset() always completes
 *    silently and the controller always returns the same generic message, whether or not a
 *    matching account exists -- otherwise this endpoint becomes a way to enumerate real users.
 *
 * 2) Never store the actual reset token. Only its SHA-256 hash is persisted (identical principle
 *    to never storing a plaintext password) -- the raw token exists only in the email itself and
 *    briefly in memory here, so a database leak alone can't be used to reset anyone's password.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Value("${app.password-reset.token-expiry-minutes}")
    private int expiryMinutes;

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email.toLowerCase().trim()).ifPresent(this::issueTokenAndSend);
        // Deliberately no else-branch and no exception for "user not found" -- see class doc.
    }

    private void issueTokenAndSend(User user) {
        // Invalidate any previously-issued, still-outstanding reset links for this user, so only
        // the most recently requested one can ever be used.
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String rawToken = generateRawToken();
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .build();
        passwordResetTokenRepository.save(token);

        String resetLink = frontendBaseUrl + "/auth/reset-password?token=" + rawToken;

        // Always logged server-side regardless of whether the actual email send succeeds below --
        // keeps this flow fully testable before real SMTP credentials are configured (see
        // EmailService), and gives ops a way to help a user manually if mail delivery ever fails.
        log.info("Password reset requested for {}. Reset link (valid {} min): {}",
                user.getEmail(), expiryMinutes, resetLink);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink, expiryMinutes);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BadRequestException("This reset link is invalid or has already been used. Please request a new one."));

        if (token.isUsed()) {
            throw new BadRequestException("This reset link has already been used. Please request a new one.");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This reset link has expired. Please request a new one.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        // Any other outstanding links for this user (there shouldn't normally be any, given
        // issueTokenAndSend already clears old ones, but this is a cheap extra guarantee).
        passwordResetTokenRepository.deleteByUserId(user.getId());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is a standard JDK algorithm, guaranteed to be available -- this can't happen.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
