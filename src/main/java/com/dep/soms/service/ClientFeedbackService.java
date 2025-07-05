package com.dep.soms.service;

import com.dep.soms.dto.client.ClientFeedbackDto;
import com.dep.soms.dto.client.ClientFeedbackRequest;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.Client;
import com.dep.soms.model.ClientFeedback;
import com.dep.soms.model.Guard;
import com.dep.soms.model.Site;
import com.dep.soms.model.User;
import com.dep.soms.repository.ClientFeedbackRepository;
import com.dep.soms.repository.ClientRepository;
import com.dep.soms.repository.GuardRepository;
import com.dep.soms.repository.SiteRepository;
import com.dep.soms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientFeedbackService {

    @Autowired
    private ClientFeedbackRepository clientFeedbackRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuardRepository guardRepository;

    @Transactional(readOnly = true)
    public List<ClientFeedbackDto> getAllFeedback() {
        return clientFeedbackRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientFeedbackDto getFeedbackById(Long id) {
        ClientFeedback feedback = clientFeedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + id));
        return mapToDto(feedback);
    }

    @Transactional(readOnly = true)
    public List<ClientFeedbackDto> getFeedbackByClientId(Long clientId) {
        return clientFeedbackRepository.findByClientId(clientId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientFeedbackDto> getFeedbackBySiteId(Long siteId) {
        return clientFeedbackRepository.findBySiteId(siteId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientFeedbackDto> getFeedbackByGuardId(Long guardId) {
        return clientFeedbackRepository.findByGuardId(guardId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClientFeedbackDto createFeedback(ClientFeedbackRequest request) {
        // Get current user
        String username = getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        // Get client
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        // Get site if provided
        Site site = null;
        if (request.getSiteId() != null) {
            site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + request.getSiteId()));
        }

        // Get guard if provided
        Guard guard = null;
        if (request.getGuardId() != null) {
            guard = guardRepository.findById(request.getGuardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + request.getGuardId()));
        }

        // Create feedback
        ClientFeedback feedback = ClientFeedback.builder()
                .client(client)
                .site(site)
                .submittedBy(currentUser)
                .rating(request.getRating())
                .comments(request.getComments())
                .serviceQualityRating(request.getServiceQualityRating())
                .responseTimeRating(request.getResponseTimeRating())
                .professionalismRating(request.getProfessionalismRating())
                .communicationRating(request.getCommunicationRating())
                .guardRating(request.getGuardRating())
                .guard(guard)
                .build();

        ClientFeedback savedFeedback = clientFeedbackRepository.save(feedback);
        return mapToDto(savedFeedback);
    }

    @Transactional
    public ClientFeedbackDto updateFeedback(Long id, ClientFeedbackRequest request) {
        ClientFeedback feedback = clientFeedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + id));

        // Get site if provided
        if (request.getSiteId() != null) {
            Site site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + request.getSiteId()));
            feedback.setSite(site);
        }

        // Get guard if provided
        if (request.getGuardId() != null) {
            Guard guard = guardRepository.findById(request.getGuardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + request.getGuardId()));
            feedback.setGuard(guard);
        }

        // Update feedback fields
        feedback.setRating(request.getRating());
        feedback.setComments(request.getComments());
        feedback.setServiceQualityRating(request.getServiceQualityRating());
        feedback.setResponseTimeRating(request.getResponseTimeRating());
        feedback.setProfessionalismRating(request.getProfessionalismRating());
        feedback.setCommunicationRating(request.getCommunicationRating());
        feedback.setGuardRating(request.getGuardRating());

        ClientFeedback updatedFeedback = clientFeedbackRepository.save(feedback);
        return mapToDto(updatedFeedback);
    }

    @Transactional
    public void deleteFeedback(Long id) {
        if (!clientFeedbackRepository.existsById(id)) {
            throw new ResourceNotFoundException("Feedback not found with id: " + id);
        }
        clientFeedbackRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingByClientId(Long clientId) {
        return clientFeedbackRepository.getAverageRatingByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingBySiteId(Long siteId) {
        return clientFeedbackRepository.getAverageRatingBySiteId(siteId);
    }

    @Transactional(readOnly = true)
    public Double getAverageGuardRatingByGuardId(Long guardId) {
        return clientFeedbackRepository.getAverageGuardRatingByGuardId(guardId);
    }

    private ClientFeedbackDto mapToDto(ClientFeedback feedback) {
        return ClientFeedbackDto.builder()
                .id(feedback.getId())
                .clientId(feedback.getClient().getId())
                .clientName(feedback.getClient().getName() != null ?
                        feedback.getClient().getName() : feedback.getClient().getCompanyName())
                .siteId(feedback.getSite() != null ? feedback.getSite().getId() : null)
                .siteName(feedback.getSite() != null ? feedback.getSite().getName() : null)
                .submittedById(feedback.getSubmittedBy().getId())
                .submittedByName(feedback.getSubmittedBy().getFirstName() + " " + feedback.getSubmittedBy().getLastName())
                .rating(feedback.getRating())
                .comments(feedback.getComments())
                .serviceQualityRating(feedback.getServiceQualityRating())
                .responseTimeRating(feedback.getResponseTimeRating())
                .professionalismRating(feedback.getProfessionalismRating())
                .communicationRating(feedback.getCommunicationRating())
                .guardRating(feedback.getGuardRating())
                .guardId(feedback.getGuard() != null ? feedback.getGuard().getId() : null)
                .guardName(feedback.getGuard() != null && feedback.getGuard().getUser() != null ?
                        feedback.getGuard().getUser().getFirstName() + " " + feedback.getGuard().getUser().getLastName() : null)
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
}
