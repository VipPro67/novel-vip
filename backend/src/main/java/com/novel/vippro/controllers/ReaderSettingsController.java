package com.novel.vippro.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.novel.vippro.dto.ReaderSettingsDTO;
import com.novel.vippro.dto.ReaderSettingsUpdateDTO;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.services.ReaderSettingsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

// ...existing imports...

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/reader-settings")
@Tag(name = "Reader Settings", description = "User reading preferences management APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ReaderSettingsController {

        private final ReaderSettingsService readerSettingsService;

        @Operation(summary = "Get user settings", description = "Get reading preferences for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Settings retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping
        public ResponseEntity<ControllerResponse<ReaderSettingsDTO>> getUserSettings() {
                ReaderSettingsDTO settings = readerSettingsService.getUserSettings();
                return ResponseEntity.ok(ControllerResponse.success("Settings retrieved successfully", settings));
        }

        @Operation(summary = "Update settings", description = "Update reading preferences for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Settings updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid settings data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @PutMapping
        public ResponseEntity<ControllerResponse<ReaderSettingsDTO>> updateSettings(
                        @Parameter(description = "Updated settings", required = true) @Valid @RequestBody ReaderSettingsUpdateDTO settingsDTO) {
                ReaderSettingsDTO settings = readerSettingsService.updateSettings(settingsDTO);
                return ResponseEntity.ok(ControllerResponse.success("Settings updated successfully", settings));
        }

        @Operation(summary = "Reset settings", description = "Reset reading preferences to default values")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Settings reset successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @PostMapping("/reset")
        public ResponseEntity<ControllerResponse<ReaderSettingsDTO>> resetSettings() {
                ReaderSettingsDTO settings = readerSettingsService.resetSettings();
                return ResponseEntity.ok(ControllerResponse.success("Settings reset successfully", settings));
        }

        @Operation(summary = "Get font options", description = "Get available font options for reader")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Font options retrieved successfully")
        })
        @GetMapping("/fonts")
        public ResponseEntity<ControllerResponse<List<String>>> getFontOptions() {
                List<String> fonts = readerSettingsService.getFontOptions();
                return ResponseEntity.ok(ControllerResponse.success("Font options retrieved successfully", fonts));
        }

        @Operation(summary = "Get theme options", description = "Get available theme options for reader")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Theme options retrieved successfully")
        })
        @GetMapping("/themes")
        public ResponseEntity<ControllerResponse<List<String>>> getThemeOptions() {
                List<String> themes = readerSettingsService.getThemeOptions();
                return ResponseEntity.ok(ControllerResponse.success("Theme options retrieved successfully", themes));
        }

        @Operation(summary = "Apply theme", description = "Apply a specific theme to the reader")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Theme applied successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid theme"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @PutMapping("/theme/{themeId}")
        public ResponseEntity<ControllerResponse<ReaderSettingsDTO>> applyTheme(
                        @Parameter(description = "Theme ID", required = true) @PathVariable String themeId) {
                ReaderSettingsDTO settings = readerSettingsService.applyTheme(themeId);
                return ResponseEntity.ok(ControllerResponse.success("Theme applied successfully", settings));
        }
}