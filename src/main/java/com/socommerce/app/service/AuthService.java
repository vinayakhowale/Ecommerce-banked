package com.socommerce.app.service;

import com.socommerce.app.dto.AuthDtos.AuthResponse;
import com.socommerce.app.dto.AuthDtos.LoginRequest;
import com.socommerce.app.dto.AuthDtos.RegisterRequest;
import com.socommerce.app.dto.UserDto;
import com.socommerce.app.entity.RoleName;
import com.socommerce.app.entity.User;
import com.socommerce.app.exception.BadRequestException;
import com.socommerce.app.repository.UserRepository;
import com.socommerce.app.security.JwtService;
import com.socommerce.app.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        // Normalise once, then use the same value for the duplicate check and the insert, so
        // "Name@Mail.com " and "name@mail.com" can never become two separate accounts (and a
        // mixed-case signup can't slip past the check only to fail on the unique constraint).
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("You already have an account with this email. Log in, or reset your password if you've forgotten it.");
        }
        User user = User.builder()
                .name(req.name().trim())
                .email(email)
                .password(passwordEncoder.encode(req.password()))
                .phone(req.phone() == null ? null : req.phone().trim())
                .role(RoleName.USER)
                .active(true)
                .build();
        user = userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal, Map.of("role", user.getRole().name(), "uid", user.getId()));
        return new AuthResponse(token, UserDto.from(user));
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email().trim().toLowerCase(), req.password()));
        } catch (AuthenticationException e) {
            // Covers BadCredentialsException (wrong password/unknown user), DisabledException
            // (inactive account), LockedException, etc. — all should read as a plain, safe
            // message to the client rather than leaking which case occurred.
            if (e.getClass().getSimpleName().equals("DisabledException")) {
                throw new BadRequestException("This account has been deactivated. Please contact support.");
            }
            throw new BadRequestException("Invalid email or password");
        }

        User user = userRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal, Map.of("role", user.getRole().name(), "uid", user.getId()));
        return new AuthResponse(token, UserDto.from(user));
    }
}
