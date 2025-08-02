package com.dep.soms.service;

import com.dep.soms.dto.patrol.PatrolAssignmentDto;
import com.dep.soms.dto.patrol.PatrolDto;
import com.dep.soms.dto.site.SiteDto;
import com.dep.soms.dto.user.UserDto;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.*;
import com.dep.soms.repository.PatrolAssignmentRepository;
import com.dep.soms.repository.PatrolRepository;
import com.dep.soms.repository.SiteRepository;
import com.dep.soms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatrolAssignmentQueryService {

    private final PatrolAssignmentRepository assignmentRepository;
    private final PatrolRepository patrolRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;

    @Autowired
    public PatrolAssignmentQueryService(PatrolAssignmentRepository assignmentRepository,
                                        PatrolRepository patrolRepository,
                                        UserRepository userRepository,
                                        SiteRepository siteRepository) {
        this.assignmentRepository = assignmentRepository;
        this.patrolRepository = patrolRepository;
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
    }

    @Transactional(readOnly = true)
    public List<PatrolAssignmentDto> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getAllPatrolsFromAssignments() {
        return assignmentRepository.findAll().stream()
                .map(assignment -> mapToPatrolDto(assignment.getPatrol()))
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getPatrolsBySiteIdFromAssignments(Long siteId) {
        // Verify site exists
        siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));

        return assignmentRepository.findAll().stream()
                .map(PatrolAssignment::getPatrol)
                .filter(patrol -> patrol.getSites().stream()
                        .anyMatch(site -> site.getId().equals(siteId)))
                .map(this::mapToPatrolDto)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getPatrolsByTimeRangeFromAssignments(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        return assignmentRepository.findAll().stream()
                .map(PatrolAssignment::getPatrol)
                .filter(patrol -> isBetween(patrol.getStartTime(), start, end) ||
                        isBetween(patrol.getEndTime(), start, end))
                .map(this::mapToPatrolDto)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getPatrolsBySupervisorId(Long supervisorId) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with id: " + supervisorId));

        return assignmentRepository.findBySupervisor(supervisor).stream()
                .map(assignment -> mapToPatrolDto(assignment.getPatrol()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getPatrolsBySupervisorAndStatus(Long supervisorId, Patrol.PatrolStatus status) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with id: " + supervisorId));

        PatrolAssignment.PatrolAssignmentStatus assignmentStatus =
                mapPatrolStatusToAssignmentStatus(status);

        return assignmentRepository.findBySupervisorAndStatus(supervisor, assignmentStatus).stream()
                .map(assignment -> mapToPatrolDto(assignment.getPatrol()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getActivePatrolsFromAssignments() {
        // Get all assignments with status ACCEPTED (which corresponds to IN_PROGRESS patrols)
        List<PatrolAssignment> activeAssignments = assignmentRepository.findByStatus(
                PatrolAssignment.PatrolAssignmentStatus.ACCEPTED
        );

        // Convert to PatrolDto and filter to ensure patrol status is IN_PROGRESS
        return activeAssignments.stream()
                .map(PatrolAssignment::getPatrol)
                .filter(patrol -> patrol.getStatus() == Patrol.PatrolStatus.IN_PROGRESS)
                .map(this::mapToPatrolDto)
                .distinct() // Ensure we don't return duplicate patrols
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public PatrolDto getPatrolByIdFromAssignments(Long id) {
        return assignmentRepository.findAll().stream()
                .map(PatrolAssignment::getPatrol)
                .filter(patrol -> patrol.getId().equals(id))
                .findFirst()
                .map(this::mapToPatrolDto)
                .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + id));
    }

    private boolean isBetween(LocalDateTime time, LocalDateTime start, LocalDateTime end) {
        return !time.isBefore(start) && !time.isAfter(end);
    }

    private PatrolAssignment.PatrolAssignmentStatus mapPatrolStatusToAssignmentStatus(Patrol.PatrolStatus status) {
        switch (status) {
            case SCHEDULED: return PatrolAssignment.PatrolAssignmentStatus.PENDING;
            case IN_PROGRESS: return PatrolAssignment.PatrolAssignmentStatus.ACCEPTED;
            case COMPLETED: return PatrolAssignment.PatrolAssignmentStatus.COMPLETED;
            case CANCELLED: return PatrolAssignment.PatrolAssignmentStatus.DECLINED;
            default: return PatrolAssignment.PatrolAssignmentStatus.PENDING;
        }
    }

    private PatrolAssignmentDto mapToDto(PatrolAssignment assignment) {
        return PatrolAssignmentDto.builder()
                .id(assignment.getId())
                .patrolId(assignment.getPatrol().getId())
                .supervisor(mapToUserDto(assignment.getSupervisor()))  // Map supervisor to UserDto
                .status(assignment.getStatus().toString())
                .patrolId(assignment.getPatrol().getId())
                .supervisorId(assignment.getSupervisor().getId())
                .supervisorName(assignment.getSupervisor().getFirstName() + " " + assignment.getSupervisor().getLastName())
                .assignedAt(assignment.getAssignedAt())
                .acceptedAt(assignment.getAcceptedAt())
                .declinedAt(assignment.getDeclinedAt())
                .declineReason(assignment.getDeclineReason())
                .completedAt(assignment.getCompletedAt())
                .createdAt(assignment.getCreatedAt())
                .assignmentDate(assignment.getAssignmentDate())
                .updatedAt(assignment.getUpdatedAt())
                .patrolDetails(mapToPatrolDto(assignment.getPatrol()))
                //.site(mapToSiteDto(assignment.getPatrol().getPrimarySite()))  // Map primary site to SiteDto
                .build();
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .active(user.isActive())
                //.lastLogin(user.getLastLogin())
                //.createdAt(user.getCreatedAt())
                //.updatedAt(user.getUpdatedAt())
                //.preferredLanguage(user.getPreferredLanguage())
                .build();
    }

    private SiteDto mapToSiteDto(Site site) {
        return SiteDto.builder()
                .id(site.getId())
                .name(site.getName())
                .address(site.getAddress())
                .city(site.getCity())
                .country(site.getCountry())
                .siteCode(site.getSiteCode())
                .contactPerson(site.getContactPerson())
                .contactPhone(site.getContactPhone())
                .latitude(site.getLatitude())
                .longitude(site.getLongitude())
                .active(site.isActive())
               // .createdAt(site.getCreatedAt())
                //.updatedAt(site.getUpdatedAt())
                .build();
    }

    private PatrolDto mapToPatrolDto(Patrol patrol) {
        return PatrolDto.builder()
                .id(patrol.getId())
                .name(patrol.getName())
                .description(patrol.getDescription())
                .primarySiteId(patrol.getPrimarySite().getId())
                .primarySiteName(patrol.getPrimarySite().getName())
                .siteIds(patrol.getSites().stream().map(Site::getId).collect(Collectors.toList()))
                .siteNames(patrol.getSites().stream().map(Site::getName).collect(Collectors.toList()))
                .startTime(patrol.getStartTime())
                .endTime(patrol.getEndTime())
                .actualStartTime(patrol.getActualStartTime())
                .actualEndTime(patrol.getActualEndTime())
                .patrolType(patrol.getPatrolType().toString())
                .requiredSupervisors(patrol.getRequiredSupervisors())
                .colorCode(patrol.getColorCode())
                .status(patrol.getStatus().toString())
                .notes(patrol.getNotes())
                .active(patrol.getActive())
                .createdAt(patrol.getCreatedAt())
                .updatedAt(patrol.getUpdatedAt())
                .build();
    }
}