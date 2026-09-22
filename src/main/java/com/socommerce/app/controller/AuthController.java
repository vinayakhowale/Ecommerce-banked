package com.socommerce.app.controller;

import com.socommerce.app.dto.AuthDtos.AuthResponse;
import com.socommerce.app.dto.AuthDtos.ForgotPasswordRequest;
import com.socommerce.app.dto.AuthDtos.LoginRequest;
import com.socommerce.app.dto.AuthDtos.RegisterRequest;
import com.socommerce.app.dto.AuthDtos.ResetPasswordRequest;
import com.socommerce.app.dto.UserDto;
import com.socommerce.app.security.UserPrincipal;
import com.socommerce.app.service.AuthService;
import com.socommerce.app.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(UserDto.from(principal.getUser()));
    }

    /**
     * Always returns the same generic message whether or not the email is actually registered --
     * see PasswordResetService for why this matters (prevents user enumeration).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok(Map.of(
                "message", "If an account exists for that email, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Your password has been reset. You can now log in."));
    }
}
