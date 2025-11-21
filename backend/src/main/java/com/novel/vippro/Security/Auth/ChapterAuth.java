package com.novel.vippro.Security.Auth;

import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.ChapterUnlock;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.ChapterUnlockRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChapterAuth {

    private final ChapterRepository chapterRepository;
    private final ChapterUnlockRepository chapterUnlockRepository;
    private final UserRepository userRepository;

    public boolean canRead(UUID chapterId) {
        Optional<Chapter> chapterOpt = chapterRepository.findById(chapterId);
        if (chapterOpt.isEmpty()) {
            return false;
        }
        Chapter chapter = chapterOpt.get();
        if (Boolean.FALSE.equals(chapter.getLocked())) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UUID userId = UserDetailsImpl.getCurrentUserId();
        if (userId == null) {
            return false;
        }

        if (isAuthor(userId, chapter.getNovel())) {
            return true;
        }

        boolean canReadAny = authentication.getAuthorities().stream()
                .anyMatch(a -> "chapter:read:any".equalsIgnoreCase(a.getAuthority()));
        if (canReadAny) {
            return true;
        }

        return userRepository.findById(userId)
                .map(user -> chapterUnlockRepository.existsByUserAndChapter(user, chapter))
                .orElse(false);
    }

    public boolean canUnlock(UUID chapterId) {
        Optional<Chapter> chapterOpt = chapterRepository.findById(chapterId);
        if (chapterOpt.isEmpty()) {
            return false;
        }
        Chapter chapter = chapterOpt.get();
        if (Boolean.FALSE.equals(chapter.getLocked())) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UUID userId = UserDetailsImpl.getCurrentUserId();
        if (userId == null) {
            return false;
        }

        if (isAuthor(userId, chapter.getNovel())) {
            return true;
        }

        boolean hasPermission = authentication.getAuthorities().stream()
                .anyMatch(a -> "chapter:unlock".equalsIgnoreCase(a.getAuthority()));
        if (!hasPermission) {
            return false;
        }

        return userRepository.findById(userId)
                .flatMap(user -> chapterUnlockRepository.findByUserAndChapter(user, chapter))
                .map(ChapterUnlock::getId)
                .isPresent();
    }

    private boolean isAuthor(UUID userId, Novel novel) {
        return novel != null && novel.getOwner() != null && userId.equals(novel.getOwner().getId());
    }
}
