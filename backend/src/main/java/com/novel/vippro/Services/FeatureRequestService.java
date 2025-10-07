package com.novel.vippro.Services;

import com.novel.vippro.DTO.FeatureRequest.CreateFeatureRequestDTO;
import com.novel.vippro.DTO.FeatureRequest.FeatureRequestDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.FeatureRequest;
import com.novel.vippro.Models.User;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.FeatureRequestRepository;
import com.novel.vippro.Repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.novel.vippro.Security.UserDetailsImpl;

@Service
public class FeatureRequestService {

        @Autowired
        private FeatureRequestRepository featureRequestRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private Mapper mapper;

        @Transactional
        public FeatureRequestDTO createFeatureRequest(CreateFeatureRequestDTO requestDTO) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new AccessDeniedException("User must be authenticated to create feature request");
            }
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetailsImpl currentUser)) {
                throw new AccessDeniedException("Authenticated user not found");
            }
            UUID requesterId = currentUser.getId();
            User user = userRepository.findById(requesterId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", requesterId));
            FeatureRequest featureRequest = mapper.CreateFeatureRequestDTOtoFeatureRequest(requestDTO);
            featureRequest.setRequester(user);
            featureRequest.setStatus(FeatureRequest.FeatureRequestStatus.VOTING);
            featureRequest.setVoteCount(0);
            FeatureRequest savedRequest = featureRequestRepository.save(featureRequest);
            return mapper.RequesttoRequestDTO(savedRequest);
        }

        @Cacheable(value = "featureRequests", key = "#id")
        public FeatureRequestDTO getFeatureRequest(UUID id, UUID userId) {
                FeatureRequest featureRequest = featureRequestRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Feature request not found"));

                boolean hasVoted = featureRequestRepository.hasUserVoted(id,
                                userRepository.findById(userId).orElseThrow());
                
                return mapper.RequesttoRequestDTO(featureRequest);
        }

        @Cacheable(value = "featureRequests")
        public PageResponse<FeatureRequestDTO> getAllFeatureRequests(Pageable pageable) {
                Page<FeatureRequest> page = featureRequestRepository.findAll(pageable);
                return new PageResponse<>(page.map(fr -> mapper.RequesttoRequestDTO(fr)));
        }

        @Cacheable(value = "featureRequests", key = "#status")
        public PageResponse<FeatureRequestDTO> getFeatureRequestsByStatus(
                        FeatureRequest.FeatureRequestStatus status, Pageable pageable) {
                Page<FeatureRequestDTO> page = featureRequestRepository.findByStatus(status, pageable);
                return new PageResponse<>(page);
        }

        @Transactional
        @CacheEvict(value = "featureRequests", allEntries = true)
        public FeatureRequestDTO voteForFeatureRequest(UUID id, UUID userId) {
                FeatureRequest featureRequest = featureRequestRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Feature request not found"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                if (featureRequest.getCreatedBy().equals(userId)) {
                        throw new AccessDeniedException("Cannot vote for your own feature request");
                }

                if (featureRequestRepository.hasUserVoted(id, user)) {
                        throw new IllegalStateException("User has already voted for this feature request");
                }

                featureRequest.getVoters().add(user);
                featureRequest.setVoteCount(featureRequest.getVoteCount() + 1);
                FeatureRequest savedRequest = featureRequestRepository.save(featureRequest);

                return mapper.RequesttoRequestDTO(savedRequest);
        }

        @Transactional
        @CacheEvict(value = "featureRequests", allEntries = true)
        public FeatureRequestDTO updateFeatureRequestStatus(
                        UUID id, FeatureRequest.FeatureRequestStatus newStatus, UUID userId) {
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

                return mapper.RequesttoRequestDTO(savedRequest);
        }

        @Transactional
        @CacheEvict(value = "featureRequests", allEntries = true)
        public void deleteFeatureRequest(UUID id, UUID userId) {
                FeatureRequest featureRequest = featureRequestRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Feature request not found"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                // Only allow deletion by creator or admin
                if (!featureRequest.getCreatedBy().equals(userId) &&
                                !user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
                        throw new AccessDeniedException("Only creator or admin can delete feature request");
                }

                featureRequestRepository.delete(featureRequest);
        }
}
