//package com.dep.soms.service;
//
//import com.dep.soms.model.ShiftAssignment;
//import com.dep.soms.repository.ShiftAssignmentRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.List;
//
//@Service
//@Slf4j
//@ConditionalOnProperty(name = "app.scheduling.enable", havingValue = "true", matchIfMissing = true)
//public class AttendanceSchedulerService {
//
//    @Autowired
//    private ShiftAssignmentRepository shiftAssignmentRepository;
//
//    /**
//     * Run every day at 23:59 to mark absent guards for the day
//     */
//    @Scheduled(cron = "0 59 23 * * *")
//    @Transactional
//    public void markAbsentGuards() {
//        log.info("Running scheduled task to mark absent guards");
//        LocalDate today = LocalDate.now();
//        processAttendanceForDate(today);
//    }
//
//    /**
//     * Manual trigger method for testing or administrative purposes
//     */
//    @Transactional
//    public void manuallyMarkAttendance(LocalDate date) {
//        log.info("Manually running attendance marking for date: {}", date);
//        processAttendanceForDate(date);
//    }
//
//    /**
//     * Common method to process attendance for a specific date
//     */
//    private void processAttendanceForDate(LocalDate date) {
//        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
//
//        // Find all assignments for the date that haven't been checked in
//        List<ShiftAssignment> missedAssignments = shiftAssignmentRepository.findByAssignmentDateAndCheckInTimeIsNullAndEndTimeBefore(
//                date, endOfDay);
//
//        log.info("Found {} missed assignments for {}", missedAssignments.size(), date);
//
//        // Process in batches to avoid excessive logging and improve performance
//        int absentCount = 0;
//        if (!missedAssignments.isEmpty()) {
//            for (ShiftAssignment assignment : missedAssignments) {
//                if (assignment.getStatus() != ShiftAssignment.AssignmentStatus.ABSENT) {
//                    assignment.setStatus(ShiftAssignment.AssignmentStatus.ABSENT);
//                    shiftAssignmentRepository.save(assignment);
//                    absentCount++;
//                    log.info("Marked guard {} as ABSENT for shift {} at site {}",
//                            assignment.getGuard().getUser().getUsername(),
//                            assignment.getShift().getName(),
//                            assignment.getShift().getSite().getName());
//                }
//            }
//            log.info("Marked {} assignments as ABSENT", absentCount);
//        }
//
//        // Find all assignments that were checked in but not checked out
//        List<ShiftAssignment> incompleteAssignments = shiftAssignmentRepository.findByAssignmentDateAndCheckInTimeIsNotNullAndCheckOutTimeIsNullAndEndTimeBefore(
//                date, endOfDay);
//
//        log.info("Found {} incomplete assignments for {}", incompleteAssignments.size(), date);
//
//        // Process in batches to avoid excessive logging and improve performance
//        int incompleteCount = 0;
//        if (!incompleteAssignments.isEmpty()) {
//            for (ShiftAssignment assignment : incompleteAssignments) {
//                if (assignment.getStatus() != ShiftAssignment.AssignmentStatus.INCOMPLETE) {
//                    assignment.setStatus(ShiftAssignment.AssignmentStatus.INCOMPLETE);
//                    shiftAssignmentRepository.save(assignment);
//                    incompleteCount++;
//                    log.info("Marked guard {} as INCOMPLETE for shift {} at site {}",
//                            assignment.getGuard().getUser().getUsername(),
//                            assignment.getShift().getName(),
//                            assignment.getShift().getSite().getName());
//                }
//            }
//            log.info("Marked {} assignments as INCOMPLETE", incompleteCount);
//        }
//    }
//
//    @Transactional
//    public void manuallyMarkAttendanceForLastMonth() {
//        log.info("Manually running attendance marking for the entire last month");
//
//        LocalDate today = LocalDate.now();
//        LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
//        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());
//
//        for (LocalDate date = firstDayOfLastMonth; !date.isAfter(lastDayOfLastMonth); date = date.plusDays(1)) {
//            manuallyMarkAttendance(date);
//        }
//
//        log.info("Finished marking attendance for all days in {}", firstDayOfLastMonth.getMonth());
//    }
//
//}

package com.dep.soms.service;

import com.dep.soms.model.ShiftAssignment;
import com.dep.soms.repository.ShiftAssignmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.scheduling.enable", havingValue = "true", matchIfMissing = true)
public class AttendanceSchedulerService {

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    /**
     * Run every day at 7:00 AM to mark absent guards.
     * Runs at 7am to properly handle night shifts (5pm-7am) that span midnight.
     */
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void markAbsentGuards() {
        log.info("Running scheduled task to mark absent guards");
        LocalDate evaluationDate = LocalDate.now().minusDays(1); // Evaluate yesterday's shifts
        processAttendanceForDate(evaluationDate);
    }

    @Transactional
    public void manuallyMarkAttendance(LocalDate date) {
        log.info("Manually running attendance marking for date: {}", date);
        processAttendanceForDate(date);
    }

    private void processAttendanceForDate(LocalDate date) {
        LocalDateTime evaluationCutoff = LocalDateTime.of(date.plusDays(1), LocalTime.of(7, 0)); // 7am next day

        // Get all shift assignments for the date
        List<ShiftAssignment> allAssignments = shiftAssignmentRepository.findByAssignmentDate(date);

        int absentCount = 0;
        int incompleteCount = 0;

        for (ShiftAssignment assignment : allAssignments) {
            // Skip already processed assignments
            if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.COMPLETED ||
                    assignment.getStatus() == ShiftAssignment.AssignmentStatus.ABSENT) {
                continue;
            }

            // Mark as ABSENT if never checked in by cutoff
            if (assignment.getCheckInTime() == null && assignment.getEndTime().isBefore(evaluationCutoff)) {
                assignment.setStatus(ShiftAssignment.AssignmentStatus.ABSENT);
                shiftAssignmentRepository.save(assignment);
                absentCount++;
                log.debug("Marked guard {} as ABSENT", assignment.getGuard().getUser().getUsername());
            }
            // Mark as INCOMPLETE if checked in but not out by cutoff
            else if (assignment.getCheckInTime() != null &&
                    assignment.getCheckOutTime() == null &&
                    assignment.getEndTime().isBefore(evaluationCutoff)) {
                assignment.setStatus(ShiftAssignment.AssignmentStatus.INCOMPLETE);
                shiftAssignmentRepository.save(assignment);
                incompleteCount++;
                log.debug("Marked guard {} as INCOMPLETE", assignment.getGuard().getUser().getUsername());
            }
        }

        log.info("Marked {} shifts as ABSENT", absentCount);
        log.info("Marked {} shifts as INCOMPLETE", incompleteCount);
    }

    @Transactional
    public void manuallyMarkAttendanceForLastMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());

        for (LocalDate date = firstDayOfLastMonth; !date.isAfter(lastDayOfLastMonth); date = date.plusDays(1)) {
            processAttendanceForDate(date);
        }
    }
}