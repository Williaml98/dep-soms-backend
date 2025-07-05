package com.dep.soms.service;

import com.dep.soms.dto.shift.LocationVerificationResponse;
import com.dep.soms.dto.shift.ShiftAssignmentDto;
import com.dep.soms.dto.shift.ShiftAssignmentRequest;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.Guard;
import com.dep.soms.model.Shift;
import com.dep.soms.model.ShiftAssignment;
import com.dep.soms.model.User;
import com.dep.soms.repository.GuardRepository;
import com.dep.soms.repository.ShiftAssignmentRepository;
import com.dep.soms.repository.ShiftRepository;
import com.dep.soms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import com.dep.soms.dto.shift.CheckInOutRequest;
import com.dep.soms.dto.shift.BulkShiftAssignmentRequest;
import com.dep.soms.dto.shift.BulkShiftAssignmentResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class ShiftAssignmentService {
    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @Autowired
    private ShiftRepository shiftRepository;
    private static final Logger log = LoggerFactory.getLogger(ShiftAssignmentService.class);
    @Autowired
    private GuardRepository guardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LocationService locationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private SmsService smsService;

    @Transactional(readOnly = true)
    public List<ShiftAssignmentDto> getAllShiftAssignments() {
        return shiftAssignmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShiftAssignmentDto getShiftAssignmentById(Long id) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found with id: " + id));
        return mapToDto(assignment);
    }


    @Transactional
    public List<ShiftAssignmentDto> getShiftAssignmentsByGuardId(Long guardId, LocalDate startDate, LocalDate endDate) {
        // Add this logging
        log.info("getShiftAssignmentsByGuardId called with guardId={}, startDate={}, endDate={}", guardId, startDate, endDate);

        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + guardId));

        List<ShiftAssignment> assignments;
        if (startDate != null && endDate != null) {
            log.info("Using date range query: {} to {}", startDate, endDate);
            assignments = shiftAssignmentRepository.findByGuardAndAssignmentDateBetween(guard, startDate, endDate);
        } else if (startDate != null) {
            log.info("Using start date query: {}", startDate);
            assignments = shiftAssignmentRepository.findByGuardAndAssignmentDateGreaterThanEqual(guard, startDate);
        } else if (endDate != null) {
            log.info("Using end date query: {}", endDate);
            assignments = shiftAssignmentRepository.findByGuardAndAssignmentDateLessThanEqual(guard, endDate);
        } else {
            log.info("Using findByGuard (no date filter)");
            assignments = shiftAssignmentRepository.findByGuard(guard);
        }

        log.info("Found {} assignments", assignments.size());
        return assignments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public List<ShiftAssignmentDto> getShiftAssignmentsByShiftId(Long shiftId, LocalDate date) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + shiftId));
        List<ShiftAssignment> assignments;
        if (date != null) {
            assignments = shiftAssignmentRepository.findByShiftAndAssignmentDate(shift, date);
        } else {
            assignments = shiftAssignmentRepository.findByShift(shift);
        }

        return assignments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ShiftAssignmentDto> getShiftAssignmentsBySiteId(Long siteId, LocalDate date) {
        List<ShiftAssignment> assignments;
        if (date != null) {
            assignments = shiftAssignmentRepository.findByShift_Site_IdAndAssignmentDate(siteId, date);
        } else {
            assignments = shiftAssignmentRepository.findByShift_Site_Id(siteId);
        }

        return assignments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShiftAssignmentDto createShiftAssignment(ShiftAssignmentRequest request) {
        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + request.getShiftId()));

        // Convert user ID to guard ID
        User user = userRepository.findById(request.getGuardId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getGuardId()));

        Guard guard = guardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Guard record not found for user id: " + user.getId()));

        // Verify user has GUARD role
        boolean isGuard = user.getRoles().stream()
                .anyMatch(role -> role.getName().toString().equals("ROLE_GUARD"));
        if (!isGuard) {
            throw new IllegalArgumentException("User is not a guard");
        }

        // Check if guard is already assigned to another shift at the same time
        List<ShiftAssignment> existingAssignments = shiftAssignmentRepository.findByGuardAndAssignmentDate(guard, request.getDate());

        // Use shift times if not provided in request
        LocalTime startTime = request.getStartTime() != null ? request.getStartTime() : shift.getStartTime();
        LocalTime endTime = request.getEndTime() != null ? request.getEndTime() : shift.getEndTime();

        for (ShiftAssignment existing : existingAssignments) {
            LocalTime existingStart = existing.getStartTime() != null ? LocalTime.from(existing.getStartTime()) : existing.getShift().getStartTime();
            LocalTime existingEnd = existing.getEndTime() != null ? LocalTime.from(existing.getEndTime()) : existing.getShift().getEndTime();

            // Check for time overlap
            if ((startTime.isBefore(existingEnd) && endTime.isAfter(existingStart)) ||
                    startTime.equals(existingStart) || endTime.equals(existingEnd)) {
                throw new IllegalArgumentException("Guard is already assigned to another shift at the same time");
            }
        }

        ShiftAssignment assignment = ShiftAssignment.builder()
                .shift(shift)
                .guard(guard)
                .assignmentDate(request.getDate())
                .startTime(LocalDateTime.of(request.getDate(), startTime))
                .endTime(LocalDateTime.of(request.getDate(), endTime))
                .status(request.getStatus())
                .notes(request.getNotes())
                .build();

        ShiftAssignment savedAssignment = shiftAssignmentRepository.save(assignment);

        // Send notifications to the guard about shift assignment
        sendShiftAssignmentNotifications(guard, shift, savedAssignment);

        return mapToDto(savedAssignment);
    }

    private void sendShiftAssignmentNotifications(Guard guard, Shift shift, ShiftAssignment assignment) {
        try {
            // Send email notification
            emailService.sendShiftAssignmentEmail(
                    guard.getUser().getEmail(),
                    guard.getUser().getFirstName() + " " + guard.getUser().getLastName(),
                    shift.getName(),
                    shift.getSite().getName(),
                    assignment.getAssignmentDate(),
                    LocalTime.from(assignment.getStartTime()),
                    LocalTime.from(assignment.getEndTime())
            );
        } catch (Exception e) {
            // Log the error but don't fail the assignment creation
            log.error("Failed to send notifications for shift assignment: " + e.getMessage());
        }
    }

    @Transactional
    public ShiftAssignmentDto updateShiftAssignment(Long id, ShiftAssignmentRequest request) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found with id: " + id));

        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + request.getShiftId()));

        // Convert user ID to guard ID for update as well
        User user = userRepository.findById(request.getGuardId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getGuardId()));

        Guard guard = guardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Guard record not found for user id: " + user.getId()));

        // Check if guard is already assigned to another shift at the same time (excluding this assignment)
        List<ShiftAssignment> existingAssignments = shiftAssignmentRepository.findByGuardAndAssignmentDateAndIdNot(
                guard, request.getDate(), id);

        // Use shift times if not provided in request
        LocalTime startTime = request.getStartTime() != null ? request.getStartTime() : shift.getStartTime();
        LocalTime endTime = request.getEndTime() != null ? request.getEndTime() : shift.getEndTime();

        for (ShiftAssignment existing : existingAssignments) {
            LocalTime existingStart = existing.getStartTime() != null ? LocalTime.from(existing.getStartTime()) : existing.getShift().getStartTime();
            LocalTime existingEnd = existing.getEndTime() != null ? LocalTime.from(existing.getEndTime()) : existing.getShift().getEndTime();

            // Check for time overlap
            if ((startTime.isBefore(existingEnd) && endTime.isAfter(existingStart)) ||
                    startTime.equals(existingStart) || endTime.equals(existingEnd)) {
                throw new IllegalArgumentException("Guard is already assigned to another shift at the same time");
            }
        }

        assignment.setShift(shift);
        assignment.setGuard(guard);
        assignment.setAssignmentDate(request.getDate());
        assignment.setStartTime(LocalDateTime.of(request.getDate(), startTime));
        assignment.setEndTime(LocalDateTime.of(request.getDate(), endTime));
        assignment.setStatus(request.getStatus());
        assignment.setNotes(request.getNotes());

        ShiftAssignment updatedAssignment = shiftAssignmentRepository.save(assignment);
        return mapToDto(updatedAssignment);
    }

    @Transactional
    public void deleteShiftAssignment(Long id) {
        if (!shiftAssignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shift assignment not found with id: " + id);
        }
        shiftAssignmentRepository.deleteById(id);
    }

    //TRYING TO SEE IF THE TIME FOR EACH GUARD WILL WORK
