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
import com.novel.vippro.Models.ERole;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.CorrectionRequestRepository;
import com.novel.vippro.Repository.ChapterRepository;
import com.novel.vippro.Repository.NovelRepository;
import com.novel.vippro.Repository.UserRepository;
import com.novel.vippro.Security.UserDetailsImpl;
import com.novel.vippro.DTO.CorrectionRequest.CorrectionRequestDTO;
import com.novel.vippro.DTO.CorrectionRequest.CorrectionRequestWithDetailsDTO;
import com.novel.vippro.Mapper.CorrectionRequestMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
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

    @Autowired
    private CorrectionRequestMapper correctionRequestMapper;
    
    @Autowired
    private CacheManager cacheManager;

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

        // Handle multiple paragraph indices
        if (dto.paragraphIndices() != null && !dto.paragraphIndices().isEmpty()) {
            String indicesStr = dto.paragraphIndices().stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(","));
            request.setParagraphIndices(indicesStr);
        }

        try {
            String s3Key = chapter.getJsonFile().getPublicId();
            byte[] fileBytes = fileStorageService.downloadFile(s3Key);
            String jsonContent = new String(fileBytes, StandardCharsets.UTF_8);

            Map<String, Object> jsonData = objectMapper.readValue(jsonContent,
                    new TypeReference<Map<String, Object>>() {
                    });
            String htmlContent = (String) jsonData.get("content");

            if (htmlContent != null) {
                Document doc = Jsoup.parseBodyFragment(htmlContent);

                Elements paragraphs = doc.select("p");

                Integer targetParagraphIndex = dto.paragraphIndex();
                String searchKey = dto.originalText() != null ? dto.originalText().trim() : "";

                if (targetParagraphIndex == null && !searchKey.isEmpty()) {
                    for (int i = 0; i < paragraphs.size(); i++) {
                        Element p = paragraphs.get(i);

                        if (p.text().contains(searchKey)) {
                            targetParagraphIndex = i;
                            request.setParagraphIndex(i);
                            break;
                        }
                    }
                }

                if (targetParagraphIndex != null) {
                    if (targetParagraphIndex > 0) {
                        request.setPreviousParagraph(paragraphs.get(targetParagraphIndex - 1).outerHtml());
                    }
                    request.setParagraphText(paragraphs.get(targetParagraphIndex).outerHtml());
                    if (targetParagraphIndex < paragraphs.size() - 1) {
                        request.setNextParagraph(paragraphs.get(targetParagraphIndex + 1).outerHtml());
                    }

                    logger.info("HTML Context found. Index: {}", targetParagraphIndex);
                } else {
                    logger.warn("Could not find paragraph containing text: '{}'", searchKey);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing HTML content", e);
        }
        
        CorrectionRequest saved = correctionRequestRepository.save(request);
        logger.info("New correction request submitted. ID: {}, Novel: {}, Chapter: {}", saved.getId(), dto.novelId(),
                dto.chapterId());

        // Check if user has EDITOR role and auto-approve
        boolean isEditor = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.EDITOR);
        
        if (isEditor) {
            logger.info("User has EDITOR role. Auto-approving correction request. ID: {}", saved.getId());
            try {
                return correctionRequestMapper.toDto(
                    autoApproveCorrectionRequest(saved)
                );
            } catch (IOException e) {
                logger.error("Failed to auto-approve correction request. ID: {}", saved.getId(), e);
                // Return as PENDING if auto-approval fails
                return correctionRequestMapper.toDto(saved);
            }
        }

        return correctionRequestMapper.toDto(saved);
    }

    /**
     * Get paginated pending correction requests
     */
    @Transactional(readOnly = true)
    public PageResponse<CorrectionRequestWithDetailsDTO> getPendingCorrections(Pageable pageable) {
        Page<CorrectionRequest> page = correctionRequestRepository.findPendingCorrections(pageable);
        return new PageResponse<>(page.map(correctionRequestMapper::toDetailsDto));
    }

    public List<CorrectionRequestDTO> getPendingByNovelId(UUID novelId) {
        return correctionRequestRepository.findPendingByNovelId(novelId).stream().map(correctionRequestMapper::toDto)
                .toList();
    }

    public List<CorrectionRequestDTO> getPendingByChapterId(UUID chapterId) {
        return correctionRequestRepository.findPendingByChapterId(chapterId).stream()
                .map(correctionRequestMapper::toDto).toList();
    }

    /**
     * Get corrections by status
     */
    public PageResponse<CorrectionRequestWithDetailsDTO> getCorrectionsByStatus(
            CorrectionRequest.CorrectionStatus status,
            Pageable pageable) {
        Page<CorrectionRequest> page = correctionRequestRepository.findByStatus(status, pageable);
        Page<CorrectionRequestWithDetailsDTO> dtoPage = page.map(correctionRequestMapper::toDetailsDto);
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
    public CorrectionRequestWithDetailsDTO approveCorrectionRequest(UUID correctionId) throws IOException {
        CorrectionRequest correction = correctionRequestRepository.findById(correctionId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Correction request not found with id: " + correctionId));

        if (!correction.getStatus().equals(CorrectionRequest.CorrectionStatus.PENDING)) {
            throw new IllegalStateException(
                    "Only PENDING corrections can be approved. Current status: " + correction.getStatus());
        }

        CorrectionRequest updated = performCorrection(correction);
        return correctionRequestMapper.toDetailsDto(updated);
    }

    /**
     * Auto-approve correction request for EDITOR role users
     * Internal method that bypasses status check
     */
    private CorrectionRequest autoApproveCorrectionRequest(CorrectionRequest correction) throws IOException {
        return performCorrection(correction);
    }

    /**
     * Perform the actual correction on the S3 file
     * Supports both single and multiple paragraph corrections
     */
    private CorrectionRequest performCorrection(CorrectionRequest correction) throws IOException {
        try {
            String s3Key = correction.getChapter().getJsonFile().getPublicId();
            // Download the JSON file from S3
            byte[] fileBytes = fileStorageService.downloadFile(s3Key);
            String jsonContent = new String(fileBytes, "UTF-8");

            // Parse JSON content
            Map<String, Object> jsonData = objectMapper.readValue(jsonContent,
                    new TypeReference<Map<String, Object>>() {
                    });
            String htmlContent = (String) jsonData.get("content");

            if (htmlContent == null) {
                throw new IOException("Invalid JSON structure: missing 'content' field");
            }

            // Parse HTML content
            Document doc = Jsoup.parseBodyFragment(htmlContent);
            Elements paragraphs = doc.select("p");

            boolean replaced = false;

            // Handle multiple paragraph indices if available
            if (correction.getParagraphIndices() != null && !correction.getParagraphIndices().isEmpty()) {
                List<Integer> indices = Arrays.stream(correction.getParagraphIndices().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .sorted()
                        .collect(java.util.stream.Collectors.toList());
                
                // Determine if this is a deletion (blank suggested text)
                String suggestedText = correction.getSuggestedText();
                boolean isDeletion = suggestedText == null || suggestedText.trim().isEmpty();
                
                // Split originalText by common separators that might appear between paragraphs
                String[] originalTextParts = correction.getOriginalText().split("\\n\\n|\\n");
                
                // If we have multiple parts matching multiple indices, process each separately
                if (originalTextParts.length == indices.size()) {
                    // Match each part to its corresponding paragraph
                    for (int i = 0; i < indices.size(); i++) {
                        int idx = indices.get(i);
                        if (idx >= 0 && idx < paragraphs.size()) {
                            Element paragraph = paragraphs.get(idx);
                            String paragraphText = paragraph.text();
                            String paragraphHtml = paragraph.html();
                            String textToReplace = originalTextParts[i].trim();
                            
                            if (!textToReplace.isEmpty() && paragraphText.contains(textToReplace)) {
                                String updatedHtml;
                                if (isDeletion) {
                                    updatedHtml = paragraphHtml.replaceFirst(
                                            java.util.regex.Pattern.quote(textToReplace),
                                            "");
                                } else {
                                    updatedHtml = paragraphHtml.replaceFirst(
                                            java.util.regex.Pattern.quote(textToReplace),
                                            java.util.regex.Matcher.quoteReplacement(suggestedText));
                                }
                                paragraph.html(updatedHtml);
                                replaced = true;
                            }
                        }
                    }
                } else {
                    // Fallback: try to find the whole text in each selected paragraph
                    String originalText = correction.getOriginalText().replaceAll("\\n+", " ").trim();
                    for (Integer idx : indices) {
                        if (idx >= 0 && idx < paragraphs.size()) {
                            Element paragraph = paragraphs.get(idx);
                            String paragraphText = paragraph.text();
                            String paragraphHtml = paragraph.html();
                            
                            if (paragraphText.contains(originalText)) {
                                String updatedHtml;
                                if (isDeletion) {
                                    updatedHtml = paragraphHtml.replaceFirst(
                                            java.util.regex.Pattern.quote(originalText),
                                            "");
                                } else {
                                    updatedHtml = paragraphHtml.replaceFirst(
                                            java.util.regex.Pattern.quote(originalText),
                                            java.util.regex.Matcher.quoteReplacement(suggestedText));
                                }
                                paragraph.html(updatedHtml);
                                replaced = true;
                                break;
                            }
                        }
                    }
                }
            } 
            // Single paragraph correction
            else if (correction.getParagraphIndex() != null && correction.getParagraphIndex() < paragraphs.size()) {
                int pIdx = correction.getParagraphIndex();
                Element paragraph = paragraphs.get(pIdx);
                String paragraphHtml = paragraph.outerHtml();
                
                // Determine if this is a deletion (blank suggested text)
                String suggestedText = correction.getSuggestedText();
                boolean isDeletion = suggestedText == null || suggestedText.trim().isEmpty();
                String replacementText = isDeletion ? "" : suggestedText;

                // Try charIndex-based replacement first
                if (correction.getCharIndex() != null && correction.getCharIndex() >= 0) {
                    String textContent = paragraph.text();
                    int cIdx = correction.getCharIndex();
                    String originalText = correction.getOriginalText();
                    
                    if (cIdx + originalText.length() <= textContent.length() &&
                            textContent.substring(cIdx, cIdx + originalText.length()).equals(originalText)) {
                        // Replace or delete at exact char position
                        String newText = textContent.substring(0, cIdx)
                                + replacementText
                                + textContent.substring(cIdx + originalText.length());
                        paragraph.text(newText);
                        replaced = true;
                    }
                }
                
                // Fallback to first occurrence in paragraph
                if (!replaced) {
                    String paragraphTextContent = paragraph.text();
                    if (paragraphTextContent.contains(correction.getOriginalText())) {
                        String updatedHtml;
                        if (isDeletion) {
                            // Delete: replace with empty string
                            updatedHtml = paragraphHtml.replaceFirst(
                                    java.util.regex.Pattern.quote(correction.getOriginalText()),
                                    "");
                        } else {
                            // Replace with suggested text
                            updatedHtml = paragraphHtml.replaceFirst(
                                    java.util.regex.Pattern.quote(correction.getOriginalText()),
                                    java.util.regex.Matcher.quoteReplacement(suggestedText));
                        }
                        paragraph.html(updatedHtml);
                        replaced = true;
                    }
                }
            }
            
            // Search all paragraphs if not replaced yet
            if (!replaced) {
                // Determine if this is a deletion (blank suggested text)
                String suggestedText = correction.getSuggestedText();
                boolean isDeletion = suggestedText == null || suggestedText.trim().isEmpty();
                
                for (int i = 0; i < paragraphs.size(); i++) {
                    Element paragraph = paragraphs.get(i);
                    String paragraphText = paragraph.text();
                    String paragraphHtml = paragraph.html();
                    
                    if (paragraphText.contains(correction.getOriginalText())) {
                        String updatedHtml;
                        if (isDeletion) {
                            // Delete: replace with empty string
                            updatedHtml = paragraphHtml.replaceFirst(
                                    java.util.regex.Pattern.quote(correction.getOriginalText()),
                                    "");
                        } else {
                            // Replace with suggested text
                            updatedHtml = paragraphHtml.replaceFirst(
                                    java.util.regex.Pattern.quote(correction.getOriginalText()),
                                    java.util.regex.Matcher.quoteReplacement(suggestedText));
                        }
                        paragraph.html(updatedHtml);
                        correction.setParagraphIndex(i);
                        replaced = true;
                        break;
                    }
                }
            }

            if (!replaced) {
                throw new IOException("Could not find the text to replace in the chapter content");
            }

            // Get updated HTML content
            String patchedHtmlContent = doc.body().html();

            // Create the JSON structure to upload back
            Map<String, Object> patchedJsonData = new HashMap<>();
            patchedJsonData.put("content", patchedHtmlContent);
            String patchedJsonContent = objectMapper.writeValueAsString(patchedJsonData);

            // Upload the patched file back to S3 (overwrite)
            fileStorageService.uploadFile(patchedJsonContent.getBytes("UTF-8"), s3Key, "application/json");

            // Update correction status to APPROVED
            correction.setStatus(CorrectionRequest.CorrectionStatus.APPROVED);
            CorrectionRequest updated = correctionRequestRepository.save(correction);

            logger.info("Correction performed and S3 file patched. Correction ID: {}, S3 Key: {}", 
                    correction.getId(), s3Key);
            
            // Evict cache
            if (cacheManager.getCache("chapters") != null) {
                cacheManager.getCache("chapters").evict(correction.getChapter().getId());
            }

            return updated;

        } catch (IOException e) {
            logger.error("Failed to perform correction. ID: {}", correction.getId(), e);
            throw new IOException("Failed to process S3 file: " + e.getMessage(), e);
        }
    }

    /**
     * Reject a correction request
     */
    @Transactional
    public CorrectionRequestWithDetailsDTO rejectCorrectionRequest(UUID correctionId, String rejectionReason) {
        CorrectionRequest correction = correctionRequestRepository.findById(correctionId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Correction request not found with id: " + correctionId));

        if (!correction.getStatus().equals(CorrectionRequest.CorrectionStatus.PENDING)) {
            throw new IllegalStateException(
                    "Only PENDING corrections can be rejected. Current status: " + correction.getStatus());
        }

        correction.setStatus(CorrectionRequest.CorrectionStatus.REJECTED);
        correction.setRejectionReason(rejectionReason);
        CorrectionRequest updated = correctionRequestRepository.save(correction);

        logger.info("Correction request rejected. ID: {}, Reason: {}", correctionId, rejectionReason);

        return correctionRequestMapper.toDetailsDto(updated);
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
