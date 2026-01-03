package com.novel.vippro.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.google.api.services.storage.Storage.Projects.HmacKeys.Create;
import com.novel.vippro.DTO.CorrectionRequest.CreateCorrectionRequestDTO;
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
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.DTO.CorrectionRequest.CorrectionRequestDTO;
import com.novel.vippro.Mapper.CorrectionRequestMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
// import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.Map;
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

    @Autowired
    private CorrectionRequestMapper correctionRequestMapper;

    /**
     * Submit a new text correction request
     */
    @Transactional
    public CorrectionRequestDTO submitCorrectionRequest(
            CreateCorrectionRequestDTO dto) {
        UUID userId = UserDetailsImpl.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Novel novel = novelRepository.findById(dto.novelId())
                .orElseThrow(() -> new ResourceNotFoundException("Novel not found with id: " + dto.novelId()));

        Chapter chapter = chapterRepository.findById(dto.chapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + dto.chapterId()));

        CorrectionRequest request = new CorrectionRequest();
        request.setUser(user);
        request.setNovel(novel);
        request.setChapter(chapter);
        request.setChapterNumber(dto.chapterNumber());
        request.setParagraphIndex(dto.paragraphIndex());
        request.setCharIndex(dto.charIndex());
        request.setOriginalText(dto.originalText());
        request.setSuggestedText(dto.suggestedText());
        request.setReason(dto.reason());
        request.setStatus(CorrectionRequest.CorrectionStatus.PENDING);

        CorrectionRequest saved = correctionRequestRepository.save(request);
        logger.info("New correction request submitted. ID: {}, Novel: {}, Chapter: {}", saved.getId(), dto.novelId(),
            dto.chapterId());

        return correctionRequestMapper.toDto(saved);
    }

    /**
     * Get paginated pending correction requests
     */
    public PageResponse<CorrectionRequestDTO> getPendingCorrections(Pageable pageable) {
        Page<CorrectionRequest> page = correctionRequestRepository.findPendingCorrections(pageable);
        return new PageResponse<>(page.map(correctionRequestMapper::toDto));
    }

    
    public List<CorrectionRequestDTO> getPendingByNovelId(UUID novelId) {
        return correctionRequestRepository.findPendingByNovelId(novelId).stream().map(correctionRequestMapper::toDto).toList();
    }

    
    public List<CorrectionRequestDTO> getPendingByChapterId(UUID chapterId) {
        return correctionRequestRepository.findPendingByChapterId(chapterId).stream().map(correctionRequestMapper::toDto).toList();
    }

    /**
     * Get corrections by status
     */
    public PageResponse<CorrectionRequestDTO> getCorrectionsByStatus(
            CorrectionRequest.CorrectionStatus status,
            Pageable pageable) {
        Page<CorrectionRequest> page = correctionRequestRepository.findByStatus(status, pageable);
        Page<CorrectionRequestDTO> dtoPage = page.map(correctionRequestMapper::toDto);
        return new PageResponse<>(dtoPage);
    }

    /**
     * Get a single correction request by ID
     */
    public CorrectionRequestDTO getCorrectionById(UUID id) {
        return correctionRequestRepository.findById(id).map(correctionRequestMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Correction request not found with id: " + id));
    }

    /**
     * Approve a correction request and patch the S3 file
     * This is the critical operation that patches the JSON file in S3
     */
    @Transactional
    public CorrectionRequestDTO approveCorrectionRequest(UUID correctionId) throws IOException {
        CorrectionRequest correction = correctionRequestRepository.findById(correctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Correction request not found with id: " + correctionId));

        if (!correction.getStatus().equals(CorrectionRequest.CorrectionStatus.PENDING)) {
            throw new IllegalStateException(
                    "Only PENDING corrections can be approved. Current status: " + correction.getStatus());
        }

        try {
            String s3Key = correction.getChapter().getJsonFile().getPublicId();
            // Download the JSON file from S3
            byte[] fileBytes = fileStorageService.downloadFile(s3Key);
            String jsonContent = new String(fileBytes, "UTF-8");

            // Parse JSON content - it should be an object with "content" field
            Map<String, Object> jsonData = objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
            String fullContent = (String) jsonData.get("content");
            
            if (fullContent == null) {
                throw new IOException("Invalid JSON structure: missing 'content' field");
            }

            // Split content into paragraphs (assuming paragraphs are separated by double newlines or similar)
            List<String> paragraphs = new java.util.ArrayList<>(java.util.Arrays.asList(fullContent.split("\n\n")));

            boolean replaced = false;
            // Try to use paragraphIndex and charIndex for precise replacement
            if (correction.getParagraphIndex() != null && correction.getParagraphIndex() < paragraphs.size()) {
                int pIdx = correction.getParagraphIndex();
                String paragraph = paragraphs.get(pIdx);
                if (correction.getCharIndex() != null && correction.getCharIndex() >= 0 && correction.getCharIndex() < paragraph.length()) {
                    int cIdx = correction.getCharIndex();
                    String originalText = correction.getOriginalText();
                    // Check if the substring at charIndex matches originalText
                    if (cIdx + originalText.length() <= paragraph.length() &&
                        paragraph.substring(cIdx, cIdx + originalText.length()).equals(originalText)) {
                        // Replace only at the specified charIndex
                        String patchedParagraph = paragraph.substring(0, cIdx)
                                + correction.getSuggestedText()
                                + paragraph.substring(cIdx + originalText.length());
                        paragraphs.set(pIdx, patchedParagraph);
                        replaced = true;
                    }
                }
                // If not replaced by charIndex, fallback to first occurrence in paragraph
                if (!replaced && paragraph.contains(correction.getOriginalText())) {
                    String patchedParagraph = paragraph.replaceFirst(java.util.regex.Pattern.quote(correction.getOriginalText()),
                            java.util.regex.Matcher.quoteReplacement(correction.getSuggestedText()));
                    paragraphs.set(pIdx, patchedParagraph);
                    replaced = true;
                }
            }
            // If not replaced, search all paragraphs for the first occurrence
            if (!replaced) {
                for (int i = 0; i < paragraphs.size(); i++) {
                    String paragraph = paragraphs.get(i);
                    int idx = paragraph.indexOf(correction.getOriginalText());
                    if (idx != -1) {
                        String patchedParagraph = paragraph.substring(0, idx)
                                + correction.getSuggestedText()
                                + paragraph.substring(idx + correction.getOriginalText().length());
                        paragraphs.set(i, patchedParagraph);
                        correction.setParagraphIndex(i);
                        correction.setCharIndex(idx);
                        replaced = true;
                        break;
                    }
                }
            }

            if (!replaced) {
                throw new IOException("Could not find the text to replace in the chapter content");
            }

            // Join paragraphs back with double newlines
            String patchedContent = String.join("\n\n", paragraphs);

            // Create the JSON structure to upload back
            Map<String, Object> patchedJsonData = new java.util.HashMap<>();
            patchedJsonData.put("content", patchedContent);
            String patchedJsonContent = objectMapper.writeValueAsString(patchedJsonData);

            // Upload the patched file back to S3 (overwrite)
            fileStorageService.uploadFile(patchedJsonContent.getBytes("UTF-8"), s3Key, "application/json");

            // Update correction status to APPROVED
            correction.setStatus(CorrectionRequest.CorrectionStatus.APPROVED);
            CorrectionRequest updated = correctionRequestRepository.save(correction);

            logger.info("Correction request approved and S3 file patched. Correction ID: {}, S3 Key: {}", correctionId, s3Key);

            return correctionRequestMapper.toDto(updated);

        } catch (IOException e) {
            logger.error("Failed to approve correction request. ID: {}", correctionId, e);
            throw new IOException("Failed to process S3 file: " + e.getMessage(), e);
        }
    }

    /**
     * Reject a correction request
     */
    @Transactional
    public CorrectionRequestDTO rejectCorrectionRequest(UUID correctionId, String rejectionReason) {
        CorrectionRequest correction = correctionRequestRepository.findById(correctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Correction request not found with id: " + correctionId));

        if (!correction.getStatus().equals(CorrectionRequest.CorrectionStatus.PENDING)) {
            throw new IllegalStateException(
                    "Only PENDING corrections can be rejected. Current status: " + correction.getStatus());
        }

        correction.setStatus(CorrectionRequest.CorrectionStatus.REJECTED);
        correction.setRejectionReason(rejectionReason);
        CorrectionRequest updated = correctionRequestRepository.save(correction);

        logger.info("Correction request rejected. ID: {}, Reason: {}", correctionId, rejectionReason);

        return correctionRequestMapper.toDto(updated);
    }

    /**
     * Get user's correction requests
     */
    public PageResponse<CorrectionRequestDTO> getUserCorrections(UUID userId, Pageable pageable) {
        Page<CorrectionRequest> page = correctionRequestRepository.findByUserId(userId, pageable);
        Page<CorrectionRequestDTO> dtoPage = page.map(correctionRequestMapper::toDto);
        return new PageResponse<>(dtoPage);
    }
}
