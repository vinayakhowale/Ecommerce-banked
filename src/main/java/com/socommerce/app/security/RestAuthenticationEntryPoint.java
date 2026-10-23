package com.socommerce.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socommerce.app.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Ensures that requests rejected by the security filter chain itself (missing or invalid JWT
 * on a protected route) get the same JSON error shape as everything else, and — critically —
 * a real 401 status. Without this, Spring Security's default entry point returns a bare 403
 * with no body, which the frontend's interceptor (which specifically watches for 401 to trigger
 * logout + redirect to login) would never catch.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws java.io.IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError error = new ApiError(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Authentication is required to access this resource", null);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