//    @Transactional
//    public BulkShiftAssignmentResponse createBulkShiftAssignments(BulkShiftAssignmentRequest request) {
//        List<ShiftAssignmentDto> createdAssignments = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//        List<String> errors = new ArrayList<>();
//        // Map to store assignments per guard for consolidated emails
//        Map<Long, List<ShiftAssignment>> guardAssignments = new HashMap<>();
//
//        try {
//            // Validate shift exists
//            Shift shift = shiftRepository.findById(request.getShiftId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + request.getShiftId()));
//
//            // Validate and convert guards
//            Map<Long, Guard> guardMap = new HashMap<>();
//            for (BulkShiftAssignmentRequest.GuardAssignment guardAssignment : request.getGuardAssignments()) {
//                try {
//                    User user = userRepository.findById(guardAssignment.getGuardId())
//                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + guardAssignment.getGuardId()));
//                    Guard guard = guardRepository.findByUserId(user.getId())
//                            .orElseThrow(() -> new ResourceNotFoundException("Guard record not found for user id: " + user.getId()));
//                    // Verify user has GUARD role
//                    boolean isGuard = user.getRoles().stream()
//                            .anyMatch(role -> role.getName().toString().equals("ROLE_GUARD"));
//                    if (!isGuard) {
//                        errors.add("User " + user.getFirstName() + " " + user.getLastName() + " is not a guard");
//                        continue;
//                    }
//                    guardMap.put(guardAssignment.getGuardId(), guard);
//                    guardAssignments.put(guard.getId(), new ArrayList<>());
//                } catch (Exception e) {
//                    errors.add("Error processing guard ID " + guardAssignment.getGuardId() + ": " + e.getMessage());
//                }
//            }
//
//            if (guardMap.isEmpty()) {
//                throw new IllegalArgumentException("No valid guards found for assignment");
//            }
//
//            // Generate assignments for the date range
//            LocalDate currentDate = request.getStartDate();
//            int weekCounter = 0;
//
//            while (!currentDate.isAfter(request.getEndDate())) {
//                // Calculate rotation if enabled
//                boolean shouldRotate = false;
//                if (request.getRotationConfig() != null && request.getRotationConfig().isEnableRotation()) {
//                    int rotationInterval = request.getRotationConfig().getRotationIntervalWeeks();
//                    shouldRotate = (weekCounter / rotationInterval) % 2 == 1;
//                }
//
//                // Process each guard assignment
//                for (BulkShiftAssignmentRequest.GuardAssignment guardAssignment : request.getGuardAssignments()) {
//                    if (!guardMap.containsKey(guardAssignment.getGuardId())) {
//                        continue; // Skip invalid guards
//                    }
//
//                    Guard guard = guardMap.get(guardAssignment.getGuardId());
//
//                    // Check if this guard should work on this day
//                    int dayOfWeek = currentDate.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
//                    if (guardAssignment.getDaysOfWeek() != null &&
//                            !guardAssignment.getDaysOfWeek().contains(dayOfWeek)) {
//                        continue; // Skip this day for this guard
//                    }
//
//                    // Apply rotation logic if enabled
//                    if (shouldRotate && request.getRotationConfig() != null) {
//                        continue; // Skip assignment during rotation
//                    }
//
//                    try {
//                        // Check for existing assignment using your repository method
//                        Optional<ShiftAssignment> existingAssignment = shiftAssignmentRepository
//                                .findByShiftIdAndGuardIdAndAssignmentDate(shift.getId(), guard.getId(), currentDate);
//
//                        if (existingAssignment.isPresent()) {
//                            warnings.add("Assignment already exists for " + guard.getUser().getFirstName() +
//                                    " " + guard.getUser().getLastName() + " on " + currentDate + " (skipped)");
//                            continue;
//                        }
//
//                        // Create new assignment
//                        ShiftAssignment newAssignment = new ShiftAssignment();
//                        newAssignment.setShift(shift);
//                        newAssignment.setGuard(guard);
//                        newAssignment.setAssignmentDate(currentDate);
//                        newAssignment.setStatus(ShiftAssignment.AssignmentStatus.SCHEDULED);
//
//                        // Use guard-specific start and end times if provided, otherwise use shift times
//                        LocalTime startTime = guardAssignment.getStartTime() != null ?
//                                guardAssignment.getStartTime() : shift.getStartTime();
//                        LocalTime endTime = guardAssignment.getEndTime() != null ?
//                                guardAssignment.getEndTime() : shift.getEndTime();
//
//                        // Handle overnight shifts correctly
//                        LocalDateTime startDateTime = LocalDateTime.of(currentDate, startTime);
//                        LocalDateTime endDateTime;
//
//                        // If end time is before or equal to start time, it means the shift ends the next day
//                        if (endTime.equals(startTime) || endTime.isBefore(startTime)) {
//                            endDateTime = LocalDateTime.of(currentDate.plusDays(1), endTime);
//                        } else {
//                            endDateTime = LocalDateTime.of(currentDate, endTime);
//                        }
//
//                        newAssignment.setStartTime(startDateTime);
//                        newAssignment.setEndTime(endDateTime);
//
//                        ShiftAssignment savedAssignment = shiftAssignmentRepository.save(newAssignment);
//                        createdAssignments.add(mapToDto(savedAssignment));
//                        guardAssignments.get(guard.getId()).add(savedAssignment);
//
//                    } catch (Exception e) {
//                        errors.add("Failed to create assignment for " + guard.getUser().getFirstName() +
//                                " " + guard.getUser().getLastName() + " on " + currentDate + ": " + e.getMessage());
//                    }
//                }
//
//                // Move to next day
//                currentDate = currentDate.plusDays(1);
//
//                // Increment week counter when we complete a week
//                if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY) {
//                    weekCounter++;
//                }
//            }
//
//            // Send consolidated email notifications
//            sendConsolidatedNotifications(guardAssignments, shift, request.getStartDate(), request.getEndDate());
//
//        } catch (Exception e) {
//            errors.add("System error: " + e.getMessage());
//        }
//
//        return BulkShiftAssignmentResponse.builder()
//                .createdAssignments(createdAssignments)
//                .warnings(warnings)
//                .errors(errors)
//                .build();
//    }
//

    @Transactional
    public BulkShiftAssignmentResponse createBulkShiftAssignments(BulkShiftAssignmentRequest request) {
        List<ShiftAssignmentDto> createdAssignments = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Map to store assignments per guard for consolidated emails
        Map<Long, List<ShiftAssignment>> guardAssignments = new HashMap<>();

        try {
            // Validate shift exists
            Shift shift = shiftRepository.findById(request.getShiftId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + request.getShiftId()));

            // Validate and convert guards
            Map<Long, Guard> guardMap = new HashMap<>();

            for (BulkShiftAssignmentRequest.GuardAssignment guardAssignment : request.getGuardAssignments()) {
                try {
                    User user = userRepository.findById(guardAssignment.getGuardId())
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + guardAssignment.getGuardId()));
                    Guard guard = guardRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Guard record not found for user id: " + user.getId()));

                    // Verify user has GUARD role
                    boolean isGuard = user.getRoles().stream()
                            .anyMatch(role -> role.getName().toString().equals("ROLE_GUARD"));
                    if (!isGuard) {
                        errors.add("User " + user.getFirstName() + " " + user.getLastName() + " is not a guard");
                        continue;
                    }

                    guardMap.put(guardAssignment.getGuardId(), guard);
                    guardAssignments.put(guard.getId(), new ArrayList<>());
                } catch (Exception e) {
                    errors.add("Error processing guard ID " + guardAssignment.getGuardId() + ": " + e.getMessage());
                }
            }

            if (guardMap.isEmpty()) {
                throw new IllegalArgumentException("No valid guards found for assignment");
            }

            // Generate assignments for the date range
            LocalDate currentDate = request.getStartDate();
            int weekCounter = 0;

            while (!currentDate.isAfter(request.getEndDate())) {
                // Calculate rotation if enabled
                boolean shouldRotate = false;
                if (request.getRotationConfig() != null && request.getRotationConfig().isEnableRotation()) {
                    int rotationInterval = request.getRotationConfig().getRotationIntervalWeeks();
                    shouldRotate = (weekCounter / rotationInterval) % 2 == 1;

                    if (shouldRotate) {
                        log.info("Applying rotation for week {}", weekCounter);
                    }
                }

                // Process each guard assignment
                for (BulkShiftAssignmentRequest.GuardAssignment guardAssignment : request.getGuardAssignments()) {
                    if (!guardMap.containsKey(guardAssignment.getGuardId())) {
                        continue; // Skip invalid guards
                    }

                    Guard guard = guardMap.get(guardAssignment.getGuardId());

                    // Check if this guard should work on this day
                    int dayOfWeek = currentDate.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
                    if (guardAssignment.getDaysOfWeek() != null &&
                            !guardAssignment.getDaysOfWeek().contains(dayOfWeek)) {
                        continue; // Skip this day for this guard
                    }

                    try {
                        // Check for existing assignment
                        Optional<ShiftAssignment> existingAssignment = shiftAssignmentRepository
                                .findByShiftIdAndGuardIdAndAssignmentDate(shift.getId(), guard.getId(), currentDate);

                        if (existingAssignment.isPresent()) {
                            warnings.add("Assignment already exists for " + guard.getUser().getFirstName() +
                                    " " + guard.getUser().getLastName() + " on " + currentDate + " (skipped)");
                            continue;
                        }

                        // Create new assignment
                        ShiftAssignment newAssignment = new ShiftAssignment();
                        newAssignment.setShift(shift);
                        newAssignment.setGuard(guard);
                        newAssignment.setAssignmentDate(currentDate);
                        newAssignment.setStatus(ShiftAssignment.AssignmentStatus.SCHEDULED);

                        // Initialize start and end times with default values
                        LocalTime startTime = guardAssignment.getStartTime() != null ?
                                guardAssignment.getStartTime() : shift.getStartTime();
                        LocalTime endTime = guardAssignment.getEndTime() != null ?
                                guardAssignment.getEndTime() : shift.getEndTime();

                        // Apply rotation if needed
                        if (shouldRotate && request.getRotationConfig() != null &&
                                ("SHIFT_TYPE".equals(request.getRotationConfig().getRotationType()) ||
                                        "BOTH".equals(request.getRotationConfig().getRotationType()))) {

                            // Get the original shift type
                            String originalShiftType = guardAssignment.getShiftType();
                            if (originalShiftType != null) {
                                // Find the shift type to rotate to
                                String rotatedShiftType = getRotatedShiftType(originalShiftType);

                                // Find a guard assignment with the rotated shift type to get times
                                for (BulkShiftAssignmentRequest.GuardAssignment otherAssignment : request.getGuardAssignments()) {
                                    if (rotatedShiftType.equals(otherAssignment.getShiftType())) {
                                        startTime = otherAssignment.getStartTime() != null ?
                                                otherAssignment.getStartTime() : shift.getStartTime();
                                        endTime = otherAssignment.getEndTime() != null ?
                                                otherAssignment.getEndTime() : shift.getEndTime();

                                        log.info("Rotating guard {} from {} to {} shift for date {}",
                                                guard.getUser().getFirstName(), originalShiftType, rotatedShiftType, currentDate);
                                        break;
                                    }
                                }
                            }
                        }

                        // Handle overnight shifts correctly
                        LocalDateTime startDateTime = LocalDateTime.of(currentDate, startTime);
                        LocalDateTime endDateTime;

                        // If end time is before or equal to start time, it means the shift ends the next day
                        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                            endDateTime = LocalDateTime.of(currentDate.plusDays(1), endTime);
                        } else {
                            endDateTime = LocalDateTime.of(currentDate, endTime);
                        }

                        newAssignment.setStartTime(startDateTime);
                        newAssignment.setEndTime(endDateTime);
                        newAssignment.setNotes(request.getNotes());

                        ShiftAssignment savedAssignment = shiftAssignmentRepository.save(newAssignment);
                        createdAssignments.add(mapToDto(savedAssignment));
                        guardAssignments.get(guard.getId()).add(savedAssignment);

                    } catch (Exception e) {
                        errors.add("Failed to create assignment for " + guard.getUser().getFirstName() +
                                " " + guard.getUser().getLastName() + " on " + currentDate + ": " + e.getMessage());
                        log.error("Error creating assignment", e);
                    }
                }

                // Move to next day
                currentDate = currentDate.plusDays(1);

                // Increment week counter when we reach Monday
                if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY) {
                    weekCounter++;
                }
            }

            // Send consolidated email notifications
            sendConsolidatedNotifications(guardAssignments, shift, request.getStartDate(), request.getEndDate());

        } catch (Exception e) {
            errors.add("System error: " + e.getMessage());
            log.error("Error in bulk shift assignment creation", e);
        }

        return BulkShiftAssignmentResponse.builder()
                .createdAssignments(createdAssignments)
                .warnings(warnings)
                .errors(errors)
                .totalAssignmentsCreated(createdAssignments.size())
                .summary("Created " + createdAssignments.size() + " assignments with " +
                        warnings.size() + " warnings and " + errors.size() + " errors.")
                .build();
    }

    // Helper method to get the rotated shift type
    private String getRotatedShiftType(String currentShiftType) {
        switch (currentShiftType) {
            case "MORNING":
                return "EVENING";
            case "EVENING":
                return "MORNING";
            case "NIGHT":
                return "DAY";
            case "DAY":
                return "NIGHT";
            default:
                return currentShiftType;
        }
    }




    private void sendConsolidatedNotifications(Map<Long, List<ShiftAssignment>> guardAssignments,
                                               Shift shift, LocalDate startDate, LocalDate endDate) {
        for (Map.Entry<Long, List<ShiftAssignment>> entry : guardAssignments.entrySet()) {
            try {
                Guard guard = guardRepository.findById(entry.getKey()).orElse(null);
                if (guard != null && !entry.getValue().isEmpty()) {
                    // Send consolidated email notification
                    emailService.sendShiftAssignmentEmail(
                            guard.getUser().getEmail(),
                            guard.getUser().getFirstName() + " " + guard.getUser().getLastName(),
                            shift.getName(),
                            shift.getSite().getName(),
                            startDate,
                            LocalTime.from(shift.getStartTime()),
                            LocalTime.from(shift.getEndTime())
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to send notification to guard ID: " + entry.getKey(), e);
            }
        }
    }
//
//    @Transactional
//    public LocationVerificationResponse checkInGuard(Long assignmentId, CheckInOutRequest request) {
//        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found with id: " + assignmentId));
//
//        // Get the site coordinates from the shift
//        Shift shift = assignment.getShift();
//        Double siteLatitude = shift.getSite().getLatitude();
//        Double siteLongitude = shift.getSite().getLongitude();
//
//        // Verify if guard is at the correct location
//        boolean isAtCorrectLocation = locationService.isWithinAllowedRadius(
//                request.getLatitude(),
//                request.getLongitude(),
//                siteLatitude,
//                siteLongitude
//        );
//
//        // Calculate actual distance
//        double distanceMeters = locationService.getDistanceInMeters(
//                request.getLatitude(),
//                request.getLongitude(),
//                siteLatitude,
//                siteLongitude
//        );
//
//        // Create appropriate message
//        String message;
//        if (isAtCorrectLocation) {
//            message = String.format("Successfully checked in at the correct location. Distance: %.1f meters from site.", distanceMeters);
//        } else {
//            message = String.format("Checked in outside allowed radius. Distance: %.1f meters from site (allowed: 500m). This has been recorded.", distanceMeters);
//        }
//
//        // Update the assignment with check-in information
//        assignment.setCheckInTime(LocalDateTime.now());
//        assignment.setCheckInLatitude(request.getLatitude());
//        assignment.setCheckInLongitude(request.getLongitude());
//        assignment.setCheckInMessage(message);
//        assignment.setCheckInLocationVerified(isAtCorrectLocation);
//        assignment.setCheckInDistanceMeters(distanceMeters);
//        assignment.setStatus(ShiftAssignment.AssignmentStatus.IN_PROGRESS);
//
//        // Add notes if provided
//        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
//            String existingNotes = assignment.getNotes() != null ? assignment.getNotes() : "";
//            assignment.setNotes(existingNotes + "\nCheck-in notes: " + request.getNotes());
//        }
//
//        ShiftAssignment updatedAssignment = shiftAssignmentRepository.save(assignment);
//
//        return LocationVerificationResponse.builder()
//                .atCorrectLocation(isAtCorrectLocation)
//                .message(message)
//                .distanceFromSite(distanceMeters)
//                .shiftAssignment(mapToDto(updatedAssignment))
//                .build();
//    }
//
//    @Transactional
//    public LocationVerificationResponse checkOutGuard(Long assignmentId, CheckInOutRequest request) {
//        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found with id: " + assignmentId));
//
//        // Get the site coordinates from the shift
//        Shift shift = assignment.getShift();
//        Double siteLatitude = shift.getSite().getLatitude();
//        Double siteLongitude = shift.getSite().getLongitude();
//
//        // Verify if guard is at the correct location
//        boolean isAtCorrectLocation = locationService.isWithinAllowedRadius(
//                request.getLatitude(),
//                request.getLongitude(),
//                siteLatitude,
//                siteLongitude
//        );
//
//        // Calculate actual distance
//        double distanceMeters = locationService.getDistanceInMeters(
//                request.getLatitude(),
//                request.getLongitude(),
//                siteLatitude,
//                siteLongitude
//        );
//
//        // Create appropriate message
//        String message;
//        if (isAtCorrectLocation) {
//            message = String.format("Successfully checked out at the correct location. Distance: %.1f meters from site.", distanceMeters);
//        } else {
//            message = String.format("Checked out outside allowed radius. Distance: %.1f meters from site (allowed: 500m). This has been recorded.", distanceMeters);
//        }
//
//        // Update the assignment with check-out information
//        assignment.setCheckOutTime(LocalDateTime.now());
//        assignment.setCheckOutLatitude(request.getLatitude());
//        assignment.setCheckOutLongitude(request.getLongitude());
//        assignment.setCheckOutMessage(message);
//        assignment.setCheckOutLocationVerified(isAtCorrectLocation);
//        assignment.setCheckOutDistanceMeters(distanceMeters);
//
//        // Update status based on check-in status
//        if (assignment.getCheckInTime() != null) {
//            assignment.setStatus(ShiftAssignment.AssignmentStatus.COMPLETED);
//        } else {
//            // Guard checked out without checking in - this is unusual but we'll mark as completed
//            assignment.setStatus(ShiftAssignment.AssignmentStatus.COMPLETED);
//        }
//
//        // Add notes if provided
//        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
//            String existingNotes = assignment.getNotes() != null ? assignment.getNotes() : "";
//            assignment.setNotes(existingNotes + "\nCheck-out notes: " + request.getNotes());
//        }
//
//        ShiftAssignment updatedAssignment = shiftAssignmentRepository.save(assignment);
//
//        return LocationVerificationResponse.builder()
//                .atCorrectLocation(isAtCorrectLocation)
//                .message(message)
//                .distanceFromSite(distanceMeters)
//                .shiftAssignment(mapToDto(updatedAssignment))
//                .build();
//    }

    // Replace your checkInGuard method with this:
//    @Transactional
//    public LocationVerificationResponse checkInGuard(Long assignmentId, CheckInOutRequest request) {
//        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found"));
//
//        Double siteLatitude = assignment.getShift().getSite().getLatitude();
//        Double siteLongitude = assignment.getShift().getSite().getLongitude();
//
//        boolean isAtCorrectLocation = locationService.isWithinAllowedRadius(
//                request.getLatitude(),
//                request.getLongitude(),
//                siteLatitude,
//                siteLongitude,
//                request.getGpsAccuracy()  // Add this parameter
//        );
//
//        if (!isAtCorrectLocation) {
//            double distance = locationService.getDistanceInMeters(
//                    request.getLatitude(), request.getLongitude(),
//                    siteLatitude, siteLongitude
//            );
//
//            log.error("Check-in failed - Distance: {:.1f}m, GPS accuracy: {}m",
//                    distance, request.getGpsAccuracy());
//
//            return new LocationVerificationResponse(false,
//                    "You are not at the correct location. Distance: " + Math.round(distance) + "m");
//        }
//
//        // Rest of your check-in logic...
//        assignment.setCheckInTime(LocalDateTime.now());
//        assignment.setCheckInLatitude(request.getLatitude());
//        assignment.setCheckInLongitude(request.getLongitude());
//        assignment.setNotes(request.getNotes());
//        assignment.setStatus(ShiftAssignment.AssignmentStatus.IN_PROGRESS);
//
//        shiftAssignmentRepository.save(assignment);
//        return new LocationVerificationResponse(true, "Check-in successful");
//    }
//
//    // Replace your checkOutGuard method with this:
//    @Transactional
//    public LocationVerificationResponse checkOutGuard(Long assignmentId, CheckInOutRequest request) {
//        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found"));
//
//        Double siteLatitude = assignment.getShift().getSite().getLatitude();
//        Double siteLongitude = assignment.getShift().getSite().getLongitude();
//
//        boolean isAtCorrectLocation = locationService.isWithinAllowedRadius(
//                request.getLatitude(),
//                request.getLongitude(),
//                siteLatitude,
//                siteLongitude,
//                request.getGpsAccuracy()  // Add this parameter
//        );
//
//        if (!isAtCorrectLocation) {
//            double distance = locationService.getDistanceInMeters(
//                    request.getLatitude(), request.getLongitude(),
//                    siteLatitude, siteLongitude
//            );
//
//            log.error("Check-out failed - Distance: {:.1f}m, GPS accuracy: {}m",
//                    distance, request.getGpsAccuracy());
//
//            return new LocationVerificationResponse(false,
//                    "You are not at the correct location. Distance: " + Math.round(distance) + "m");
//        }
//
//        // Rest of your check-out logic...
//        assignment.setCheckOutTime(LocalDateTime.now());
//        assignment.setCheckOutLatitude(request.getLatitude());
//        assignment.setCheckOutLongitude(request.getLongitude());
//        assignment.setNotes(request.getNotes());
//        assignment.setStatus(ShiftAssignment.AssignmentStatus.COMPLETED);
//
//        shiftAssignmentRepository.save(assignment);
//        return new LocationVerificationResponse(true, "Check-out successful");
//    }

    @Transactional
    public LocationVerificationResponse checkInGuard(Long assignmentId, CheckInOutRequest request) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found"));

        Double siteLatitude = assignment.getShift().getSite().getLatitude();
        Double siteLongitude = assignment.getShift().getSite().getLongitude();

        boolean isAtCorrectLocation = locationService.isWithinAllowedRadius(
                request.getLatitude(),
                request.getLongitude(),
                siteLatitude,
                siteLongitude,
                request.getGpsAccuracy()
        );

        // Set the verification flag
        assignment.setCheckInLocationVerified(isAtCorrectLocation);

        // Always record the check-in attempt
        assignment.setCheckInTime(LocalDateTime.now());
        assignment.setCheckInLatitude(request.getLatitude());
        assignment.setCheckInLongitude(request.getLongitude());
        assignment.setStatus(ShiftAssignment.AssignmentStatus.IN_PROGRESS);

        // Handle notes properly
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            String existingNotes = assignment.getNotes() != null ? assignment.getNotes() : "";
            String separator = existingNotes.isEmpty() ? "" : "\n";
            assignment.setNotes(existingNotes + separator + "Check-in notes: " + request.getNotes());
        }

        if (!isAtCorrectLocation) {
            double distance = locationService.getDistanceInMeters(
                    request.getLatitude(), request.getLongitude(),
                    siteLatitude, siteLongitude
            );

            log.warn("Check-in location verification failed - Distance: {:.1f}m, GPS accuracy: {}m, Assignment ID: {}",
                    distance, request.getGpsAccuracy(), assignmentId);

            // Add location warning to notes
            String locationNote = "\nLocation verification failed - Distance: " + Math.round(distance) + "m";
            String currentNotes = assignment.getNotes() != null ? assignment.getNotes() : "";
            assignment.setNotes(currentNotes + locationNote);

            shiftAssignmentRepository.save(assignment);

            return new LocationVerificationResponse(false,
                    "Check-in recorded with location warning. Distance: " + Math.round(distance) + "m");
        }

        shiftAssignmentRepository.save(assignment);
        return new LocationVerificationResponse(true, "Check-in successful");
    }

    @Transactional
    public LocationVerificationResponse checkOutGuard(Long assignmentId, CheckInOutRequest request) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found"));

        Double siteLatitude = assignment.getShift().getSite().getLatitude();
        Double siteLongitude = assignment.getShift().getSite().getLongitude();

        boolean isAtCorrectLocation = locationService.isWithinAllowedRadius(
                request.getLatitude(),
                request.getLongitude(),
                siteLatitude,
                siteLongitude,
                request.getGpsAccuracy()
        );

        // Set the verification flag
        assignment.setCheckOutLocationVerified(isAtCorrectLocation);

        // Always record the check-out attempt
        assignment.setCheckOutTime(LocalDateTime.now());
        assignment.setCheckOutLatitude(request.getLatitude());
        assignment.setCheckOutLongitude(request.getLongitude());
        assignment.setStatus(ShiftAssignment.AssignmentStatus.COMPLETED);

        // Handle notes properly
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            String existingNotes = assignment.getNotes() != null ? assignment.getNotes() : "";
            String separator = existingNotes.isEmpty() ? "" : "\n";
            assignment.setNotes(existingNotes + separator + "Check-out notes: " + request.getNotes());
        }

        if (!isAtCorrectLocation) {
            double distance = locationService.getDistanceInMeters(
                    request.getLatitude(), request.getLongitude(),
                    siteLatitude, siteLongitude
            );

            log.warn("Check-out location verification failed - Distance: {:.1f}m, GPS accuracy: {}m, Assignment ID: {}",
                    distance, request.getGpsAccuracy(), assignmentId);

            // Add location warning to notes
            String locationNote = "\nLocation verification failed - Distance: " + Math.round(distance) + "m";
            String currentNotes = assignment.getNotes() != null ? assignment.getNotes() : "";
            assignment.setNotes(currentNotes + locationNote);

            shiftAssignmentRepository.save(assignment);

            return new LocationVerificationResponse(false,
                    "Check-out recorded with location warning. Distance: " + Math.round(distance) + "m");
        }

        shiftAssignmentRepository.save(assignment);
        return new LocationVerificationResponse(true, "Check-out successful");
    }

    @Transactional(readOnly = true)
    public ShiftAssignmentDto getCurrentActiveShiftByGuard(Long guardId) {
        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + guardId));

        Optional<ShiftAssignment> activeShift = shiftAssignmentRepository.findCurrentInProgressShiftByGuard(guard);

        if (activeShift.isEmpty()) {
            throw new ResourceNotFoundException("No active shift found for guard with id: " + guardId);
        }

        return mapToDto(activeShift.get());
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void updateMissedShifts() {
        LocalDateTime now = LocalDateTime.now();
        List<ShiftAssignment> assignments = shiftAssignmentRepository
                .findByStatusAndEndTimeBefore(ShiftAssignment.AssignmentStatus.SCHEDULED, now);

        for (ShiftAssignment assignment : assignments) {
            assignment.setStatus(ShiftAssignment.AssignmentStatus.MISSED);
            shiftAssignmentRepository.save(assignment);
        }
    }

    // Helper method for location validation (used by scheduled task)
    private boolean isWithinRadius(Double siteLat, Double siteLon, double userLat, double userLon, double radiusMeters) {
        return locationService.getDistanceInMeters(userLat, userLon, siteLat, siteLon) <= radiusMeters;
    }

    // Keep the existing mapToDto method
    private ShiftAssignmentDto mapToDto(ShiftAssignment assignment) {
//        return ShiftAssignmentDto.builder()
//                .id(assignment.getId())
//                .shiftId(assignment.getShift().getId())
//                .shiftName(assignment.getShift().getName())
//                .guardId(assignment.getGuard().getId())
//                .guardName(assignment.getGuard().getUser().getFirstName() + " " + assignment.getGuard().getUser().getLastName())
//                .siteId(assignment.getShift().getSite().getId())
//                .siteName(assignment.getShift().getSite().getName())
//                .date(assignment.getAssignmentDate())
//                .startTime(assignment.getStartTime() != null ? LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime())
//                .endTime(assignment.getEndTime() != null ? LocalTime.from(assignment.getEndTime()) : assignment.getShift().getEndTime())
//                .status(assignment.getStatus())
//                .notes(assignment.getNotes())
//                .checkInTime(assignment.getCheckInTime() != null ? LocalTime.from(assignment.getCheckInTime()) : null)
//                .checkOutTime(assignment.getCheckOutTime() != null ? LocalTime.from(assignment.getCheckOutTime()) : null)
//                .checkInLatitude(assignment.getCheckInLatitude())
//                .checkInLongitude(assignment.getCheckInLongitude())
//                .checkOutLatitude(assignment.getCheckOutLatitude())
//                .checkOutLongitude(assignment.getCheckOutLongitude())
//                .build();

        // Create nested guard object with user
//        ShiftAssignmentDto.GuardDto guardDto = ShiftAssignmentDto.GuardDto.builder()
//                .id(assignment.getGuard().getId())
//                .user(ShiftAssignmentDto.GuardDto.UserDto.builder()
//                        .id(assignment.getGuard().getUser().getId())
//                        .firstName(assignment.getGuard().getUser().getFirstName())
//                        .lastName(assignment.getGuard().getUser().getLastName())
//                        .email(assignment.getGuard().getUser().getEmail())
//                        .profilePicture(assignment.getGuard().getUser().getProfilePicture())
//                        .build())
//                .build();
//
//        // Create nested site object
//        ShiftAssignmentDto.SiteDto siteDto = ShiftAssignmentDto.SiteDto.builder()
//                .id(assignment.getShift().getSite().getId())
//                .name(assignment.getShift().getSite().getName())
//                .build();

        // Create nested shift object
//        ShiftAssignmentDto.ShiftDto shiftDto = ShiftAssignmentDto.ShiftDto.builder()
//                .id(assignment.getShift().getId())
//                .name(assignment.getShift().getName())
//                .startTime(assignment.getShift().getStartTime())
//                .endTime(assignment.getShift().getEndTime())
//                .build();
//
//        return ShiftAssignmentDto.builder()
//                .id(assignment.getId())
//                .guard(guardDto)
//                .site(siteDto)
//                .shift(shiftDto)
//                .date(assignment.getAssignmentDate())
//                .startTime(assignment.getStartTime() != null ? LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime())
//                .endTime(assignment.getEndTime() != null ? LocalTime.from(assignment.getEndTime()) : assignment.getShift().getEndTime())
//                .status(assignment.getStatus())
//                .notes(assignment.getNotes())
//                .checkInTime(assignment.getCheckInTime() != null ? LocalTime.from(assignment.getCheckInTime()) : null)
//                .checkOutTime(assignment.getCheckOutTime() != null ? LocalTime.from(assignment.getCheckOutTime()) : null)
//                .build();
//    }

       // private ShiftAssignmentDto mapToDto(ShiftAssignment assignment) {
            // Create nested guard object with user
            ShiftAssignmentDto.GuardDto guardDto = ShiftAssignmentDto.GuardDto.builder()
                    .id(assignment.getGuard().getId())
                    .user(ShiftAssignmentDto.GuardDto.UserDto.builder()
                            .id(assignment.getGuard().getUser().getId())
                            .firstName(assignment.getGuard().getUser().getFirstName())
                            .lastName(assignment.getGuard().getUser().getLastName())
                            .email(assignment.getGuard().getUser().getEmail())
                            .profilePicture(assignment.getGuard().getUser().getProfilePicture())
                            .build())
                    .build();

            // Create nested site object
            ShiftAssignmentDto.SiteDto siteDto = ShiftAssignmentDto.SiteDto.builder()
                    .id(assignment.getShift().getSite().getId())
                    .name(assignment.getShift().getSite().getName())
                    .build();

            // Create nested shift object
            ShiftAssignmentDto.ShiftDto shiftDto = ShiftAssignmentDto.ShiftDto.builder()
                    .id(assignment.getShift().getId())
                    .name(assignment.getShift().getName())
                    .startTime(assignment.getShift().getStartTime())
                    .endTime(assignment.getShift().getEndTime())
                    .build();

            return ShiftAssignmentDto.builder()
                    .id(assignment.getId())
                    // Add these fields to maintain backward compatibility
                    .shiftId(assignment.getShift().getId())
                    .shiftName(assignment.getShift().getName())
                    .guardId(assignment.getGuard().getId())
                    .guardName(assignment.getGuard().getUser().getFirstName() + " " + assignment.getGuard().getUser().getLastName())
                    .siteId(assignment.getShift().getSite().getId())
                    .siteName(assignment.getShift().getSite().getName())
                    // Keep the nested objects too
                    .guard(guardDto)
                    .site(siteDto)
                    .shift(shiftDto)
                    .date(assignment.getAssignmentDate())
                    .startTime(assignment.getStartTime() != null ? LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime())
                    .endTime(assignment.getEndTime() != null ? LocalTime.from(assignment.getEndTime()) : assignment.getShift().getEndTime())
                    .status(assignment.getStatus())
                    .notes(assignment.getNotes())
                    .checkInTime(assignment.getCheckInTime() != null ? LocalTime.from(assignment.getCheckInTime()) : null)
                    .checkOutTime(assignment.getCheckOutTime() != null ? LocalTime.from(assignment.getCheckOutTime()) : null)
                    .checkInLatitude(assignment.getCheckInLatitude())
                    .checkInLongitude(assignment.getCheckInLongitude())
                    .checkOutLatitude(assignment.getCheckOutLatitude())
                    .checkOutLongitude(assignment.getCheckOutLongitude())
                    .build();
        }






    }
