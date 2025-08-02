package com.dep.soms.service;

import com.dep.soms.dto.patrol.*;
import com.dep.soms.dto.site.SiteDto;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.*;
import com.dep.soms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupervisorPatrolService {

    private final PatrolAssignmentRepository assignmentRepository;
    private final PatrolRepository patrolRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;

    private final PatrolAssignmentRepository patrolAssignmentRepository;

    private static final Logger log = LoggerFactory.getLogger(SupervisorPatrolService.class);

    @Autowired
    public SupervisorPatrolService(PatrolAssignmentRepository assignmentRepository,
                                   PatrolRepository patrolRepository,
                                   UserRepository userRepository,
                                   LocationService locationService, PatrolAssignmentRepository patrolAssignmentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.patrolRepository = patrolRepository;
        this.userRepository = userRepository;
        this.locationService = locationService;
        this.patrolAssignmentRepository = patrolAssignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getAllPatrols() {
        return patrolRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PatrolDto> getPatrolsBySupervisor(Long supervisorId) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with id: " + supervisorId));

        return assignmentRepository.findBySupervisor(supervisor).stream()
                .map(PatrolAssignment::getPatrol)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<PatrolAssignmentDto> getTodayAssignments(Long supervisorId) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with id: " + supervisorId));

        LocalDate today = LocalDate.now();
        List<PatrolAssignment> assignments = assignmentRepository.findBySupervisorAndStartTimeBetween(
                supervisor,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );

        return assignments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatrolAssignmentDto> getInProgressAssignments(Long supervisorId) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with id: " + supervisorId));

        return assignmentRepository.findBySupervisorAndStatus(
                        supervisor,
                        PatrolAssignment.PatrolAssignmentStatus.ACCEPTED
                ).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public PatrolAssignmentDto acceptAssignment(Long assignmentId, Long supervisorId) {
        PatrolAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (!assignment.getSupervisor().getId().equals(supervisorId)) {
            throw new IllegalArgumentException("This assignment doesn't belong to the current supervisor");
        }

        if (assignment.getStatus() != PatrolAssignment.PatrolAssignmentStatus.PENDING) {
            throw new IllegalStateException("Assignment can only be accepted from PENDING status");
        }

        assignment.setStatus(PatrolAssignment.PatrolAssignmentStatus.ACCEPTED);
        assignment.setAcceptedAt(LocalDateTime.now());

        return mapToDto(assignmentRepository.save(assignment));
    }

    @Transactional
    public PatrolAssignmentDto declineAssignment(Long assignmentId, Long supervisorId, String reason) {
        PatrolAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (!assignment.getSupervisor().getId().equals(supervisorId)) {
            throw new IllegalArgumentException("This assignment doesn't belong to the current supervisor");
        }

        if (assignment.getStatus() != PatrolAssignment.PatrolAssignmentStatus.PENDING) {
            throw new IllegalStateException("Assignment can only be declined from PENDING status");
        }

        assignment.setStatus(PatrolAssignment.PatrolAssignmentStatus.DECLINED);
        assignment.setDeclinedAt(LocalDateTime.now());
        assignment.setDeclineReason(reason);

        return mapToDto(assignmentRepository.save(assignment));
    }

    @Transactional
    public PatrolDto startPatrol(Long assignmentId, Long supervisorId, Double latitude, Double longitude) {
        PatrolAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (!assignment.getSupervisor().getId().equals(supervisorId)) {
            throw new IllegalArgumentException("This assignment doesn't belong to the current supervisor");
        }

        if (assignment.getStatus() != PatrolAssignment.PatrolAssignmentStatus.ACCEPTED) {
            throw new IllegalStateException("Patrol can only be started from ACCEPTED status");
        }

        Patrol patrol = assignment.getPatrol();

        if (patrol.getStatus() != Patrol.PatrolStatus.SCHEDULED) {
            throw new IllegalStateException("Patrol can only be started from SCHEDULED status");
        }

        patrol.setStatus(Patrol.PatrolStatus.IN_PROGRESS);
        patrol.setActualStartTime(LocalDateTime.now());

        // Record starting location for first checkpoint
        if (latitude != null && longitude != null) {
            Optional<PatrolCheckpoint> firstCheckpoint = patrol.getCheckpoints().stream()
                    .filter(cp -> cp.getCheckTime() == null)
                    .findFirst();

            if (firstCheckpoint.isPresent()) {
                PatrolCheckpoint checkpoint = firstCheckpoint.get();
                checkpoint.setCheckTime(LocalDateTime.now());
                checkpoint.setLatitude(latitude);
                checkpoint.setLongitude(longitude);
            }
        }

        return mapToDto(patrolRepository.save(patrol));
    }

    @Transactional
    public PatrolDto updateCheckpoint(Long assignmentId, Long checkpointId, Long supervisorId, CheckpointUpdateRequest request) {
        PatrolAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (!assignment.getSupervisor().getId().equals(supervisorId)) {
            throw new IllegalArgumentException("This assignment doesn't belong to the current supervisor");
        }

        Patrol patrol = assignment.getPatrol();

        if (patrol.getStatus() != Patrol.PatrolStatus.IN_PROGRESS) {
            throw new IllegalStateException("Checkpoint can only be updated during IN_PROGRESS patrol");
        }

        PatrolCheckpoint checkpoint = patrol.getCheckpoints().stream()
                .filter(cp -> cp.getId().equals(checkpointId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Checkpoint not found with id: " + checkpointId));

        // Verify location if provided
        if (request.getLatitude() != null && request.getLongitude() != null) {
            boolean isAtCorrectLocation = locationService.isWithinAllowedRadius(
                    request.getLatitude(),
                    request.getLongitude(),
                    checkpoint.getPatrolPoint().getLatitude(),
                    checkpoint.getPatrolPoint().getLongitude(),
                    request.getGpsAccuracy()
            );

            if (!isAtCorrectLocation) {
                double distance = locationService.getDistanceInMeters(
                        request.getLatitude(), request.getLongitude(),
                        checkpoint.getPatrolPoint().getLatitude(),
                        checkpoint.getPatrolPoint().getLongitude()
                );

                String locationNote = "Location verification failed - Distance: " +
                        Math.round(distance) + "m from checkpoint";
                request.setNotes(request.getNotes() != null ?
                        request.getNotes() + "\n" + locationNote : locationNote);
            }
        }

        checkpoint.setCheckTime(LocalDateTime.now());
        if (request.getLatitude() != null) checkpoint.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) checkpoint.setLongitude(request.getLongitude());
        if (request.getNotes() != null) checkpoint.setNotes(request.getNotes());
        if (request.getPhotoPath() != null) checkpoint.setPhotoPath(request.getPhotoPath());

        return mapToDto(patrolRepository.save(patrol));
    }

    @Transactional
    public PatrolDto completePatrol(Long assignmentId, Long supervisorId, Double latitude, Double longitude, String notes) {
        PatrolAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (!assignment.getSupervisor().getId().equals(supervisorId)) {
            throw new IllegalArgumentException("This assignment doesn't belong to the current supervisor");
        }

        Patrol patrol = assignment.getPatrol();

        if (patrol.getStatus() != Patrol.PatrolStatus.IN_PROGRESS) {
            throw new IllegalStateException("Patrol can only be completed from IN_PROGRESS status");
        }

        // Mark any remaining checkpoints as completed with current location
        if (latitude != null && longitude != null) {
            patrol.getCheckpoints().stream()
                    .filter(cp -> cp.getCheckTime() == null)
                    .forEach(cp -> {
                        cp.setCheckTime(LocalDateTime.now());
                        cp.setLatitude(latitude);
                        cp.setLongitude(longitude);
                    });
        }

        patrol.setStatus(Patrol.PatrolStatus.COMPLETED);
        patrol.setActualEndTime(LocalDateTime.now());
        assignment.setStatus(PatrolAssignment.PatrolAssignmentStatus.COMPLETED);
        assignment.setCompletedAt(LocalDateTime.now());

        if (notes != null && !notes.isEmpty()) {
            patrol.setNotes(patrol.getNotes() != null ?
                    patrol.getNotes() + "\n" + notes : notes);
        }

        patrolRepository.save(patrol);
        assignmentRepository.save(assignment);

        return mapToDto(patrol);
    }

    private PatrolDto mapToDto(Patrol patrol) {
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
                .checkpoints(patrol.getCheckpoints().stream()
                        .map(this::mapCheckpointToDto)
                        .sorted(Comparator.comparing(PatrolDto.CheckpointDto::getCheckTime,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(Collectors.toList()))
                .build();
    }

    private PatrolDto.CheckpointDto mapCheckpointToDto(PatrolCheckpoint checkpoint) {
        return PatrolDto.CheckpointDto.builder()
                .id(checkpoint.getId())
                .patrolPointId(checkpoint.getPatrolPoint().getId())
                .patrolPointName(checkpoint.getPatrolPoint().getName())
                .checkTime(checkpoint.getCheckTime())
                .latitude(checkpoint.getLatitude())
                .longitude(checkpoint.getLongitude())
                .notes(checkpoint.getNotes())
                .photoPath(checkpoint.getPhotoPath())
                .expectedLatitude(checkpoint.getPatrolPoint().getLatitude())
                .expectedLongitude(checkpoint.getPatrolPoint().getLongitude())
                .build();
    }

//    private PatrolAssignmentDto mapToDto(PatrolAssignment assignment) {
//        return PatrolAssignmentDto.builder()
//                .id(assignment.getId())
//                .patrolId(assignment.getPatrol().getId())
//                .patrolDetails(mapToDto(assignment.getPatrol()))
//                .supervisorId(assignment.getSupervisor().getId())
//                .supervisorName(assignment.getSupervisor().getFirstName() + " " + assignment.getSupervisor().getLastName())
//                .status(assignment.getStatus().toString())
//                //.patrolType(assignment.getPatrolType() != null ? assignment.getPatrolType().toString() : null)
//                .assignedAt(assignment.getAssignedAt())
//                .acceptedAt(assignment.getAcceptedAt())
//                .declinedAt(assignment.getDeclinedAt())
//                .declineReason(assignment.getDeclineReason())
//                .completedAt(assignment.getCompletedAt())
//                .assignmentDate(assignment.getAssignmentDate())
//                //.startTime(assignment.getStartTime())
//                //.endTime(assignment.getEndTime())
//                //.notes(assignment.getNotes())
//                .createdAt(assignment.getCreatedAt())
//                .updatedAt(assignment.getUpdatedAt())
//                .build();
//    }

    private PatrolAssignmentDto mapToDto(PatrolAssignment assignment) {
        return PatrolAssignmentDto.builder()
                .id(assignment.getId())
                .patrolId(assignment.getPatrol().getId())
                .patrolDetails(mapToDto(assignment.getPatrol()))
                .supervisorId(assignment.getSupervisor().getId())
                .supervisorName(assignment.getSupervisor().getFirstName() + " " + assignment.getSupervisor().getLastName())
                .status(assignment.getStatus().toString())
                .assignedAt(assignment.getAssignedAt())
                .acceptedAt(assignment.getAcceptedAt())
                .declinedAt(assignment.getDeclinedAt())
                .primarySite(mapSiteToDto(assignment.getPatrol().getPrimarySite()))
                .sites(assignment.getPatrol().getSites().stream()
                        .map(this::mapSiteToDto)
                        .collect(Collectors.toList()))
                .declineReason(assignment.getDeclineReason())
                .completedAt(assignment.getCompletedAt())
                .assignmentDate(assignment.getAssignmentDate()) // THIS IS CRUCIAL
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    private SiteDto mapSiteToDto(Site site) {
        if (site == null) {
            return null;
        }
        return SiteDto.builder()
                .id(site.getId())
                .name(site.getName())
                // Include other site properties you need
                .build();
    }

    @Transactional(readOnly = true)
    public List<PatrolAssignmentDto> getAllAssignmentsBySupervisor(Long supervisorId) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with id: " + supervisorId));

        return assignmentRepository.findBySupervisor(supervisor).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}