package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.Gamification.GamificationProfileDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.Services.GamificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/gamification")
@Tag(name = "Gamification", description = "Rank and Achievement System")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user gamification profile")
    public ControllerResponse<GamificationProfileDTO> getProfile() {
        UUID userId = UserDetailsImpl.getCurrentUserId();
        return ControllerResponse.success("Fetched profile", gamificationService.getUserGamificationProfile(userId));
    }

    @PostMapping("/test/add-points")
    @PreAuthorize("hasRole('ADMIN')") 
    @Operation(summary = "Admin: Add points for testing")
    public ControllerResponse<Void> addPoints(@RequestParam int points, @RequestParam(required = false) UUID userId) {
        if (userId == null) {
            userId = UserDetailsImpl.getCurrentUserId();
        }
        gamificationService.addPoints(userId, points);
        return ControllerResponse.success("Points added", null);
    }
}
