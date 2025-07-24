package com.dep.soms.service;

import com.dep.soms.dto.patrol.*;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.*;
import com.dep.soms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatrolService {
    private final PatrolRepository patrolRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final PatrolPointRepository patrolPointRepository;
    private final EmailService emailService;
    private final LocationService locationService;

    @Autowired
    public PatrolService(PatrolRepository patrolRepository,
                         SiteRepository siteRepository,
                         UserRepository userRepository,
                         PatrolPointRepository patrolPointRepository,
                         EmailService emailService,
                         LocationService locationService) {
        this.patrolRepository = patrolRepository;
        this.siteRepository = siteRepository;
        this.userRepository = userRepository;
        this.patrolPointRepository = patrolPointRepository;
        this.emailService = emailService;
        this.locationService = locationService;
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getAllPatrols() {
        return patrolRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getPatrolsBySiteId(Long siteId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));

        return patrolRepository.findBySite(site).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getPatrolsByTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        return patrolRepository.findByStartTimeBetweenOrEndTimeBetween(start, end, start, end).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public PatrolDto createPatrol(CreatePatrolRequest request) {
        // Validate supervisor exists and has SUPERVISOR role
        User supervisor = userRepository.findById(request.getSupervisorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getSupervisorId()));

        if (!isSupervisor(supervisor)) {
            throw new IllegalArgumentException("User is not a supervisor");
        }

        // Validate sites exist
        List<Site> sites = siteRepository.findAllById(request.getSiteIds());
        if (sites.size() != request.getSiteIds().size()) {
            throw new ResourceNotFoundException("One or more sites not found");
        }

        // Create the patrol with primary site (first in list)
        Patrol patrol = Patrol.builder()
                .supervisor(supervisor)
                .site(sites.get(0))
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(Patrol.PatrolStatus.SCHEDULED)
                .notes(request.getNotes())
                .build();

        // Save the patrol first to generate ID
        Patrol savedPatrol = patrolRepository.save(patrol);

        // Create patrol checkpoints for all patrol points in all sites
        Set<PatrolCheckpoint> checkpoints = new HashSet<>();
        for (Site site : sites) {
            List<PatrolPoint> points = patrolPointRepository.findBySiteAndActive(site, true);

            // Sort points by sequence number
            points.sort(Comparator.comparingInt(PatrolPoint::getSequenceNumber));

            for (PatrolPoint point : points) {
                PatrolCheckpoint checkpoint = PatrolCheckpoint.builder()
                        .patrol(savedPatrol)
                        .patrolPoint(point)
                        .latitude(point.getLatitude())
                        .longitude(point.getLongitude())
                        .build();
                checkpoints.add(checkpoint);
            }
        }

        savedPatrol.setCheckpoints(checkpoints);
        Patrol finalPatrol = patrolRepository.save(savedPatrol);

        // Send notification to supervisor
        sendPatrolAssignmentNotification(finalPatrol);

        return mapToDto(finalPatrol);
    }

    @Transactional
    public BulkPatrolResponse createBulkPatrols(BulkPatrolRequest request) {
        List<PatrolDto> createdPatrols = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            // Validate all supervisors first
            Map<Long, User> supervisorMap = new HashMap<>();
            for (Long supervisorId : request.getSupervisorIds()) {
                try {
                    User supervisor = userRepository.findById(supervisorId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + supervisorId));

                    if (!isSupervisor(supervisor)) {
                        errors.add("User " + supervisor.getFirstName() + " " + supervisor.getLastName() + " is not a supervisor");
                        continue;
                    }
                    supervisorMap.put(supervisorId, supervisor);
                } catch (Exception e) {
                    errors.add("Error processing supervisor ID " + supervisorId + ": " + e.getMessage());
                }
            }

            if (supervisorMap.isEmpty()) {
                throw new IllegalArgumentException("No valid supervisors found for assignment");
            }

            // Validate sites exist
            List<Site> sites = siteRepository.findAllById(request.getSiteIds());
            if (sites.size() != request.getSiteIds().size()) {
                throw new ResourceNotFoundException("One or more sites not found");
            }

            // Create patrols for each supervisor
            for (User supervisor : supervisorMap.values()) {
                try {
                    Patrol patrol = Patrol.builder()
                            .supervisor(supervisor)
                            .site(sites.get(0)) // Primary site
                            .startTime(request.getStartTime())
                            .endTime(request.getEndTime())
                            .status(Patrol.PatrolStatus.SCHEDULED)
                            .notes(request.getNotes())
                            .build();

                    Patrol savedPatrol = patrolRepository.save(patrol);

                    // Create checkpoints
                    Set<PatrolCheckpoint> checkpoints = new HashSet<>();
                    for (Site site : sites) {
                        List<PatrolPoint> points = patrolPointRepository.findBySiteAndActive(site, true);
                        points.sort(Comparator.comparingInt(PatrolPoint::getSequenceNumber));

                        for (PatrolPoint point : points) {
                            PatrolCheckpoint checkpoint = PatrolCheckpoint.builder()
                                    .patrol(savedPatrol)
                                    .patrolPoint(point)
                                    .latitude(point.getLatitude())
                                    .longitude(point.getLongitude())
                                    .build();
                            checkpoints.add(checkpoint);
                        }
                    }

                    savedPatrol.setCheckpoints(checkpoints);
                    Patrol finalPatrol = patrolRepository.save(savedPatrol);
                    createdPatrols.add(mapToDto(finalPatrol));

                    sendPatrolAssignmentNotification(finalPatrol);

                } catch (Exception e) {
                    errors.add("Failed to create patrol for supervisor " +
                            supervisor.getFirstName() + " " + supervisor.getLastName() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("System error: " + e.getMessage());
        }

        return BulkPatrolResponse.builder()
                .createdPatrols(createdPatrols)
                .warnings(warnings)
                .errors(errors)
                .totalPatrolsCreated(createdPatrols.size())
                .build();
    }

    @Transactional
    public PatrolDto startPatrol(Long patrolId, Double latitude, Double longitude) {
        Patrol patrol = patrolRepository.findById(patrolId)
                .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + patrolId));

        if (patrol.getStatus() != Patrol.PatrolStatus.SCHEDULED) {
            throw new IllegalStateException("Patrol can only be started from SCHEDULED status");
        }

        patrol.setStatus(Patrol.PatrolStatus.IN_PROGRESS);
        patrol.setStartTime(LocalDateTime.now());

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

        Patrol updatedPatrol = patrolRepository.save(patrol);
        return mapToDto(updatedPatrol);
    }

    @Transactional
    public PatrolDto updateCheckpoint(Long patrolId, Long checkpointId, CheckpointUpdateRequest request) {
        Patrol patrol = patrolRepository.findById(patrolId)
                .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + patrolId));

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

        Patrol updatedPatrol = patrolRepository.save(patrol);
        return mapToDto(updatedPatrol);
    }

    @Transactional
    public PatrolDto completePatrol(Long patrolId, Double latitude, Double longitude, String notes) {
        Patrol patrol = patrolRepository.findById(patrolId)
                .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + patrolId));

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
        patrol.setEndTime(LocalDateTime.now());

        if (notes != null && !notes.isEmpty()) {
            patrol.setNotes(patrol.getNotes() != null ?
                    patrol.getNotes() + "\n" + notes : notes);
        }

        Patrol updatedPatrol = patrolRepository.save(patrol);
        return mapToDto(updatedPatrol);
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getPatrolsBySupervisorId(Long supervisorId) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with id: " + supervisorId));

        return patrolRepository.findBySupervisor(supervisor).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatrolDto getPatrolById(Long id) {
        Patrol patrol = patrolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + id));
        return mapToDto(patrol);
    }

    @Transactional(readOnly = true)
    public List<PatrolDto> getActivePatrols() {
        return patrolRepository.findByStatus(Patrol.PatrolStatus.IN_PROGRESS).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelPatrol(Long patrolId, String reason) {
        Patrol patrol = patrolRepository.findById(patrolId)
                .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + patrolId));

        if (patrol.getStatus() != Patrol.PatrolStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled patrols can be cancelled");
        }

        patrol.setStatus(Patrol.PatrolStatus.MISSED);
        patrol.setNotes(patrol.getNotes() != null ?
                patrol.getNotes() + "\nCancelled: " + reason : "Cancelled: " + reason);

        patrolRepository.save(patrol);
    }

    private boolean isSupervisor(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().toString().equals("ROLE_SUPERVISOR"));
    }

    private void sendPatrolAssignmentNotification(Patrol patrol) {
        try {
            emailService.sendPatrolAssignmentEmail(
                    patrol.getSupervisor().getEmail(),
                    patrol.getSupervisor().getFirstName() + " " + patrol.getSupervisor().getLastName(),
                    patrol.getSite().getName(),
                    patrol.getStartTime(),
                    patrol.getEndTime(),
                    patrol.getNotes()
            );
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send patrol assignment email: " + e.getMessage());
        }
    }

    private PatrolDto mapToDto(Patrol patrol) {
        return PatrolDto.builder()
                .id(patrol.getId())
                .supervisorId(patrol.getSupervisor().getId())
                .supervisorName(patrol.getSupervisor().getFirstName() + " " + patrol.getSupervisor().getLastName())
                .siteId(patrol.getSite().getId())
                .siteName(patrol.getSite().getName())
                .startTime(patrol.getStartTime())
                .endTime(patrol.getEndTime())
                .status(patrol.getStatus())
                .notes(patrol.getNotes())
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
}