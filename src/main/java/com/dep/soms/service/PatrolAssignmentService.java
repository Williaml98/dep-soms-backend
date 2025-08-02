

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
@Transactional
public class PatrolAssignmentService {
    private final PatrolRepository patrolRepository;
    private final UserRepository userRepository;
    private final PatrolAssignmentRepository assignmentRepository;
    private final EmailService emailService;

    private static final Logger log = LoggerFactory.getLogger(PatrolAssignmentService.class);

    @Autowired
    public PatrolAssignmentService(PatrolRepository patrolRepository,
                                   UserRepository userRepository,
                                   PatrolAssignmentRepository assignmentRepository,
                                   EmailService emailService) {
        this.patrolRepository = patrolRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<PatrolAssignmentDto> getAllPatrolAssignments() {
        return assignmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public PatrolAssignmentDto createAssignment(CreatePatrolAssignmentRequest request) {
        Patrol patrol = patrolRepository.findById(request.getPatrolId())
                .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + request.getPatrolId()));

        User supervisor = userRepository.findById(request.getSupervisorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getSupervisorId()));

        if (!isSupervisor(supervisor)) {
            throw new IllegalArgumentException("User is not a supervisor");
        }

        Optional<PatrolAssignment> existingAssignment = assignmentRepository
                .findByPatrolIdAndSupervisorIdAndAssignmentDate(
                        patrol.getId(),
                        supervisor.getId(),
                        request.getAssignmentDate()
                );

        if (existingAssignment.isPresent()) {
            throw new IllegalArgumentException("Assignment already exists for this supervisor on the selected date");
        }

        LocalTime startTime = request.getStartTime() != null ?
                request.getStartTime() : patrol.getStartTime().toLocalTime();
        LocalTime endTime = request.getEndTime() != null ?
                request.getEndTime() : patrol.getEndTime().toLocalTime();

        LocalDateTime startDateTime = LocalDateTime.of(request.getAssignmentDate(), startTime);
        LocalDateTime endDateTime = endTime.isBefore(startTime) ?
                LocalDateTime.of(request.getAssignmentDate().plusDays(1), endTime) :
                LocalDateTime.of(request.getAssignmentDate(), endTime);

        PatrolAssignment assignment = PatrolAssignment.builder()
                .patrol(patrol)
                .supervisor(supervisor)
                .status(PatrolAssignment.PatrolAssignmentStatus.PENDING)
                .assignedAt(LocalDateTime.now())
                .assignmentDate(request.getAssignmentDate())
                .startTime(startDateTime)
                .endTime(endDateTime)
                .notes(request.getNotes())
                .patrolType(patrol.getPatrolType())
                .build();

        PatrolAssignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Created patrol assignment ID: {} for date: {}", savedAssignment.getId(), request.getAssignmentDate());

