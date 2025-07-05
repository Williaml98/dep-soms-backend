package com.dep.soms.service;

import com.dep.soms.dto.client.*;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.*;
import com.dep.soms.repository.ClientRequestRepository;
import com.dep.soms.repository.ClientRepository;
import com.dep.soms.repository.SiteRepository;
import com.dep.soms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientRequestService {

    private final ClientRequestRepository clientRequestRepository;
    private final ClientRepository clientRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ClientRequestDTO> getAllRequests() {
        return clientRequestRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientRequestDTO> getRequestsByClient(Long clientId) {
        return clientRequestRepository.findByClientId(clientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientRequestDTO getRequestById(Long id) {
        return clientRequestRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
    }

    @Transactional
    public ClientRequestDTO createRequest(CreateClientRequestDTO requestDTO, Long requestedById) {
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + requestDTO.getClientId()));

        User requestedBy = userRepository.findById(requestedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestedById));

        Site site = null;
        if (requestDTO.getSiteId() != null) {
            site = siteRepository.findById(requestDTO.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + requestDTO.getSiteId()));
        }

        ClientRequest request = ClientRequest.builder()
                .client(client)
                .site(site)
                .requestedBy(requestedBy)
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .priority(requestDTO.getPriority())
                .status(ClientRequest.RequestStatus.OPEN)
                .comments(new HashSet<>())
                .build();

        ClientRequest savedRequest = clientRequestRepository.save(request);
        return convertToDTO(savedRequest);
    }

    @Transactional
    public ClientRequestDTO updateRequest(Long id, UpdateClientRequestDTO requestDTO) {
        ClientRequest request = clientRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        if (requestDTO.getStatus() != null) {
            request.setStatus(requestDTO.getStatus());
            if (requestDTO.getStatus() == ClientRequest.RequestStatus.RESOLVED) {
                request.setResolvedAt(LocalDateTime.now());
            }
        }

        if (requestDTO.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(requestDTO.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestDTO.getAssignedToId()));
            request.setAssignedTo(assignedTo);
        }

        if (requestDTO.getResolutionNotes() != null) {
            request.setResolutionNotes(requestDTO.getResolutionNotes());
        }

        ClientRequest updatedRequest = clientRequestRepository.save(request);
        return convertToDTO(updatedRequest);
    }

    @Transactional
    public ClientRequestCommentDTO addComment(AddCommentDTO commentDTO, Long userId) {
        ClientRequest request = clientRequestRepository.findById(commentDTO.getClientRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + commentDTO.getClientRequestId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ClientRequestComment comment = ClientRequestComment.builder()
                .clientRequest(request)
                .user(user)
                .comment(commentDTO.getComment())
                .build();

        request.getComments().add(comment);
        clientRequestRepository.save(request);

        return convertCommentToDTO(comment);
    }

    @Transactional(readOnly = true)
    public List<ClientRequestCommentDTO> getCommentsByRequestId(Long requestId) {
        ClientRequest request = clientRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        // If the request has comments, convert them to DTOs
        if (request.getComments() != null && !request.getComments().isEmpty()) {
            return request.getComments().stream()
                    .map(this::convertCommentToDTO)
                    .collect(Collectors.toList());
        }

        // Return empty list if no comments
        return List.of();
    }


    private ClientRequestDTO convertToDTO(ClientRequest request) {
        List<ClientRequestCommentDTO> commentDTOs = request.getComments() != null
                ? request.getComments().stream()
                .map(this::convertCommentToDTO)
                .collect(Collectors.toList())
                : List.of();  // Empty list if comments is null
        return ClientRequestDTO.builder()
                .id(request.getId())
                .clientId(request.getClient().getId())
                .clientName(request.getClient().getName())
                .siteId(request.getSite() != null ? request.getSite().getId() : null)
                .siteName(request.getSite() != null ? request.getSite().getName() : null)
                .requestedById(request.getRequestedBy().getId())
                .requestedByName(request.getRequestedBy().getFirstName() + " " + request.getRequestedBy().getLastName())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus())
                .assignedToId(request.getAssignedTo() != null ? request.getAssignedTo().getId() : null)
                .assignedToName(request.getAssignedTo() != null ?
                        request.getAssignedTo().getFirstName() + " " + request.getAssignedTo().getLastName() : null)
                .resolutionNotes(request.getResolutionNotes())
                .resolvedAt(request.getResolvedAt())
                .comments(commentDTOs)
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    private ClientRequestCommentDTO convertCommentToDTO(ClientRequestComment comment) {
        return ClientRequestCommentDTO.builder()
                .id(comment.getId())
                .clientRequestId(comment.getClientRequest().getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName())
                .userProfilePicture(comment.getUser().getProfilePicture())
                .comment(comment.getComment())
                .createdAt(comment.getCreatedAt())
                .build();
    }



}