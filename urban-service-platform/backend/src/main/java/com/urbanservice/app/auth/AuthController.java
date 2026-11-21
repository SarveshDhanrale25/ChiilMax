package com.urbanservice.app.auth;

import com.urbanservice.app.users.User;
import com.urbanservice.app.users.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    public record RegisterRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 20) String phone
    ) {}

    public record UserResponse(Long id, String fullName, String email, String phone, User.Role role, User.Status status) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole(), u.getStatus());
        }
    }

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final CurrentUser currentUser;

    public AuthController(UserRepository users, PasswordEncoder encoder, CurrentUser currentUser) {
        this.users = users;
        this.encoder = encoder;
        this.currentUser = currentUser;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("application", "Urban Service Platform", "status", "UP");
    }

    @GetMapping("/auth/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken());
    }

    @PostMapping("/auth/register")
    @Transactional
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        String normalizedEmail = req.email().trim().toLowerCase();
        if (users.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("Email is already registered");
        }
        User u = new User();
        u.setFullName(req.fullName().trim());
        u.setEmail(normalizedEmail);
        u.setPhone(req.phone().trim());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole(User.Role.CUSTOMER); // Server enforces least-privileged role
        u.setStatus(User.Status.ACTIVE);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(users.save(u)));
    }

    @PostMapping("/admin/providers")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<UserResponse> createProvider(@Valid @RequestBody RegisterRequest req) {
        String normalizedEmail = req.email().trim().toLowerCase();
        if (users.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("Email is already registered");
        }
        User u = new User();
        u.setFullName(req.fullName().trim());
        u.setEmail(normalizedEmail);
        u.setPhone(req.phone().trim());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole(User.Role.PROVIDER);
        u.setStatus(User.Status.ACTIVE);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(users.save(u)));
    }

    @GetMapping("/auth/me")
    public UserResponse me() {
        return UserResponse.from(currentUser.requireActive());
    }
}