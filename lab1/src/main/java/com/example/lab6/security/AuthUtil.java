package com.example.lab6.security;

import entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import repository.UserRepository;

import java.util.Optional;

public class AuthUtil {
    public static boolean isAdmin(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        for (GrantedAuthority a : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equalsIgnoreCase(a.getAuthority())) return true;
        }
        return false;
    }

    public static Optional<User> currentUser(Authentication auth, UserRepository repo) {
        if (auth == null) return Optional.empty();
        return repo.findByLogin(auth.getName());
    }
}


