package com.dep.soms.service;

import com.dep.soms.dto.incident.AttachmentDto;
import com.dep.soms.dto.incident.IncidentDto;
import com.dep.soms.dto.incident.IncidentRequest;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.*;
import com.dep.soms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    private IncidentAttachmentRepository incidentAttachmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    public IncidentDto createIncidentByGuard(IncidentRequest request, String guardUsername) {
        // Get the current user
        User user = userRepository.findByUsername(guardUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + guardUsername));

        // Find the guard associated with this user
        Guard guard = guardRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Guard profile not found for user: " + guardUsername));

        // Find the current active shift assignment for this guard
        ShiftAssignment currentShift = shiftAssignmentRepository
                .findCurrentActiveShiftByGuard(guard)
                .orElseThrow(() -> new IllegalStateException("Guard is not currently assigned to any active shift"));

        // Create the incident
        Incident incident = Incident.builder()
                .site(currentShift.getShift().getSite())
                .guard(guard)
                .shiftAssignment(currentShift)
                .reportedByUser(user)
                .incidentType(request.getIncidentType())
                .title(request.getTitle())
                .description(request.getDescription())
                .incidentTime(request.getIncidentTime())
                .locationDetails(request.getLocationDetails())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .severity(request.getSeverity())
                .status(Incident.IncidentStatus.REPORTED)
                .actionTaken(request.getActionTaken())
                .involvedParties(request.getInvolvedParties())
                .reportedTo(request.getReportedTo())
                .build();

        // Save the incident first
        Incident savedIncident = incidentRepository.save(incident);

        // Handle MULTIPLE file attachments
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            List<IncidentAttachment> attachments = new ArrayList<>();

            for (MultipartFile file : request.getAttachments()) {
                if (!file.isEmpty()) {
                    IncidentAttachment attachment = saveIncidentAttachment(savedIncident, file, user);
                    attachments.add(attachment);
                }
            }

            System.out.println("Successfully uploaded " + attachments.size() + " files for incident " + savedIncident.getId());
        }

        return mapToDto(savedIncident);
    }

    @Transactional
    public IncidentDto addMultipleAttachments(Long incidentId, List<MultipartFile> files, String username) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<IncidentAttachment> newAttachments = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                IncidentAttachment attachment = saveIncidentAttachment(incident, file, user);
                newAttachments.add(attachment);
            }
        }

        return mapToDto(incident);
    }

    private IncidentAttachment saveIncidentAttachment(Incident incident, MultipartFile file, User uploadedBy) {
        try {
            // Validate file type (optional - restrict to images only)
            if (!isValidImageFile(file)) {
                throw new IllegalArgumentException("Only image files are allowed: " + file.getOriginalFilename());
            }

            // Store the file and get the filename
            String filename = fileStorageService.storeFile(file);

            IncidentAttachment attachment = IncidentAttachment.builder()
                    .incident(incident)
                    .fileName(file.getOriginalFilename())
                    .filePath(filename) // Store just the filename
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .uploadedBy(uploadedBy)
                    .build();

            return incidentAttachmentRepository.save(attachment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save attachment: " + file.getOriginalFilename(), e);
        }
    }

    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp")
        );
    }
    @Transactional(readOnly = true)
    public IncidentDto getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));
        return mapToDto(incident);
    }
    @Transactional(readOnly = true)
    public List<IncidentDto> getAllIncidents() {
        return incidentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<IncidentDto> getIncidentsByGuard(String guardUsername) {
        User user = userRepository.findByUsername(guardUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + guardUsername));

        Guard guard = guardRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Guard profile not found for user: " + guardUsername));

        List<Incident> incidents = incidentRepository.findByGuardOrderByIncidentTimeDesc(guard);
        return incidents.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<IncidentDto> getIncidentsBySiteId(Long siteId, LocalDate startDate, LocalDate endDate) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));

        List<Incident> incidents;
        if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            incidents = incidentRepository.findBySiteAndIncidentTimeBetween(site, startDateTime, endDateTime);
        } else if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            incidents = incidentRepository.findBySiteAndIncidentTimeGreaterThanEqual(site, startDateTime);
        } else if (endDate != null) {
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            incidents = incidentRepository.findBySiteAndIncidentTimeLessThan(site, endDateTime);
        } else {
            incidents = incidentRepository.findBySite(site);
        }

        return incidents.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<IncidentDto> getIncidentsByGuardId(Long guardId, LocalDate startDate, LocalDate endDate) {
        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + guardId));

        List<Incident> incidents;
        if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            incidents = incidentRepository.findByGuardAndIncidentTimeBetween(guard, startDateTime, endDateTime);
        } else if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            incidents = incidentRepository.findByGuardAndIncidentTimeGreaterThanEqual(guard, startDateTime);
        } else if (endDate != null) {
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            incidents = incidentRepository.findByGuardAndIncidentTimeLessThan(guard, endDateTime);
        } else {
            incidents = incidentRepository.findByGuard(guard);
        }

        return incidents.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<IncidentDto> getFilteredIncidents(Long siteId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Incident> incidents;

        if (siteId != null) {
            Site site = siteRepository.findById(siteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));

            if (startTime != null && endTime != null) {
                incidents = incidentRepository.findBySiteAndIncidentTimeBetween(site, startTime, endTime);
            } else if (startTime != null) {
                incidents = incidentRepository.findBySiteAndIncidentTimeGreaterThanEqual(site, startTime);
            } else if (endTime != null) {
                incidents = incidentRepository.findBySiteAndIncidentTimeLessThan(site, endTime);
            } else {
                incidents = incidentRepository.findBySite(site);
            }
        } else {
            if (startTime != null && endTime != null) {
                incidents = incidentRepository.findByIncidentTimeBetween(startTime, endTime);
            } else if (startTime != null) {
                incidents = incidentRepository.findByIncidentTimeGreaterThanEqual(startTime);
            } else if (endTime != null) {
                incidents = incidentRepository.findByIncidentTimeLessThan(endTime);
            } else {
                incidents = incidentRepository.findAll();
            }
        }

        return incidents.stream().map(this::mapToDto).collect(Collectors.toList());
    }

