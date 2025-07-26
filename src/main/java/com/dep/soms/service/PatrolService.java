package com.dep.soms.service;

import com.dep.soms.dto.patrol.*;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.*;
import com.dep.soms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final PatrolAssignmentRepository assignmentRepository;
    private static final Logger log = LoggerFactory.getLogger(ShiftAssignmentService.class);


    @Autowired
    public PatrolService(PatrolRepository patrolRepository,
                         SiteRepository siteRepository,
                         UserRepository userRepository,
                         PatrolPointRepository patrolPointRepository,
                         EmailService emailService,
                         LocationService locationService,
                         PatrolAssignmentRepository assignmentRepository) {
        this.patrolRepository = patrolRepository;
        this.siteRepository = siteRepository;
        this.userRepository = userRepository;
        this.patrolPointRepository = patrolPointRepository;
        this.emailService = emailService;
        this.locationService = locationService;
        this.assignmentRepository = assignmentRepository;
    }
    @Transactional(readOnly = true)
    public List<PatrolDto> getAllPatrols() {
        return patrolRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }



    @Transactional
    public PatrolDto createPatrol(CreatePatrolRequest request) {
        // Validate required fields
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new IllegalArgumentException("Patrol name is required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start and end times are required");
        }
        if (request.getPrimarySiteId() == null) {
            throw new IllegalArgumentException("Primary site is required");
        }

        // Parse times
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = LocalTime.parse(request.getEndTime());

        // Combine with current date
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), startTime);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), endTime);

        // Validate time order
        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // Get primary site
        Site primarySite = siteRepository.findById(request.getPrimarySiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Primary site not found with id: " + request.getPrimarySiteId()));

        // Get all sites (including primary)
        Set<Long> allSiteIds = new HashSet<>();
        allSiteIds.add(request.getPrimarySiteId());
        if (request.getSiteIds() != null) {
            allSiteIds.addAll(request.getSiteIds());
        }

        List<Site> sites = siteRepository.findAllById(allSiteIds);
        if (sites.size() != allSiteIds.size()) {
            throw new ResourceNotFoundException("One or more sites not found");
        }

        // Get supervisor if provided
        User supervisor = null;
        if (request.getSupervisorId() != null) {
            supervisor = userRepository.findById(request.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with id: " + request.getSupervisorId()));

            if (!isSupervisor(supervisor)) {
                throw new IllegalArgumentException("User is not a supervisor");
            }
        }

        // Create patrol
        Patrol patrol = Patrol.builder()
                .name(request.getName())
                .description(request.getDescription())
                .primarySite(primarySite)
                .sites(new HashSet<>(sites))
                //.supervisor(supervisor)
                .startTime(startDateTime)
                .endTime(endDateTime)
                .patrolType(Patrol.PatrolType.valueOf(request.getPatrolType()))
                .requiredSupervisors(request.getRequiredSupervisors())
                .colorCode(request.getColorCode())
                .status(Patrol.PatrolStatus.SCHEDULED)
                .notes(request.getNotes())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        // Create checkpoints for all patrol points in all sites
        Set<PatrolCheckpoint> checkpoints = new HashSet<>();
        for (Site site : sites) {
            List<PatrolPoint> points = patrolPointRepository.findBySiteAndActive(site, true);
            points.sort(Comparator.comparingInt(PatrolPoint::getSequenceNumber));

            for (PatrolPoint point : points) {
                PatrolCheckpoint checkpoint = PatrolCheckpoint.builder()
                        .patrol(patrol)
                        .patrolPoint(point)
                        .latitude(point.getLatitude())
                        .longitude(point.getLongitude())
                        .build();
                checkpoints.add(checkpoint);
            }
        }

        patrol.setCheckpoints(checkpoints);
        Patrol savedPatrol = patrolRepository.save(patrol);

        // Send notification if supervisor is assigned
//        if (supervisor != null) {
//            sendPatrolAssignmentNotification(savedPatrol);
//        }

        return mapToDto(savedPatrol);
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
                    // Parse times
                    LocalTime startTime = LocalTime.parse(request.getStartTime());
                    LocalTime endTime = LocalTime.parse(request.getEndTime());
                    LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), startTime);
                    LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), endTime);

                    Patrol patrol = Patrol.builder()
                            .name(request.getName())
                            .description(request.getDescription())
                            .primarySite(sites.get(0))
                            .sites(new HashSet<>(sites))
                            //.supervisor(supervisor)
                            .startTime(startDateTime)
                            .endTime(endDateTime)
                            .patrolType(Patrol.PatrolType.valueOf(request.getPatrolType()))
                            .requiredSupervisors(request.getRequiredSupervisors())
                            .colorCode(request.getColorCode())
                            .status(Patrol.PatrolStatus.SCHEDULED)
                            .notes(request.getNotes())
                            .active(request.getActive() != null ? request.getActive() : true)
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