        sendAssignmentNotification(savedAssignment);
        return mapToDto(savedAssignment);
    }

    @Transactional
    public BulkPatrolAssignmentResponse createBulkPatrolAssignments(BulkPatrolAssignmentRequest request) {
        List<PatrolAssignmentDto> createdAssignments = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<Long, List<PatrolAssignment>> supervisorAssignments = new HashMap<>();

        try {
            Patrol patrol = patrolRepository.findById(request.getPatrolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patrol not found with id: " + request.getPatrolId()));

            Map<Long, User> supervisorMap = validateAndMapSupervisors(request, errors);

            if (supervisorMap.isEmpty()) {
                throw new IllegalArgumentException("No valid supervisors found for assignment");
            }

            log.info("Creating bulk patrol assignments from {} to {}", request.getStartDate(), request.getEndDate());

            LocalDate currentDate = request.getStartDate();
            int weekCounter = 0;

            while (!currentDate.isAfter(request.getEndDate())) {
                boolean shouldRotate = false;
                if (request.getRotationConfig() != null && request.getRotationConfig().isEnableRotation()) {
                    int rotationInterval = request.getRotationConfig().getRotationIntervalWeeks();
                    shouldRotate = (weekCounter / rotationInterval) % 2 == 1;
                }

                for (BulkPatrolAssignmentRequest.PatrolAssignment pa : request.getSupervisorAssignments()) {
                    if (!supervisorMap.containsKey(pa.getSupervisorId())) {
                        continue;
                    }

                    User supervisor = supervisorMap.get(pa.getSupervisorId());
                    int dayOfWeek = currentDate.getDayOfWeek().getValue();
                    if (pa.getDaysOfWeek() != null && !pa.getDaysOfWeek().contains(dayOfWeek)) {
                        continue;
                    }

                    try {
                        Optional<PatrolAssignment> existing = assignmentRepository
                                .findByPatrolIdAndSupervisorIdAndAssignmentDate(
                                        patrol.getId(),
                                        supervisor.getId(),
                                        currentDate);

                        if (existing.isPresent() && !request.isOverrideExisting()) {
                            warnings.add("Patrol assignment already exists for " + supervisor.getUsername() +
                                    " on " + currentDate + " (skipped)");
                            continue;
                        }

                        PatrolAssignment newAssignment = createPatrolAssignmentForDate(
                                patrol,
                                supervisor,
                                currentDate,
                                pa,
                                request.getNotes(),
                                shouldRotate,
                                request.getRotationConfig(),
                                request.getSupervisorAssignments()
                        );

                        PatrolAssignment savedAssignment = assignmentRepository.save(newAssignment);
                        createdAssignments.add(mapToDto(savedAssignment));
                        supervisorAssignments
                                .computeIfAbsent(supervisor.getId(), k -> new ArrayList<>())
                                .add(savedAssignment);

                        log.debug("Created patrol assignment for {} on {}", supervisor.getUsername(), currentDate);

                    } catch (Exception e) {
                        errors.add("Failed to create patrol assignment for " + supervisor.getUsername() +
                                " on " + currentDate + ": " + e.getMessage());
                        log.error("Patrol assignment creation error", e);
                    }
                }

                currentDate = currentDate.plusDays(1);
                if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY) {
                    weekCounter++;
                }
            }

            sendConsolidatedPatrolNotifications(supervisorAssignments);

        } catch (Exception e) {
            errors.add("System error: " + e.getMessage());
            log.error("Bulk patrol assignment failed", e);
        }

        return buildBulkAssignmentResponse(createdAssignments, warnings, errors);
    }

    private PatrolAssignment createPatrolAssignmentForDate(
            Patrol patrol,
            User supervisor,
            LocalDate assignmentDate,
            BulkPatrolAssignmentRequest.PatrolAssignment pa,
            String notes,
            boolean shouldRotate,
            BulkPatrolAssignmentRequest.RotationConfig rotationConfig,
            List<BulkPatrolAssignmentRequest.PatrolAssignment> allAssignments) {

        LocalTime startTime = patrol.getStartTime().toLocalTime();
        LocalTime endTime = patrol.getEndTime().toLocalTime();

        if (pa.getStartTime() != null) {
            startTime = LocalTime.parse(pa.getStartTime());
        }
        if (pa.getEndTime() != null) {
            endTime = LocalTime.parse(pa.getEndTime());
        }

        Patrol.PatrolType patrolType = pa.getPatrolType() != null ?
                Patrol.PatrolType.valueOf(pa.getPatrolType()) : patrol.getPatrolType();

        if (shouldRotate && rotationConfig != null) {
            if ("PATROL_TYPE".equals(rotationConfig.getRotationType())) {
                Patrol.PatrolType rotatedPatrolType = getRotatedPatrolType(patrolType);

                for (BulkPatrolAssignmentRequest.PatrolAssignment otherAssignment : allAssignments) {
                    if (rotatedPatrolType.toString().equals(otherAssignment.getPatrolType())) {
                        startTime = otherAssignment.getStartTime() != null ?
                                LocalTime.parse(otherAssignment.getStartTime()) : patrol.getStartTime().toLocalTime();
                        endTime = otherAssignment.getEndTime() != null ?
                                LocalTime.parse(otherAssignment.getEndTime()) : patrol.getEndTime().toLocalTime();
                        break;
                    }
                }
                patrolType = rotatedPatrolType;
            }
        }

        LocalDateTime startDateTime = LocalDateTime.of(assignmentDate, startTime);
        LocalDateTime endDateTime = endTime.isBefore(startTime) ?
                LocalDateTime.of(assignmentDate.plusDays(1), endTime) :
                LocalDateTime.of(assignmentDate, endTime);

        return PatrolAssignment.builder()
                .patrol(patrol)
                .supervisor(supervisor)
                .assignedAt(LocalDateTime.now())
                .assignmentDate(assignmentDate)
                .status(PatrolAssignment.PatrolAssignmentStatus.PENDING)
                .patrolType(patrolType)
                .notes(notes)
                .startTime(startDateTime)
                .endTime(endDateTime)
                .build();
    }

    private Patrol.PatrolType getRotatedPatrolType(Patrol.PatrolType currentPatrolType) {
        if (currentPatrolType == null) return Patrol.PatrolType.DAY;

        switch (currentPatrolType) {
            case MORNING: return Patrol.PatrolType.EVENING;
            case EVENING: return Patrol.PatrolType.MORNING;
            case NIGHT: return Patrol.PatrolType.DAY;
            case DAY: return Patrol.PatrolType.NIGHT;
            default: return currentPatrolType;
        }
    }

    private Map<Long, User> validateAndMapSupervisors(BulkPatrolAssignmentRequest request, List<String> errors) {
        Map<Long, User> supervisorMap = new HashMap<>();

        for (BulkPatrolAssignmentRequest.PatrolAssignment pa : request.getSupervisorAssignments()) {
            try {
                User user = userRepository.findById(pa.getSupervisorId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + pa.getSupervisorId()));

                if (!isSupervisor(user)) {
                    errors.add("User " + user.getUsername() + " is not a supervisor");
                    continue;
                }

                supervisorMap.put(pa.getSupervisorId(), user);
            } catch (Exception e) {
                errors.add("Error processing supervisor ID " + pa.getSupervisorId() + ": " + e.getMessage());
            }
        }
        return supervisorMap;
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
                    assignment.getStartTime(),
                    assignment.getEndTime(),
                    assignment.getPatrol().getNotes()
            );
        } catch (Exception e) {
            log.error("Failed to send patrol assignment notification email", e);
        }
    }

    private void sendConsolidatedPatrolNotifications(Map<Long, List<PatrolAssignment>> supervisorAssignments) {
        for (Map.Entry<Long, List<PatrolAssignment>> entry : supervisorAssignments.entrySet()) {
            if (entry.getValue().isEmpty()) continue;

            try {
                User supervisor = userRepository.findById(entry.getKey())
                        .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found"));

                LocalDate earliestDate = entry.getValue().stream()
                        .map(PatrolAssignment::getAssignmentDate)
                        .min(LocalDate::compareTo)
                        .orElseThrow();

                LocalDate latestDate = entry.getValue().stream()
                        .map(PatrolAssignment::getAssignmentDate)
                        .max(LocalDate::compareTo)
                        .orElseThrow();

                emailService.sendPatrolAssignmentEmails(
                        supervisor.getEmail(),
                        supervisor.getFirstName() + " " + supervisor.getLastName(),
                        entry.getValue().get(0).getPatrol().getName(),
                        entry.getValue().size(),
                        earliestDate,
                        latestDate
                );
            } catch (Exception e) {
                log.warn("Failed to send notification to supervisor ID: " + entry.getKey(), e);
            }
        }
    }

    private BulkPatrolAssignmentResponse buildBulkAssignmentResponse(
            List<PatrolAssignmentDto> createdAssignments,
            List<String> warnings,
            List<String> errors) {

        return BulkPatrolAssignmentResponse.builder()
                .createdAssignments(createdAssignments)
                .warnings(warnings)
                .errors(errors)
                .totalAssignmentsCreated(createdAssignments.size())
                .summary("Created " + createdAssignments.size() + " patrol assignments with " +
                        warnings.size() + " warnings and " + errors.size() + " errors.")
                .build();
    }

    @Transactional
    public PatrolAssignmentDto updateAssignment(Long id, UpdatePatrolAssignmentRequest request) {
        PatrolAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patrol assignment not found with id: " + id));

        User supervisor = userRepository.findById(request.getSupervisorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getSupervisorId()));

        if (!isSupervisor(supervisor)) {
            throw new IllegalArgumentException("User is not a supervisor");
        }

        LocalTime startTime = request.getStartTime() != null ?
                request.getStartTime() : assignment.getStartTime().toLocalTime();
        LocalTime endTime = request.getEndTime() != null ?
                request.getEndTime() : assignment.getEndTime().toLocalTime();

        LocalDateTime startDateTime = LocalDateTime.of(request.getAssignmentDate(), startTime);
        LocalDateTime endDateTime = endTime.isBefore(startTime) ?
                LocalDateTime.of(request.getAssignmentDate().plusDays(1), endTime) :
                LocalDateTime.of(request.getAssignmentDate(), endTime);

        assignment.setSupervisor(supervisor);
        assignment.setAssignmentDate(request.getAssignmentDate());
        assignment.setStartTime(startDateTime);
        assignment.setEndTime(endDateTime);
        assignment.setNotes(request.getNotes());
        assignment.setStatus(PatrolAssignment.PatrolAssignmentStatus.valueOf(request.getStatus()));

        PatrolAssignment updatedAssignment = assignmentRepository.save(assignment);
        log.info("Updated patrol assignment ID: {}, new date: {}", updatedAssignment.getId(), request.getAssignmentDate());

        return mapToDto(updatedAssignment);
    }

    @Transactional
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patrol assignment not found with id: " + id);
        }
        assignmentRepository.deleteById(id);
    }

    private boolean isSupervisor(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().toString().equals("ROLE_SUPERVISOR"));
    }

    private PatrolAssignmentDto mapToDto(PatrolAssignment assignment) {
        return PatrolAssignmentDto.builder()
                .id(assignment.getId())
                .patrolId(assignment.getPatrol().getId())
                .supervisorId(assignment.getSupervisor().getId())
                .supervisorName(assignment.getSupervisor().getFirstName() + " " + assignment.getSupervisor().getLastName())
                .status(assignment.getStatus().toString())
                .assignedAt(assignment.getAssignedAt())
                .assignmentDate(assignment.getAssignmentDate())
                .acceptedAt(assignment.getAcceptedAt())
                .declinedAt(assignment.getDeclinedAt())
                .declineReason(assignment.getDeclineReason())
                .completedAt(assignment.getCompletedAt())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                //.startTime(assignment.getStartTime())
                //.endTime(assignment.getEndTime())
                //.notes(assignment.getNotes())
                .patrolDetails(mapToPatrolDto(assignment.getPatrol()))
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
                .patrolType(patrol.getPatrolType().toString())
                .status(patrol.getStatus().toString())
                .build();
    }
}