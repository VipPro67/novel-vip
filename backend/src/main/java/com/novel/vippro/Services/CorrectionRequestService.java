package com.novel.vippro.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Models.CorrectionRequest;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Models.Novel;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.CorrectionRequestRepository;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.UserRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CorrectionRequestService {

    private static final Logger logger = LogManager.getLogger(CorrectionRequestService.class);

    @Autowired
    private CorrectionRequestRepository correctionRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Submit a new text correction request
     */
    @Transactional
    public CorrectionRequest submitCorrectionRequest(
            UUID userId,
            UUID novelId,
            UUID chapterId,
            String s3Key,
            Integer paragraphIndex,
            String originalText,
            String suggestedText) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Novel not found with id: " + novelId));

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        CorrectionRequest request = new CorrectionRequest();
        request.setUser(user);
        request.setNovel(novel);
        request.setChapter(chapter);
        request.setS3Key(s3Key);
        request.setParagraphIndex(paragraphIndex);
        request.setOriginalText(originalText);
        request.setSuggestedText(suggestedText);
        request.setStatus(CorrectionRequest.CorrectionStatus.PENDING);

        CorrectionRequest saved = correctionRequestRepository.save(request);
        logger.info("New correction request submitted. ID: {}, Novel: {}, Chapter: {}", saved.getId(), novelId,
                chapterId);

        return saved;
    }

    /**
     * Get paginated pending correction requests
     */
    public PageResponse<CorrectionRequest> getPendingCorrections(Pageable pageable) {
        Page<CorrectionRequest> page = correctionRequestRepository.findPendingCorrections(pageable);
        return new PageResponse<>(page);
    }

    /**
     * Get all pending corrections for a specific novel
     */
    public List<CorrectionRequest> getPendingByNovelId(UUID novelId) {
        return correctionRequestRepository.findPendingByNovelId(novelId);
    }

    /**
     * Get all pending corrections for a specific chapter
     */
    public List<CorrectionRequest> getPendingByChapterId(UUID chapterId) {
        return correctionRequestRepository.findPendingByChapterId(chapterId);
    }

    /**
     * Get corrections by status
     */
    public PageResponse<CorrectionRequest> getCorrectionsByStatus(
            CorrectionRequest.CorrectionStatus status,
            Pageable pageable) {
        Page<CorrectionRequest> page = correctionRequestRepository.findByStatus(status, pageable);
        return new PageResponse<>(page);
    }

    /**
     * Get a single correction request by ID
     */
    public CorrectionRequest getCorrectionById(UUID id) {
        return correctionRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Correction request not found with id: " + id));
    }

    /**
     * Approve a correction request and patch the S3 file
     * This is the critical operation that patches the JSON file in S3
     */
    @Transactional
    public CorrectionRequest approveCorrectionRequest(UUID correctionId) throws IOException {
        CorrectionRequest correction = getCorrectionById(correctionId);

        if (!correction.getStatus().equals(CorrectionRequest.CorrectionStatus.PENDING)) {
            throw new IllegalStateException(
                    "Only PENDING corrections can be approved. Current status: " + correction.getStatus());
        }

        try {
            // Download the JSON file from S3
            byte[] fileBytes = fileStorageService.downloadFile(correction.getS3Key());
            String jsonContent = new String(fileBytes, "UTF-8");

            // Parse JSON content
            List<String> paragraphs = objectMapper.readValue(jsonContent, new TypeReference<List<String>>() {
            });

            // Locate and patch the specific paragraph
            if (correction.getParagraphIndex() != null && correction.getParagraphIndex() < paragraphs.size()) {
                String originalParagraph = paragraphs.get(correction.getParagraphIndex());

                // Replace the original text with suggested text in the paragraph
                String patchedParagraph = originalParagraph.replace(correction.getOriginalText(),
                        correction.getSuggestedText());
                paragraphs.set(correction.getParagraphIndex(), patchedParagraph);
            } else if (correction.getParagraphIndex() == null) {
                // If no specific paragraph index, search for the text in all paragraphs and
                // replace first occurrence
                for (int i = 0; i < paragraphs.size(); i++) {
                    if (paragraphs.get(i).contains(correction.getOriginalText())) {
                        String patchedParagraph = paragraphs.get(i)
                                .replace(correction.getOriginalText(), correction.getSuggestedText());
                        paragraphs.set(i, patchedParagraph);
                        correction.setParagraphIndex(i);
                        break;
                    }
                }
            }

            // Serialize the patched content back to JSON
            String patchedJsonContent = objectMapper.writeValueAsString(paragraphs);

            // Upload the patched file back to S3 (overwrite)
            fileStorageService.uploadFile(patchedJsonContent.getBytes("UTF-8"), correction.getS3Key(),
                    "application/json");

            // Update correction status to APPROVED
            correction.setStatus(CorrectionRequest.CorrectionStatus.APPROVED);
            CorrectionRequest updated = correctionRequestRepository.save(correction);

            logger.info("Correction request approved and S3 file patched. Correction ID: {}, S3 Key: {}", correctionId,
                    correction.getS3Key());

            return updated;

        } catch (IOException e) {
            logger.error("Failed to approve correction request. ID: {}", correctionId, e);
            throw new IOException("Failed to process S3 file: " + e.getMessage(), e);
        }
    }

    /**
     * Reject a correction request
     */
    @Transactional
    public CorrectionRequest rejectCorrectionRequest(UUID correctionId, String rejectionReason) {
        CorrectionRequest correction = getCorrectionById(correctionId);

        if (!correction.getStatus().equals(CorrectionRequest.CorrectionStatus.PENDING)) {
            throw new IllegalStateException(
                    "Only PENDING corrections can be rejected. Current status: " + correction.getStatus());
        }

        correction.setStatus(CorrectionRequest.CorrectionStatus.REJECTED);
        correction.setRejectionReason(rejectionReason);
        CorrectionRequest updated = correctionRequestRepository.save(correction);

        logger.info("Correction request rejected. ID: {}, Reason: {}", correctionId, rejectionReason);

        return updated;
    }

    /**
     * Get user's correction requests
     */
    public PageResponse<CorrectionRequest> getUserCorrections(UUID userId, Pageable pageable) {
        Page<CorrectionRequest> page = correctionRequestRepository.findByUserId(userId, pageable);
        return new PageResponse<>(page);
    }
}
