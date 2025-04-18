package com.novel.vippro.services;

import com.novel.vippro.dto.FeatureRequestDTO;
import com.novel.vippro.models.FeatureRequest;
import com.novel.vippro.models.User;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.repository.FeatureRequestRepository;
import com.novel.vippro.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FeatureRequestService {

        @Autowired
        private FeatureRequestRepository featureRequestRepository;

        @Autowired
        private UserRepository userRepository;

        @Transactional
        public FeatureRequestDTO createFeatureRequest(FeatureRequestDTO requestDTO, UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                FeatureRequest featureRequest = new FeatureRequest();
                featureRequest.setTitle(requestDTO.getTitle());
                featureRequest.setDescription(requestDTO.getDescription());
                featureRequest.setCreatedBy(user);
                featureRequest.setStatus(FeatureRequest.FeatureRequestStatus.VOTING);
                featureRequest.setVoteCount(0);

                FeatureRequest savedRequest = featureRequestRepository.save(featureRequest);
                return FeatureRequestDTO.fromEntity(savedRequest, false);
        }

        @Cacheable(value = "featureRequests", key = "#id")
        public FeatureRequestDTO getFeatureRequest(Long id, UUID userId) {
                FeatureRequest featureRequest = featureRequestRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Feature request not found"));

                boolean hasVoted = featureRequestRepository.hasUserVoted(id,
                                userRepository.findById(userId).orElseThrow());

                return FeatureRequestDTO.fromEntity(featureRequest, hasVoted);
        }

        @Cacheable(value = "featureRequests")
        public PageResponse<FeatureRequestDTO> getAllFeatureRequests(Pageable pageable, UUID userId) {
                User user = userRepository.findById(userId).orElseThrow();
                Page<FeatureRequest> page = featureRequestRepository.findAll(pageable);
                return new PageResponse<>(page.map(fr -> FeatureRequestDTO.fromEntity(fr,
                                featureRequestRepository.hasUserVoted(fr.getId(), user))));
        }

        @Cacheable(value = "featureRequests", key = "#status")
        public PageResponse<FeatureRequestDTO> getFeatureRequestsByStatus(
                        FeatureRequest.FeatureRequestStatus status, Pageable pageable, UUID userId) {
                User user = userRepository.findById(userId).orElseThrow();
                Page<FeatureRequest> page = featureRequestRepository.findByStatus(status, pageable);
                return new PageResponse<>(page.map(fr -> FeatureRequestDTO.fromEntity(fr,
                                featureRequestRepository.hasUserVoted(fr.getId(), user))));
        }

        @Transactional
        @CacheEvict(value = "featureRequests", allEntries = true)
        public FeatureRequestDTO voteForFeatureRequest(Long id, UUID userId) {
                FeatureRequest featureRequest = featureRequestRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Feature request not found"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                if (featureRequest.getCreatedBy().getId().equals(userId)) {
                        throw new AccessDeniedException("Cannot vote for your own feature request");
                }

                if (featureRequestRepository.hasUserVoted(id, user)) {
                        throw new IllegalStateException("User has already voted for this feature request");
                }

                featureRequest.getVoters().add(user);
                featureRequest.setVoteCount(featureRequest.getVoteCount() + 1);
                FeatureRequest savedRequest = featureRequestRepository.save(featureRequest);

                return FeatureRequestDTO.fromEntity(savedRequest, true);
        }

        @Transactional
        @CacheEvict(value = "featureRequests", allEntries = true)
        public FeatureRequestDTO updateFeatureRequestStatus(
                        Long id, FeatureRequest.FeatureRequestStatus newStatus, UUID userId) {
                FeatureRequest featureRequest = featureRequestRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Feature request not found"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                // Check if user is admin (you should implement proper role checking)
                if (!user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
                        throw new AccessDeniedException("Only admins can update feature request status");
                }

                featureRequest.setStatus(newStatus);
                FeatureRequest savedRequest = featureRequestRepository.save(featureRequest);

                return FeatureRequestDTO.fromEntity(savedRequest,
                                featureRequestRepository.hasUserVoted(id, user));
        }

        @Transactional
        @CacheEvict(value = "featureRequests", allEntries = true)
        public void deleteFeatureRequest(Long id, UUID userId) {
                FeatureRequest featureRequest = featureRequestRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Feature request not found"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                // Only allow deletion by creator or admin
                if (!featureRequest.getCreatedBy().getId().equals(userId) &&
                                !user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
                        throw new AccessDeniedException("Only creator or admin can delete feature request");
                }

                featureRequestRepository.delete(featureRequest);
        }
}