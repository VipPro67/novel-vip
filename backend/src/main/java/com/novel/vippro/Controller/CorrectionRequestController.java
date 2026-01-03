package com.novel.vippro.Controller;

import com.novel.vippro.DTO.CorrectionRequest.CreateCorrectionRequestDTO;
import com.novel.vippro.DTO.CorrectionRequest.CorrectionRequestDTO;
import com.novel.vippro.Models.CorrectionRequest;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.CorrectionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController("correctionRequestControllerV1")
@RequestMapping("/api/v1/correction-requests")
public class CorrectionRequestController {

    @Autowired
    private CorrectionRequestService correctionRequestService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CorrectionRequestDTO> submitCorrectionRequest(@RequestBody CreateCorrectionRequestDTO dto) {
        return ResponseEntity.ok(correctionRequestService.submitCorrectionRequest(dto));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<CorrectionRequestDTO>> getPendingCorrections(Pageable pageable) {
        return ResponseEntity.ok(correctionRequestService.getPendingCorrections(pageable));
    }

    @GetMapping("/pending/novel/{novelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CorrectionRequestDTO>> getPendingByNovelId(@PathVariable UUID novelId) {
        return ResponseEntity.ok(correctionRequestService.getPendingByNovelId(novelId));
    }

    @GetMapping("/pending/chapter/{chapterId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CorrectionRequestDTO>> getPendingByChapterId(@PathVariable UUID chapterId) {
        return ResponseEntity.ok(correctionRequestService.getPendingByChapterId(chapterId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<CorrectionRequestDTO>> getCorrectionsByStatus(@RequestParam CorrectionRequest.CorrectionStatus status, Pageable pageable) {
        return ResponseEntity.ok(correctionRequestService.getCorrectionsByStatus(status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CorrectionRequestDTO> getCorrectionById(@PathVariable UUID id) {
        return ResponseEntity.ok(correctionRequestService.getCorrectionById(id));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CorrectionRequestDTO> approveCorrectionRequest(@PathVariable UUID id) throws IOException {
        return ResponseEntity.ok(correctionRequestService.approveCorrectionRequest(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CorrectionRequestDTO> rejectCorrectionRequest(@PathVariable UUID id, @RequestBody String rejectionReason) {
        return ResponseEntity.ok(correctionRequestService.rejectCorrectionRequest(id, rejectionReason));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<CorrectionRequestDTO>> getUserCorrections(@PathVariable UUID userId, Pageable pageable) {
        return ResponseEntity.ok(correctionRequestService.getUserCorrections(userId, pageable));
    }
}
