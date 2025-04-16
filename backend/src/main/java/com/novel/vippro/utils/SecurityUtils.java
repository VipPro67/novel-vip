package com.novel.vippro.utils;

import com.novel.vippro.models.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class SecurityUtils {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    public UUID getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
}