//    @Transactional
//    public IncidentDto updateIncident(Long id, IncidentRequest request) {
//        Incident incident = incidentRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));
//
//        // Update incident fields
//        incident.setIncidentType(request.getIncidentType());
//        incident.setTitle(request.getTitle());
//        incident.setDescription(request.getDescription());
//        incident.setIncidentTime(request.getIncidentTime());
//        incident.setLocationDetails(request.getLocationDetails());
//        incident.setLatitude(request.getLatitude());
//        incident.setLongitude(request.getLongitude());
//        incident.setSeverity(request.getSeverity());
//        incident.setActionTaken(request.getActionTaken());
//        incident.setInvolvedParties(request.getInvolvedParties());
//        incident.setReportedTo(request.getReportedTo());
//        incident.setStatus(Incident.IncidentStatus.valueOf(request.getStatus()));
//
//        Incident updatedIncident = incidentRepository.save(incident);
//        return mapToDto(updatedIncident);
//    }
@Transactional
public IncidentDto updateIncident(Long id, IncidentRequest request) {
    Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

    // For admin updates, we only want to update specific fields
    // and preserve the original incident details

    // Update status if provided
    if (request.getStatus() != null && !request.getStatus().isEmpty()) {
        try {
            Incident.IncidentStatus status = Incident.IncidentStatus.valueOf(request.getStatus());
            incident.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + request.getStatus());
        }
    }

    // Update action taken if provided
    if (request.getActionTaken() != null) {
        incident.setActionTaken(request.getActionTaken());
    }

    // Update involved parties if provided
    if (request.getInvolvedParties() != null) {
        incident.setInvolvedParties(request.getInvolvedParties());
    }

    // Update reported to if provided
    if (request.getReportedTo() != null) {
        incident.setReportedTo(request.getReportedTo());
    }

    // The updatedAt field will be automatically updated by Spring Data JPA
    // due to the @LastModifiedDate annotation

    Incident updatedIncident = incidentRepository.save(incident);
    return mapToDto(updatedIncident);
}


    @Transactional
    public IncidentDto updateIncidentByGuard(Long id, IncidentRequest request, String guardUsername) {
        // Get the current user
        User user = userRepository.findByUsername(guardUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + guardUsername));

        // Find the guard associated with this user
        Guard guard = guardRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Guard profile not found for user: " + guardUsername));

        // Find the incident and verify ownership
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        // Verify that this guard is the one who reported the incident
        if (!incident.getGuard().getId().equals(guard.getId())) {
            throw new IllegalStateException("You can only update incidents that you reported");
        }

        // Guards can update description, action taken, involved parties, and reported to
        if (request.getDescription() != null) {
            incident.setDescription(request.getDescription());
        }

        if (request.getActionTaken() != null) {
            incident.setActionTaken(request.getActionTaken());
        }

        if (request.getInvolvedParties() != null) {
            incident.setInvolvedParties(request.getInvolvedParties());
        }

        if (request.getReportedTo() != null) {
            incident.setReportedTo(request.getReportedTo());
        }

        // The updatedAt field will be automatically updated by Spring Data JPA

        Incident updatedIncident = incidentRepository.save(incident);
        return mapToDto(updatedIncident);
    }


    @Transactional
    public void deleteIncident(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        // Delete associated files
        for (IncidentAttachment attachment : incident.getAttachments()) {
            try {
                fileStorageService.deleteFile(attachment.getFilePath());
            } catch (Exception e) {
                System.err.println("Failed to delete file: " + attachment.getFilePath());
            }
        }

        incidentRepository.deleteById(id);
    }

    @Transactional
    public void deleteAttachment(Long incidentId, Long attachmentId) {
        IncidentAttachment attachment = incidentAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));

        if (!attachment.getIncident().getId().equals(incidentId)) {
            throw new IllegalArgumentException("Attachment does not belong to the specified incident");
        }

        try {
            fileStorageService.deleteFile(attachment.getFilePath());
        } catch (Exception e) {
            System.err.println("Failed to delete file: " + attachment.getFilePath());
        }

        incidentAttachmentRepository.deleteById(attachmentId);
    }
    @Transactional(readOnly = true)
    public IncidentAttachment getAttachmentById(Long attachmentId) {
        return incidentAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));
    }

    private IncidentDto mapToDto(Incident incident) {
        List<AttachmentDto> attachmentDtos = incident.getAttachments().stream()
                .map(this::mapAttachmentToDto)
                .collect(Collectors.toList());

        return IncidentDto.builder()
                .id(incident.getId())
                .siteId(incident.getSite().getId())
                .siteName(incident.getSite().getName())
                .siteAddress(incident.getSite().getAddress())
                .guardId(incident.getGuard() != null ? incident.getGuard().getId() : null)
                .guardName(incident.getGuard() != null ?
                        incident.getGuard().getUser().getFirstName() + " " + incident.getGuard().getUser().getLastName() : null)
                .shiftAssignmentId(incident.getShiftAssignment() != null ? incident.getShiftAssignment().getId() : null)
                .shiftName(incident.getShiftAssignment() != null ?
                        incident.getShiftAssignment().getShift().getName() : null)
                .reportedByUserId(incident.getReportedByUser() != null ? incident.getReportedByUser().getId() : null)
                .reportedByUserName(incident.getReportedByUser() != null ?
                        incident.getReportedByUser().getFirstName() + " " + incident.getReportedByUser().getLastName() : null)
                .incidentType(incident.getIncidentType())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .incidentTime(incident.getIncidentTime())
                .locationDetails(incident.getLocationDetails())
                .latitude(incident.getLatitude())
                .longitude(incident.getLongitude())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .actionTaken(incident.getActionTaken())
                .involvedParties(incident.getInvolvedParties())
                .reportedTo(incident.getReportedTo())
                .attachments(attachmentDtos)
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }

    private AttachmentDto mapAttachmentToDto(IncidentAttachment attachment) {
        return AttachmentDto.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileUrl("/api/uploads/" + attachment.getFilePath()) // Uses your existing FileController
                .fileSize(attachment.getFileSize())
                .uploadedByName(attachment.getUploadedBy().getFirstName() + " " + attachment.getUploadedBy().getLastName())
                .uploadTime(attachment.getCreatedAt())
                .build();
    }
}

