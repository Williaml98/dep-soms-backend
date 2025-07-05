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
     * Run every day at 23:59 to mark absent guards for the day
     */
    @Scheduled(cron = "0 59 23 * * *")
    @Transactional
    public void markAbsentGuards() {
        log.info("Running scheduled task to mark absent guards");
        LocalDate today = LocalDate.now();
        processAttendanceForDate(today);
    }

    /**
     * Manual trigger method for testing or administrative purposes
     */
    @Transactional
    public void manuallyMarkAttendance(LocalDate date) {
        log.info("Manually running attendance marking for date: {}", date);
        processAttendanceForDate(date);
    }

    /**
     * Common method to process attendance for a specific date
     */
    private void processAttendanceForDate(LocalDate date) {
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);

        // Find all assignments for the date that haven't been checked in
        List<ShiftAssignment> missedAssignments = shiftAssignmentRepository.findByAssignmentDateAndCheckInTimeIsNullAndEndTimeBefore(
                date, endOfDay);

        log.info("Found {} missed assignments for {}", missedAssignments.size(), date);

        // Process in batches to avoid excessive logging and improve performance
        int absentCount = 0;
        if (!missedAssignments.isEmpty()) {
            for (ShiftAssignment assignment : missedAssignments) {
                if (assignment.getStatus() != ShiftAssignment.AssignmentStatus.ABSENT) {
                    assignment.setStatus(ShiftAssignment.AssignmentStatus.ABSENT);
                    shiftAssignmentRepository.save(assignment);
                    absentCount++;
                    log.info("Marked guard {} as ABSENT for shift {} at site {}",
                            assignment.getGuard().getUser().getUsername(),
                            assignment.getShift().getName(),
                            assignment.getShift().getSite().getName());
                }
            }
            log.info("Marked {} assignments as ABSENT", absentCount);
        }

        // Find all assignments that were checked in but not checked out
        List<ShiftAssignment> incompleteAssignments = shiftAssignmentRepository.findByAssignmentDateAndCheckInTimeIsNotNullAndCheckOutTimeIsNullAndEndTimeBefore(
                date, endOfDay);

        log.info("Found {} incomplete assignments for {}", incompleteAssignments.size(), date);

        // Process in batches to avoid excessive logging and improve performance
        int incompleteCount = 0;
        if (!incompleteAssignments.isEmpty()) {
            for (ShiftAssignment assignment : incompleteAssignments) {
                if (assignment.getStatus() != ShiftAssignment.AssignmentStatus.INCOMPLETE) {
                    assignment.setStatus(ShiftAssignment.AssignmentStatus.INCOMPLETE);
                    shiftAssignmentRepository.save(assignment);
                    incompleteCount++;
                    log.info("Marked guard {} as INCOMPLETE for shift {} at site {}",
                            assignment.getGuard().getUser().getUsername(),
                            assignment.getShift().getName(),
                            assignment.getShift().getSite().getName());
                }
            }
            log.info("Marked {} assignments as INCOMPLETE", incompleteCount);
        }
    }

    @Transactional
    public void manuallyMarkAttendanceForLastMonth() {
        log.info("Manually running attendance marking for the entire last month");

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());

        for (LocalDate date = firstDayOfLastMonth; !date.isAfter(lastDayOfLastMonth); date = date.plusDays(1)) {
            manuallyMarkAttendance(date);
        }

        log.info("Finished marking attendance for all days in {}", firstDayOfLastMonth.getMonth());
    }

}
