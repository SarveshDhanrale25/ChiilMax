package com.urbanservice.app.auth;

import com.urbanservice.app.users.User;
import com.urbanservice.app.users.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final UserRepository users;

    public CurrentUser(UserRepository users) { this.users = users; }

    public User requireActive() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }
        User user = users.findByEmail(auth.getName())
            .orElseThrow(() -> new AccessDeniedException("Account not found"));
        if (user.getStatus() != User.Status.ACTIVE) {
            throw new AccessDeniedException("Account is inactive");
        }
        return user;
    }
}