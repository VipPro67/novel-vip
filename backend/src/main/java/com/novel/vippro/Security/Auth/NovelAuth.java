package com.novel.vippro.Security.Auth;

import com.novel.vippro.Models.Novel;
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.Repository.NovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NovelAuth {

    private final NovelRepository novelRepository;

    public boolean canEdit(UUID novelId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UUID userId = UserDetailsImpl.getCurrentUserId();
        if (userId == null) {
            return false;
        }

        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            return false;
        }

        boolean isOwner = novel.getOwner() != null && userId.equals(novel.getOwner().getId());
        boolean canEditAny = authentication.getAuthorities().stream()
                .anyMatch(a -> "novel:edit:any".equalsIgnoreCase(a.getAuthority()));

        return isOwner || canEditAny;
    }
}