//                    sendPatrolAssignmentNotification(finalPatrol);

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
        patrol.setActualEndTime(LocalDateTime.now());

        if (notes != null && !notes.isEmpty()) {
            patrol.setNotes(patrol.getNotes() != null ?
                    patrol.getNotes() + "\n" + notes : notes);
        }

        Patrol updatedPatrol = patrolRepository.save(patrol);
        return mapToDto(updatedPatrol);
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


    @Transactional
    public void cancelPatrol(Long patrolId, String reason) {
        Patrol patrol = patrolRepository.findById(patrolId)
                .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + patrolId));

        if (patrol.getStatus() != Patrol.PatrolStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled patrols can be cancelled");
        }

        patrol.setStatus(Patrol.PatrolStatus.CANCELLED);
        patrol.setNotes(patrol.getNotes() != null ?
                patrol.getNotes() + "\nCancelled: " + reason : "Cancelled: " + reason);

        patrolRepository.save(patrol);
    }

    private boolean isSupervisor(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().toString().equals("ROLE_SUPERVISOR"));
    }

    @Transactional
    public PatrolDto assignSupervisor(Long patrolId, Long supervisorId) {
        Patrol patrol = patrolRepository.findById(patrolId)
                .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + patrolId));

        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with id: " + supervisorId));

        if (!isSupervisor(supervisor)) {
            throw new IllegalArgumentException("User is not a supervisor");
        }

        //patrol.setSupervisor(supervisor);
        Patrol updatedPatrol = patrolRepository.save(patrol);

        // Send notification
       // sendPatrolAssignmentNotification(updatedPatrol);

        return mapToDto(updatedPatrol);
    }

    // Add these methods to PatrolService

    @Transactional
    public PatrolAssignmentDto createAssignment(CreatePatrolAssignmentRequest request) {
        Patrol patrol = patrolRepository.findById(request.getPatrolId())
                .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + request.getPatrolId()));

        User supervisor = userRepository.findById(request.getSupervisorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getSupervisorId()));

        if (!isSupervisor(supervisor)) {
            throw new IllegalArgumentException("User is not a supervisor");
        }

        PatrolAssignment assignment = PatrolAssignment.builder()
                .patrol(patrol)
                .supervisor(supervisor)
                .status(PatrolAssignment.PatrolAssignmentStatus.PENDING)
                .assignedAt(LocalDateTime.now())
                .build();

        PatrolAssignment savedAssignment = assignmentRepository.save(assignment);

        // Send notification
        sendAssignmentNotification(savedAssignment);

        return mapToDto(savedAssignment);
    }


    @Transactional
    public PatrolAssignmentDto acceptAssignment(Long assignmentId) {
        PatrolAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (assignment.getStatus() != PatrolAssignment.PatrolAssignmentStatus.PENDING) {
            throw new IllegalStateException("Assignment can only be accepted from PENDING status");
        }

        assignment.setStatus(PatrolAssignment.PatrolAssignmentStatus.ACCEPTED);
        assignment.setAcceptedAt(LocalDateTime.now());

        // Call internal method instead of service reference
        startPatrolForAssignment(assignment.getPatrol().getId());

        return mapToDto(assignmentRepository.save(assignment));
    }

    private void startPatrolForAssignment(Long patrolId) {
        Patrol patrol = patrolRepository.findById(patrolId)
                .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + patrolId));

        if (patrol.getStatus() != Patrol.PatrolStatus.SCHEDULED) {
            throw new IllegalStateException("Patrol can only be started from SCHEDULED status");
        }

        patrol.setStatus(Patrol.PatrolStatus.IN_PROGRESS);
        patrol.setActualStartTime(LocalDateTime.now());
        patrolRepository.save(patrol);
    }

    @Transactional
    public PatrolAssignmentDto declineAssignment(Long assignmentId, String reason) {
        PatrolAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (assignment.getStatus() != PatrolAssignment.PatrolAssignmentStatus.PENDING) {
            throw new IllegalStateException("Assignment can only be declined from PENDING status");
        }

        assignment.setStatus(PatrolAssignment.PatrolAssignmentStatus.DECLINED);
        assignment.setDeclinedAt(LocalDateTime.now());
        assignment.setDeclineReason(reason);

        return mapToDto(assignmentRepository.save(assignment));
    }

    private PatrolAssignmentDto mapToDto(PatrolAssignment assignment) {
        return PatrolAssignmentDto.builder()
                .id(assignment.getId())
                .patrolId(assignment.getPatrol().getId())
                .supervisorId(assignment.getSupervisor().getId())
                .supervisorName(assignment.getSupervisor().getFirstName() + " " + assignment.getSupervisor().getLastName())
                .status(assignment.getStatus().toString())
                .assignedAt(assignment.getAssignedAt())
                .acceptedAt(assignment.getAcceptedAt())
                .declinedAt(assignment.getDeclinedAt())
                .declineReason(assignment.getDeclineReason())
                .completedAt(assignment.getCompletedAt())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .patrolDetails(mapToDto(assignment.getPatrol()))
                .build();
    }

    private void sendAssignmentNotification(PatrolAssignment assignment) {
        try {
            String siteNames = assignment.getPatrol().getSites().stream()
                    .map(Site::getName)
                    .collect(Collectors.joining(", "));

            emailService.sendAssignmentNotificationEmail(
                    assignment.getSupervisor().getEmail(),
                    assignment.getSupervisor().getFirstName() + " " + assignment.getSupervisor().getLastName(),
                    siteNames,
                    assignment.getPatrol().getStartTime(),
                    assignment.getPatrol().getEndTime(),
                    assignment.getPatrol().getNotes()
                    //assignment.getId()
            );
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send assignment notification email: " + e.getMessage());
        }
    }


    @Transactional
    public BulkPatrolAssignmentResponse createBulkPatrolAssignments(BulkPatrolAssignmentRequest request) {
        List<PatrolAssignmentDto> createdAssignments = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<Long, List<PatrolAssignment>> supervisorAssignments = new HashMap<>();

        try {
            // Validate patrol exists
            Patrol patrol = patrolRepository.findById(request.getPatrolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + request.getPatrolId()));

            // Validate and map supervisors
            Map<Long, User> supervisorMap = new HashMap<>();
            Map<Long, String> originalPatrolTypes = new HashMap<>();

            for (BulkPatrolAssignmentRequest.PatrolAssignment sa : request.getSupervisorAssignments()) {
                try {
                    User user = userRepository.findById(sa.getSupervisorId())
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + sa.getSupervisorId()));

                    if (!user.getRoles().stream().anyMatch(r -> r.getName().toString().equals("ROLE_SUPERVISOR"))) {
                        errors.add("User " + user.getUsername() + " is not a supervisor");
                        continue;
                    }

                    supervisorMap.put(sa.getSupervisorId(), user);
                    supervisorAssignments.put(user.getId(), new ArrayList<>());
                    originalPatrolTypes.put(sa.getSupervisorId(), sa.getPatrolType());
                } catch (Exception e) {
                    errors.add("Error processing supervisor ID " + sa.getSupervisorId() + ": " + e.getMessage());
                }
            }

            if (supervisorMap.isEmpty()) {
                throw new IllegalArgumentException("No valid supervisors found");
            }

            // Rotation setup
            boolean rotationEnabled = request.getRotationConfig() != null &&
                    request.getRotationConfig().isEnableRotation();
            int rotationInterval = rotationEnabled ?
                    request.getRotationConfig().getRotationIntervalWeeks() : 0;
            String rotationType = rotationEnabled ?
                    request.getRotationConfig().getRotationType() : null;

            LocalDate currentDate = request.getStartDate();
            int weekCounter = 0;

            while (!currentDate.isAfter(request.getEndDate())) {
                // Determine if we should rotate this week
                boolean shouldRotate = rotationEnabled &&
                        (rotationInterval > 0) &&
                        ((weekCounter / rotationInterval) % 2 == 1);

                if (shouldRotate) {
                    log.info("Applying rotation for week {}", weekCounter);
                }

                // Process each supervisor assignment
                for (BulkPatrolAssignmentRequest.PatrolAssignment sa : request.getSupervisorAssignments()) {
                    if (!supervisorMap.containsKey(sa.getSupervisorId())) continue;

                    User supervisor = supervisorMap.get(sa.getSupervisorId());
                    int dayOfWeek = currentDate.getDayOfWeek().getValue();

                    // Skip if not assigned for this day
                    if (sa.getDaysOfWeek() != null && !sa.getDaysOfWeek().contains(dayOfWeek)) {
                        continue;
                    }

                    try {
                        // Check for existing assignment
                        Optional<PatrolAssignment> existing = assignmentRepository
                                .findByPatrolIdAndSupervisorIdAndAssignedAtBetween(
                                        patrol.getId(),
                                        supervisor.getId(),
                                        currentDate.atStartOfDay(),
                                        currentDate.plusDays(1).atStartOfDay());

                        if (existing.isPresent() && !request.isOverrideExisting()) {
                            warnings.add("Assignment exists for " + supervisor.getUsername() +
                                    " on " + currentDate + " - skipped");
                            continue;
                        }

                        // Determine patrol type (apply rotation if needed)
                        String patrolType = sa.getPatrolType();
                        if (shouldRotate && "PATROL_TYPE".equals(rotationType)) {
                            // Find the rotated patrol type
                            String rotatedType = getRotatedPatrolType(originalPatrolTypes.get(sa.getSupervisorId()));

                            // Find another supervisor with the rotated type to get times
                            for (BulkPatrolAssignmentRequest.PatrolAssignment otherSa : request.getSupervisorAssignments()) {
                                if (rotatedType.equals(otherSa.getPatrolType())) {
                                    patrolType = rotatedType;
                                    log.info("Rotating supervisor {} from {} to {} patrol for date {}",
                                            supervisor.getUsername(), sa.getPatrolType(), rotatedType, currentDate);
                                    break;
                                }
                            }
                        }

                        // Set times (with rotation applied if needed)
                        LocalTime startTime = sa.getStartTime() != null ?
                                LocalTime.parse(sa.getStartTime()) : patrol.getStartTime().toLocalTime();
                        LocalTime endTime = sa.getEndTime() != null ?
                                LocalTime.parse(sa.getEndTime()) : patrol.getEndTime().toLocalTime();

                        // Create the assignment
                        PatrolAssignment newAssignment = new PatrolAssignment();
                        newAssignment.setPatrol(patrol);
                        newAssignment.setSupervisor(supervisor);
                        newAssignment.setAssignedAt(LocalDateTime.now());
                        newAssignment.setStatus(PatrolAssignment.PatrolAssignmentStatus.PENDING);
                        newAssignment.setPatrolType(Patrol.PatrolType.valueOf(patrolType));
                        newAssignment.setNotes(request.getNotes());

                        LocalDateTime startDateTime = LocalDateTime.of(currentDate, startTime);
                        LocalDateTime endDateTime = endTime.isBefore(startTime) ?
                                LocalDateTime.of(currentDate.plusDays(1), endTime) :
                                LocalDateTime.of(currentDate, endTime);

                        newAssignment.setStartTime(startDateTime);
                        newAssignment.setEndTime(endDateTime);

                        PatrolAssignment savedAssignment = assignmentRepository.save(newAssignment);
                        createdAssignments.add(mapToDto(savedAssignment));
                        supervisorAssignments.get(supervisor.getId()).add(savedAssignment);

                    } catch (Exception e) {
                        errors.add("Failed to create assignment for " + supervisor.getUsername() +
                                " on " + currentDate + ": " + e.getMessage());
                        log.error("Assignment creation error", e);
                    }
                }

                currentDate = currentDate.plusDays(1);

                // Increment week counter on Mondays
                if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY) {
                    weekCounter++;
                }
            }

            sendConsolidatedPatrolNotifications(supervisorAssignments);

        } catch (Exception e) {
            errors.add("System error: " + e.getMessage());
            log.error("Bulk assignment failed", e);
        }

        return BulkPatrolAssignmentResponse.builder()
                .createdAssignments(createdAssignments)
                .warnings(warnings)
                .errors(errors)
                .totalAssignmentsCreated(createdAssignments.size())
                .summary("Created " + createdAssignments.size() + " assignments with " +
                        warnings.size() + " warnings and " + errors.size() + " errors.")
                .build();
    }

    // Helper method to rotate patrol types
    private String getRotatedPatrolType(String originalType) {
        if ("MORNING".equals(originalType)) return "EVENING";
        if ("EVENING".equals(originalType)) return "MORNING";
        if ("DAY".equals(originalType)) return "NIGHT";
        if ("NIGHT".equals(originalType)) return "DAY";
        return originalType; // default return if no match
    }
    private Patrol.PatrolType rotatePatrolType(Patrol.PatrolType currentType) {
        switch (currentType) {
            case MORNING: return Patrol.PatrolType.EVENING;
            case EVENING: return Patrol.PatrolType.MORNING;
            case AFTERNOON: return Patrol.PatrolType.NIGHT;
            case NIGHT: return Patrol.PatrolType.AFTERNOON;
            default: return currentType;
        }
    }

    private LocalTime adjustTimeForPatrolType(LocalTime time, Patrol.PatrolType type) {
        switch (type) {
            case MORNING: return time.withHour(6).withMinute(0);
            case AFTERNOON: return time.withHour(12).withMinute(0);
            case EVENING: return time.withHour(18).withMinute(0);
            case NIGHT: return time.withHour(0).withMinute(0);
            default: return time;
        }
    }

    private Patrol createOrUpdatePatrolForAssignment(Patrol originalPatrol, LocalDateTime startTime, LocalDateTime endTime) {
        // For simplicity, we'll create a new patrol for each assignment
        // In a real system, you might want to reuse patrols with the same parameters

        Patrol newPatrol = new Patrol();
        newPatrol.setName(originalPatrol.getName());
        newPatrol.setDescription(originalPatrol.getDescription());
        newPatrol.setPrimarySite(originalPatrol.getPrimarySite());
        newPatrol.setSites(new HashSet<>(originalPatrol.getSites()));
        newPatrol.setStartTime(startTime);
        newPatrol.setEndTime(endTime);
        newPatrol.setPatrolType(originalPatrol.getPatrolType());
        newPatrol.setRequiredSupervisors(originalPatrol.getRequiredSupervisors());
        newPatrol.setColorCode(originalPatrol.getColorCode());
        newPatrol.setStatus(Patrol.PatrolStatus.SCHEDULED);
        newPatrol.setNotes("Assigned from bulk assignment");
        newPatrol.setActive(true);

        return patrolRepository.save(newPatrol);
    }

    private void sendConsolidatedPatrolNotifications(Map<Long, List<PatrolAssignment>> supervisorAssignments) {
        for (Map.Entry<Long, List<PatrolAssignment>> entry : supervisorAssignments.entrySet()) {
            try {
                User supervisor = userRepository.findById(entry.getKey()).orElse(null);
                if (supervisor != null && !entry.getValue().isEmpty()) {
                    // Get unique patrols for this supervisor
                    Set<Patrol> uniquePatrols = entry.getValue().stream()
                            .map(PatrolAssignment::getPatrol)
                            .collect(Collectors.toSet());

                    // Send consolidated email notification
                    emailService.sendPatrolAssignmentEmails(
                            supervisor.getEmail(),
                            supervisor.getFirstName() + " " + supervisor.getLastName(),
                            uniquePatrols.stream()
                                    .map(Patrol::getName)
                                    .collect(Collectors.joining(", ")),
                            entry.getValue().size(),
                            entry.getValue().get(0).getPatrol().getStartTime().toLocalDate(),
                            entry.getValue().get(0).getPatrol().getEndTime().toLocalDate()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to send notification to supervisor ID: " + entry.getKey(), e);
            }
        }
    }

    private PatrolDto mapToDto(Patrol patrol) {
        return PatrolDto.builder()
                .id(patrol.getId())
                .name(patrol.getName())
                .description(patrol.getDescription())
                //.supervisorId(patrol.getSupervisor() != null ? patrol.getSupervisor().getId() : null)
                //.supervisorName(patrol.getSupervisor() != null ?
                //        patrol.getSupervisor().getFirstName() + " " + patrol.getSupervisor().getLastName() : null)
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
}