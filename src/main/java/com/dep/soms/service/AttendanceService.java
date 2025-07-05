package com.dep.soms.service;

import com.dep.soms.dto.attendance.AttendanceRecordDto;
import com.dep.soms.dto.attendance.AttendanceReportRequest;
import com.dep.soms.dto.attendance.AttendanceSummaryDto;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.Guard;
import com.dep.soms.model.ShiftAssignment;
import com.dep.soms.model.User;
import com.dep.soms.repository.GuardRepository;
import com.dep.soms.repository.ShiftAssignmentRepository;
import com.dep.soms.repository.UserRepository;
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
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Generate attendance records for a specific guard within a date range
     */
    @Transactional(readOnly = true)
    public AttendanceSummaryDto getGuardAttendance(Long guardId, LocalDate startDate, LocalDate endDate) {
        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + guardId));

        List<ShiftAssignment> assignments;
        if (startDate != null && endDate != null) {
            assignments = shiftAssignmentRepository.findByGuardAndAssignmentDateBetween(guard, startDate, endDate);
        } else if (startDate != null) {
            assignments = shiftAssignmentRepository.findByGuardAndAssignmentDateGreaterThanEqual(guard, startDate);
        } else if (endDate != null) {
            assignments = shiftAssignmentRepository.findByGuardAndAssignmentDateLessThanEqual(guard, endDate);
        } else {
            // Default to last 30 days if no dates provided
            LocalDate defaultStartDate = LocalDate.now().minusDays(30);
            assignments = shiftAssignmentRepository.findByGuardAndAssignmentDateGreaterThanEqual(guard, defaultStartDate);
        }

        List<AttendanceRecordDto> records = assignments.stream()
                .map(this::mapToAttendanceRecord)
                .collect(Collectors.toList());

        return createAttendanceSummary(guard, records);
    }

    /**
     * Generate attendance records for all guards at a specific site within a date range
     */
    @Transactional(readOnly = true)
    public List<AttendanceSummaryDto> getSiteAttendance(Long siteId, LocalDate startDate, LocalDate endDate) {
        List<ShiftAssignment> assignments;
        if (startDate != null && endDate != null) {
            assignments = shiftAssignmentRepository.findByShift_Site_IdAndAssignmentDateBetween(siteId, startDate, endDate);
        } else if (startDate != null) {
            assignments = shiftAssignmentRepository.findByShift_Site_IdAndAssignmentDateGreaterThanEqual(siteId, startDate);
        } else if (endDate != null) {
            assignments = shiftAssignmentRepository.findByShift_Site_IdAndAssignmentDateLessThanEqual(siteId, endDate);
        } else {
            //Default to last 30 days if no dates provided
            LocalDate defaultStartDate = LocalDate.now().minusDays(30);
            assignments = shiftAssignmentRepository.findByShift_Site_IdAndAssignmentDateGreaterThanEqual(siteId, defaultStartDate);
        }

        // Group assignments by guard
        Map<Guard, List<ShiftAssignment>> assignmentsByGuard = assignments.stream()
                .collect(Collectors.groupingBy(ShiftAssignment::getGuard));

        // Create attendance summary for each guard
        return assignmentsByGuard.entrySet().stream()
                .map(entry -> {
                    List<AttendanceRecordDto> records = entry.getValue().stream()
                            .map(this::mapToAttendanceRecord)
                            .collect(Collectors.toList());
                    return createAttendanceSummary(entry.getKey(), records);
                })
                .collect(Collectors.toList());
    }

    /**
     * Generate attendance records based on custom filters
     */
    @Transactional(readOnly = true)
    public List<AttendanceSummaryDto> getAttendanceReport(AttendanceReportRequest request) {
        // Start with all assignments in the date range
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();

        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByAssignmentDateBetween(startDate, endDate);

        // Apply filters
        if (request.getGuardIds() != null && !request.getGuardIds().isEmpty()) {
            assignments = assignments.stream()
                    .filter(a -> request.getGuardIds().contains(a.getGuard().getId()))
                    .collect(Collectors.toList());
        }

        if (request.getSiteIds() != null && !request.getSiteIds().isEmpty()) {
            assignments = assignments.stream()
                    .filter(a -> request.getSiteIds().contains(a.getShift().getSite().getId()))
                    .collect(Collectors.toList());
        }

        if (request.getShiftIds() != null && !request.getShiftIds().isEmpty()) {
            assignments = assignments.stream()
                    .filter(a -> request.getShiftIds().contains(a.getShift().getId()))
                    .collect(Collectors.toList());
        }

        // Group assignments by guard
        Map<Guard, List<ShiftAssignment>> assignmentsByGuard = assignments.stream()
                .collect(Collectors.groupingBy(ShiftAssignment::getGuard));

        // Create attendance summary for each guard
        return assignmentsByGuard.entrySet().stream()
                .map(entry -> {
                    List<AttendanceRecordDto> records = entry.getValue().stream()
                            .map(this::mapToAttendanceRecord)
                            .collect(Collectors.toList());

                    // Apply status filter if provided
                    if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                        records = records.stream()
                                .filter(r -> r.getStatus().equals(request.getStatus()))
                                .collect(Collectors.toList());
                    }

                    return createAttendanceSummary(entry.getKey(), records);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get attendance for the currently logged-in user (guard)
     */
    @Transactional(readOnly = true)
    public AttendanceSummaryDto getMyAttendance(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Guard guard = guardRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Guard record not found for user id: " + userId));

        return getGuardAttendance(guard.getId(), startDate, endDate);
    }

    /**
     * Map a ShiftAssignment to an AttendanceRecordDto with attendance status calculation
     */
    private AttendanceRecordDto mapToAttendanceRecord(ShiftAssignment assignment) {
        // Extract scheduled times
        LocalTime scheduledStartTime = LocalTime.from(assignment.getStartTime());
        LocalTime scheduledEndTime = LocalTime.from(assignment.getEndTime());

        // Calculate attendance status
        String status = calculateAttendanceStatus(assignment);

        // Calculate hours worked
        Duration hoursWorked = calculateHoursWorked(assignment);

        // Calculate lateness and early departure
        Long lateByMinutes = calculateLateMinutes(assignment);
        Long leftEarlyByMinutes = calculateLeftEarlyMinutes(assignment);

        return AttendanceRecordDto.builder()
                .id(assignment.getId())
                .shiftAssignmentId(assignment.getId())
                .guardId(assignment.getGuard().getId())
                .guardName(assignment.getGuard().getUser().getFirstName() + " " + assignment.getGuard().getUser().getLastName())
                .siteId(assignment.getShift().getSite().getId())
                .siteName(assignment.getShift().getSite().getName())
                .shiftId(assignment.getShift().getId())
                .shiftName(assignment.getShift().getName())
                .date(assignment.getAssignmentDate())
                .scheduledStartTime(scheduledStartTime)
                .scheduledEndTime(scheduledEndTime)
                .checkInTime(assignment.getCheckInTime())
                .checkOutTime(assignment.getCheckOutTime())
                .checkInLocationVerified(assignment.getCheckInLocationVerified() != null ? assignment.getCheckInLocationVerified() : false)
                .checkOutLocationVerified(assignment.getCheckOutLocationVerified() != null ? assignment.getCheckOutLocationVerified() : false)
                .hoursWorked(hoursWorked)
                .status(status)
                .lateByMinutes(lateByMinutes)
                .leftEarlyByMinutes(leftEarlyByMinutes)
                .checkInLatitude(assignment.getCheckInLatitude())
                .checkInLongitude(assignment.getCheckInLongitude())
                .checkOutLatitude(assignment.getCheckOutLatitude())
                .checkOutLongitude(assignment.getCheckOutLongitude())
                .notes(assignment.getNotes())
                .build();
    }

    /**
     * Calculate attendance status based on check-in/out times
     */
    private String calculateAttendanceStatus(ShiftAssignment assignment) {
        LocalDateTime scheduledStart = assignment.getStartTime();
        LocalDateTime scheduledEnd = assignment.getEndTime();
        LocalDateTime now = LocalDateTime.now();

        // If the shift is in the future
        if (scheduledStart.isAfter(now)) {
            return "SCHEDULED";
        }

        // If no check-in and the shift has started
        if (assignment.getCheckInTime() == null && scheduledStart.isBefore(now)) {
            if (scheduledEnd.isBefore(now)) {
                return "ABSENT";
            } else {
                return "PENDING";
            }
        }

        // If checked in but not checked out
        if (assignment.getCheckInTime() != null && assignment.getCheckOutTime() == null) {
            if (scheduledEnd.isBefore(now)) {
                return "INCOMPLETE";
            } else {
                return "IN_PROGRESS";
            }
        }

        // If both check-in and check-out exist
        if (assignment.getCheckInTime() != null && assignment.getCheckOutTime() != null) {
            boolean isLate = assignment.getCheckInTime().isAfter(scheduledStart.plusMinutes(15));
            boolean isLeftEarly = assignment.getCheckOutTime().isBefore(scheduledEnd.minusMinutes(15));

            if (isLate && isLeftEarly) {
                return "LATE_AND_LEFT_EARLY";
            } else if (isLate) {
                return "LATE";
            } else if (isLeftEarly) {
                return "LEFT_EARLY";
            } else {
                return "PRESENT";
            }
        }

        return "UNKNOWN";
    }

    /**
     * Calculate hours worked based on check-in/out times
     */
    private Duration calculateHoursWorked(ShiftAssignment assignment) {
        if (assignment.getCheckInTime() == null || assignment.getCheckOutTime() == null) {
            return Duration.ZERO;
        }

        return Duration.between(assignment.getCheckInTime(), assignment.getCheckOutTime());
    }

    /**
     * Calculate minutes late for check-in
     */
    private Long calculateLateMinutes(ShiftAssignment assignment) {
        if (assignment.getCheckInTime() == null) {
            return 0L;
        }

        LocalDateTime scheduledStart = assignment.getStartTime();
        if (assignment.getCheckInTime().isAfter(scheduledStart)) {
            return Duration.between(scheduledStart, assignment.getCheckInTime()).toMinutes();
        }

        return 0L;
    }

    /**
     * Calculate minutes left early for check-out
     */
    private Long calculateLeftEarlyMinutes(ShiftAssignment assignment) {
        if (assignment.getCheckOutTime() == null) {
            return 0L;
        }

        LocalDateTime scheduledEnd = assignment.getEndTime();
        if (assignment.getCheckOutTime().isBefore(scheduledEnd)) {
            return Duration.between(assignment.getCheckOutTime(), scheduledEnd).toMinutes();
        }

        return 0L;
    }

    /**
     * Create attendance summary from a list of attendance records
     */
    private AttendanceSummaryDto createAttendanceSummary(Guard guard, List<AttendanceRecordDto> records) {
        int totalShifts = records.size();
        int presentCount = 0;
        int absentCount = 0;
        int lateCount = 0;
        int leftEarlyCount = 0;
        int incompleteCount = 0;
        Duration totalHoursWorked = Duration.ZERO;

        Map<String, Integer> statusCounts = new HashMap<>();

        for (AttendanceRecordDto record : records) {
            // Count by status
            statusCounts.merge(record.getStatus(), 1, Integer::sum);

            // Count specific categories
            switch (record.getStatus()) {
                case "PRESENT":
                    presentCount++;
                    break;
                case "ABSENT":
                    absentCount++;
                    break;
                case "LATE":
                case "LATE_AND_LEFT_EARLY":
                    lateCount++;
                    if (record.getStatus().equals("LATE_AND_LEFT_EARLY")) {
                        leftEarlyCount++;
                    }
                    break;
                case "LEFT_EARLY":
                    leftEarlyCount++;
                    break;
                case "INCOMPLETE":
                    incompleteCount++;
                    break;
            }

            // Add to total hours worked
            totalHoursWorked = totalHoursWorked.plus(record.getHoursWorked());
        }

        return AttendanceSummaryDto.builder()
                .guardId(guard.getId())
                .guardName(guard.getUser().getFirstName() + " " + guard.getUser().getLastName())
                .totalShifts(totalShifts)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateCount(lateCount)
                .leftEarlyCount(leftEarlyCount)
                .incompleteCount(incompleteCount)
                .totalHoursWorked(totalHoursWorked)
                .statusCounts(statusCounts)
                .records(records)
                .build();
    }
}
