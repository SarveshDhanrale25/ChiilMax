package com.urbanservice.app.auth;

import com.urbanservice.app.users.User;
import com.urbanservice.app.users.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    public DatabaseUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String rawEmail) throws UsernameNotFoundException {
        String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase();
        User user = users.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.getStatus() != User.Status.ACTIVE) {
            throw new DisabledException("Account is not active");
        }
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPasswordHash(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}