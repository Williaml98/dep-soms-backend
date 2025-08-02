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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatrolAttendanceService {

    private static final Logger log = LoggerFactory.getLogger(PatrolAttendanceService.class);

    @Autowired
    private PatrolAssignmentRepository assignmentRepository;

    @Autowired
    private PatrolRepository patrolRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatrolPointRepository patrolPointRepository;

    @Autowired
    LocationService locationService;

    /**
     * Generate patrol attendance records for a specific supervisor within a date range
     */
    @Transactional(readOnly = true)
    public PatrolAttendanceSummaryDto getSupervisorPatrolAttendance(Long supervisorId, LocalDate startDate, LocalDate endDate) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with id: " + supervisorId));

        List<PatrolAssignment> assignments;
        if (startDate != null && endDate != null) {
            assignments = assignmentRepository.findBySupervisorAndStartTimeBetween(supervisor,
                    startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        } else if (startDate != null) {
            assignments = assignmentRepository.findBySupervisorAndStartTimeGreaterThanEqual(supervisor,
                    startDate.atStartOfDay());
        } else if (endDate != null) {
            assignments = assignmentRepository.findBySupervisorAndStartTimeLessThanEqual(supervisor,
                    endDate.plusDays(1).atStartOfDay());
        } else {
            // Default to last 30 days if no dates provided
            LocalDate defaultStartDate = LocalDate.now().minusDays(30);
            assignments = assignmentRepository.findBySupervisorAndStartTimeGreaterThanEqual(supervisor,
                    defaultStartDate.atStartOfDay());
        }

        List<PatrolAttendanceRecordDto> records = assignments.stream()
                .map(this::mapToPatrolAttendanceRecord)
                .collect(Collectors.toList());

        return createPatrolAttendanceSummary(supervisor, records);
    }

    /**
     * Generate patrol attendance records for all supervisors at a specific site within a date range
     */
    @Transactional(readOnly = true)
    public List<PatrolAttendanceSummaryDto> getSitePatrolAttendance(Long siteId, LocalDate startDate, LocalDate endDate) {
        List<PatrolAssignment> assignments;
        if (startDate != null && endDate != null) {
            assignments = assignmentRepository.findByPatrol_PrimarySite_IdAndStartTimeBetween(
                    siteId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        } else if (startDate != null) {
            assignments = assignmentRepository.findByPatrol_PrimarySite_IdAndStartTimeGreaterThanEqual(
                    siteId, startDate.atStartOfDay());
        } else if (endDate != null) {
            assignments = assignmentRepository.findByPatrol_PrimarySite_IdAndStartTimeLessThanEqual(
                    siteId, endDate.plusDays(1).atStartOfDay());
        } else {
            // Default to last 30 days if no dates provided
            LocalDate defaultStartDate = LocalDate.now().minusDays(30);
            assignments = assignmentRepository.findByPatrol_PrimarySite_IdAndStartTimeGreaterThanEqual(
                    siteId, defaultStartDate.atStartOfDay());
        }

        // Group assignments by supervisor
        Map<User, List<PatrolAssignment>> assignmentsBySupervisor = assignments.stream()
                .collect(Collectors.groupingBy(PatrolAssignment::getSupervisor));

        // Create attendance summary for each supervisor
        return assignmentsBySupervisor.entrySet().stream()
                .map(entry -> {
                    List<PatrolAttendanceRecordDto> records = entry.getValue().stream()
                            .map(this::mapToPatrolAttendanceRecord)
                            .collect(Collectors.toList());
                    return createPatrolAttendanceSummary(entry.getKey(), records);
                })
                .collect(Collectors.toList());
    }

    /**
     * Generate patrol attendance records based on custom filters
     */
    @Transactional(readOnly = true)
    public List<PatrolAttendanceSummaryDto> getPatrolAttendanceReport(PatrolAttendanceReportRequest request) {
        // Start with all assignments in the date range
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();

        List<PatrolAssignment> assignments = assignmentRepository.findByStartTimeBetween(
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        // Apply filters
        if (request.getSupervisorIds() != null && !request.getSupervisorIds().isEmpty()) {
            assignments = assignments.stream()
                    .filter(a -> request.getSupervisorIds().contains(a.getSupervisor().getId()))
                    .collect(Collectors.toList());
        }

        if (request.getSiteIds() != null && !request.getSiteIds().isEmpty()) {
            assignments = assignments.stream()
                    .filter(a -> request.getSiteIds().contains(a.getPatrol().getPrimarySite().getId()))
                    .collect(Collectors.toList());
        }

        if (request.getPatrolIds() != null && !request.getPatrolIds().isEmpty()) {
            assignments = assignments.stream()
                    .filter(a -> request.getPatrolIds().contains(a.getPatrol().getId()))
                    .collect(Collectors.toList());
        }

        // Group assignments by supervisor
        Map<User, List<PatrolAssignment>> assignmentsBySupervisor = assignments.stream()
                .collect(Collectors.groupingBy(PatrolAssignment::getSupervisor));

        // Create attendance summary for each supervisor
        return assignmentsBySupervisor.entrySet().stream()
                .map(entry -> {
                    List<PatrolAttendanceRecordDto> records = entry.getValue().stream()
                            .map(this::mapToPatrolAttendanceRecord)
                            .collect(Collectors.toList());

                    // Apply status filter if provided
                    if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                        records = records.stream()
                                .filter(r -> r.getStatus().equals(request.getStatus()))
                                .collect(Collectors.toList());
                    }

                    return createPatrolAttendanceSummary(entry.getKey(), records);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get patrol attendance for the currently logged-in user (supervisor)
     */
    @Transactional(readOnly = true)
    public PatrolAttendanceSummaryDto getMyPatrolAttendance(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return getSupervisorPatrolAttendance(user.getId(), startDate, endDate);
    }

    /**
     * Map a PatrolAssignment to an PatrolAttendanceRecordDto with attendance status calculation
     */
    private PatrolAttendanceRecordDto mapToPatrolAttendanceRecord(PatrolAssignment assignment) {
        Patrol patrol = assignment.getPatrol();

        // Calculate attendance status
        String status = calculatePatrolAttendanceStatus(assignment);

        // Calculate completion percentage
        double completionPercentage = calculateCompletionPercentage(patrol);

        // Calculate average time per checkpoint
        Duration avgCheckpointTime = calculateAverageCheckpointTime(patrol);

        return PatrolAttendanceRecordDto.builder()
                .id(assignment.getId())
                .patrolId(patrol.getId())
                .patrolName(patrol.getName())
                .supervisorId(assignment.getSupervisor().getId())
                .supervisorName(assignment.getSupervisor().getFirstName() + " " + assignment.getSupervisor().getLastName())
                .siteId(patrol.getPrimarySite().getId())
                .siteName(patrol.getPrimarySite().getName())
                .date(assignment.getStartTime().toLocalDate())
                .scheduledStartTime(assignment.getStartTime().toLocalTime())
                .scheduledEndTime(assignment.getEndTime().toLocalTime())
                .actualStartTime(patrol.getActualStartTime())
                .actualEndTime(patrol.getActualEndTime())
                .totalCheckpoints(patrol.getCheckpoints().size())
                .completedCheckpoints((int) patrol.getCheckpoints().stream()
                        .filter(cp -> cp.getCheckTime() != null)
                        .count())
                .completionPercentage(completionPercentage)
                .avgCheckpointTime(avgCheckpointTime)
                .status(status)
                .checkpoints(patrol.getCheckpoints().stream()
                        .map(this::mapToCheckpointDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private PatrolCheckpointDto mapToCheckpointDto(PatrolCheckpoint checkpoint) {
        return PatrolCheckpointDto.builder()
                .id(checkpoint.getId())
                .patrolPointId(checkpoint.getPatrolPoint().getId())
                .patrolPointName(checkpoint.getPatrolPoint().getName())
                .expectedTime(checkpoint.getPatrolPoint().getExpectedCheckTime())
                .actualTime(checkpoint.getCheckTime())
                .latitude(checkpoint.getLatitude())
                .longitude(checkpoint.getLongitude())
                .locationVerified(locationService.isWithinAllowedRadius(
                        checkpoint.getLatitude(),
                        checkpoint.getLongitude(),
                        checkpoint.getPatrolPoint().getLatitude(),
                        checkpoint.getPatrolPoint().getLongitude(),
                        50.0 // Default GPS accuracy
                ))
                .distanceFromExpected(locationService.getDistanceInMeters(
                        checkpoint.getLatitude(),
                        checkpoint.getLongitude(),
                        checkpoint.getPatrolPoint().getLatitude(),
                        checkpoint.getPatrolPoint().getLongitude()
                ))
                .notes(checkpoint.getNotes())
                .photoPath(checkpoint.getPhotoPath())
                .build();
    }

    /**
     * Calculate patrol attendance status
     */
    private String calculatePatrolAttendanceStatus(PatrolAssignment assignment) {
        Patrol patrol = assignment.getPatrol();
        LocalDateTime now = LocalDateTime.now();

        // If the patrol is in the future
        if (assignment.getStartTime().isAfter(now)) {
            return "SCHEDULED";
        }

        // If not started and the start time has passed
        if (patrol.getActualStartTime() == null && assignment.getStartTime().isBefore(now)) {
            if (assignment.getEndTime().isBefore(now)) {
                return "MISSED";
            } else {
                return "PENDING_START";
            }
        }

        // If started but not completed
        if (patrol.getActualStartTime() != null && patrol.getActualEndTime() == null) {
            if (assignment.getEndTime().isBefore(now)) {
                return "INCOMPLETE";
            } else {
                return "IN_PROGRESS";
            }
        }

        // If completed
        if (patrol.getActualEndTime() != null) {
            // Calculate completion percentage
            long totalCheckpoints = patrol.getCheckpoints().size();
            long completedCheckpoints = patrol.getCheckpoints().stream()
                    .filter(cp -> cp.getCheckTime() != null)
                    .count();

            double completionPercentage = (double) completedCheckpoints / totalCheckpoints * 100;

            if (completionPercentage < 50) {
                return "POOR_COMPLETION";
            } else if (completionPercentage < 80) {
                return "PARTIAL_COMPLETION";
            } else if (completionPercentage < 100) {
                return "NEARLY_COMPLETE";
            } else {
                return "COMPLETED";
            }
        }

        return "UNKNOWN";
    }

    /**
     * Calculate completion percentage for a patrol
     */
    private double calculateCompletionPercentage(Patrol patrol) {
        if (patrol.getCheckpoints().isEmpty()) {
            return 0.0;
        }

        long total = patrol.getCheckpoints().size();
        long completed = patrol.getCheckpoints().stream()
                .filter(cp -> cp.getCheckTime() != null)
                .count();

        return ((double) completed / total) * 100;
    }

    /**
     * Calculate average time spent per checkpoint
     */
    private Duration calculateAverageCheckpointTime(Patrol patrol) {
        if (patrol.getCheckpoints().isEmpty() || patrol.getActualStartTime() == null) {
            return Duration.ZERO;
        }

        List<LocalDateTime> checkpointTimes = patrol.getCheckpoints().stream()
                .map(PatrolCheckpoint::getCheckTime)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        if (checkpointTimes.isEmpty()) {
            return Duration.ZERO;
        }

        Duration totalDuration = Duration.ZERO;
        LocalDateTime previousTime = patrol.getActualStartTime();

        for (LocalDateTime checkpointTime : checkpointTimes) {
            totalDuration = totalDuration.plus(Duration.between(previousTime, checkpointTime));
            previousTime = checkpointTime;
        }

        return totalDuration.dividedBy(checkpointTimes.size());
    }

    /**
     * Create patrol attendance summary from a list of attendance records
     */
    private PatrolAttendanceSummaryDto createPatrolAttendanceSummary(User supervisor, List<PatrolAttendanceRecordDto> records) {
        int totalPatrols = records.size();
        int completedCount = 0;
        int incompleteCount = 0;
        int missedCount = 0;
        int poorCompletionCount = 0;
        int partialCompletionCount = 0;
        double avgCompletionPercentage = 0.0;
        Duration avgPatrolDuration = Duration.ZERO;

        Map<String, Integer> statusCounts = new HashMap<>();

        for (PatrolAttendanceRecordDto record : records) {
            // Count by status
            statusCounts.merge(record.getStatus(), 1, Integer::sum);

            // Count specific categories
            switch (record.getStatus()) {
                case "COMPLETED":
                    completedCount++;
                    break;
                case "INCOMPLETE":
                    incompleteCount++;
                    break;
                case "MISSED":
                    missedCount++;
                    break;
                case "POOR_COMPLETION":
                    poorCompletionCount++;
                    break;
                case "PARTIAL_COMPLETION":
                    partialCompletionCount++;
                    break;
            }

            // Add to average completion percentage
            avgCompletionPercentage += record.getCompletionPercentage();

            // Add to average duration if patrol was completed
            if (record.getActualStartTime() != null && record.getActualEndTime() != null) {
                avgPatrolDuration = avgPatrolDuration.plus(
                        Duration.between(record.getActualStartTime(), record.getActualEndTime()));
            }
        }

        // Calculate averages
        if (totalPatrols > 0) {
            avgCompletionPercentage /= totalPatrols;
            avgPatrolDuration = avgPatrolDuration.dividedBy(totalPatrols);
        }

        return PatrolAttendanceSummaryDto.builder()
                .supervisorId(supervisor.getId())
                .supervisorName(supervisor.getFirstName() + " " + supervisor.getLastName())
                .totalPatrols(totalPatrols)
                .completedCount(completedCount)
                .incompleteCount(incompleteCount)
                .missedCount(missedCount)
                .poorCompletionCount(poorCompletionCount)
                .partialCompletionCount(partialCompletionCount)
                .avgCompletionPercentage(avgCompletionPercentage)
                .avgPatrolDuration(avgPatrolDuration)
                .statusCounts(statusCounts)
                .records(records)
                .build();
    }
}