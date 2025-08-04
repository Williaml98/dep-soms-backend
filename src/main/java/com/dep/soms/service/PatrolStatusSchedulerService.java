//package com.dep.soms.service;
//
//import com.dep.soms.model.PatrolAssignment;
//import com.dep.soms.repository.PatrolAssignmentRepository;
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
//public class PatrolStatusSchedulerService {
//
//    @Autowired
//    private PatrolAssignmentRepository patrolAssignmentRepository;
//
//    /**
//     * Run every day at 23:59 to update patrol assignment statuses
//     */
//    @Scheduled(cron = "0 59 23 * * *")
//    @Transactional
//    public void updatePatrolAssignmentStatuses() {
//        log.info("Running scheduled task to update patrol assignment statuses");
//        LocalDate today = LocalDate.now();
//        processPatrolAssignmentsForDate(today);
//    }
//
//    /**
//     * Manual trigger method for testing or administrative purposes
//     */
//    @Transactional
//    public void manuallyUpdatePatrolStatuses(LocalDate date) {
//        log.info("Manually updating patrol assignment statuses for date: {}", date);
//        processPatrolAssignmentsForDate(date);
//    }
//
//    /**
//     * Common method to process patrol assignments for a specific date
//     */
//    private void processPatrolAssignmentsForDate(LocalDate date) {
//        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
//
//        // 1. Mark assignments as MISSED if they were never started (PENDING/ACCEPTED but past end time)
//        List<PatrolAssignment> missedAssignments = patrolAssignmentRepository
//                .findByAssignmentDateAndStatusInAndScheduledEndTimeBefore(
//                        date,
//                        List.of(PatrolAssignment.PatrolAssignmentStatus.PENDING,
//                                PatrolAssignment.PatrolAssignmentStatus.ACCEPTED),
//                        endOfDay
//                );
//
//        log.info("Found {} missed patrol assignments for {}", missedAssignments.size(), date);
//
//        int missedCount = 0;
//        for (PatrolAssignment assignment : missedAssignments) {
//            assignment.setStatus(PatrolAssignment.PatrolAssignmentStatus.MISSED);
//            assignment.setNotes((assignment.getNotes() != null ? assignment.getNotes() + "\n" : "") +
//                    "Automatically marked as MISSED by system on " + LocalDateTime.now());
//            patrolAssignmentRepository.save(assignment);
//            missedCount++;
//            log.debug("Marked patrol assignment {} as MISSED", assignment.getId());
//        }
//        log.info("Marked {} patrol assignments as MISSED", missedCount);
//
//        // 2. Mark assignments as INCOMPLETE if they were started but not finished
//        List<PatrolAssignment> incompleteAssignments = patrolAssignmentRepository
//                .findByAssignmentDateAndStatusAndActualEndTimeIsNullAndScheduledEndTimeBefore(
//                        date,
//                        PatrolAssignment.PatrolAssignmentStatus.IN_PROGRESS,
//                        endOfDay
//                );
//
//        log.info("Found {} incomplete patrol assignments for {}", incompleteAssignments.size(), date);
//
//        int incompleteCount = 0;
//        for (PatrolAssignment assignment : incompleteAssignments) {
//            assignment.setStatus(PatrolAssignment.PatrolAssignmentStatus.INCOMPLETE);
//            assignment.setNotes((assignment.getNotes() != null ? assignment.getNotes() + "\n" : "") +
//                    "Automatically marked as INCOMPLETE by system on " + LocalDateTime.now());
//            patrolAssignmentRepository.save(assignment);
//            incompleteCount++;
//            log.debug("Marked patrol assignment {} as INCOMPLETE", assignment.getId());
//        }
//        log.info("Marked {} patrol assignments as INCOMPLETE", incompleteCount);
//    }
//
//    /**
//     * Process assignments for a range of dates (for backfilling or testing)
//     */
//    @Transactional
//    public void manuallyUpdatePatrolStatusesForDateRange(LocalDate startDate, LocalDate endDate) {
//        log.info("Manually updating patrol assignment statuses from {} to {}", startDate, endDate);
//
//        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
//            manuallyUpdatePatrolStatuses(date);
//        }
//
//        log.info("Finished updating patrol assignment statuses for date range");
//    }
//
//    /**
//     * Process assignments for the entire last month
//     */
//    @Transactional
//    public void manuallyUpdatePatrolStatusesForLastMonth() {
//        log.info("Manually updating patrol assignment statuses for last month");
//
//        LocalDate today = LocalDate.now();
//        LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
//        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());
//
//        manuallyUpdatePatrolStatusesForDateRange(firstDayOfLastMonth, lastDayOfLastMonth);
//
//        log.info("Finished updating patrol assignment statuses for {}", firstDayOfLastMonth.getMonth());
//    }
//}

package com.dep.soms.service;

import com.dep.soms.model.PatrolAssignment;
import com.dep.soms.repository.PatrolAssignmentRepository;
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
public class PatrolStatusSchedulerService {

    @Autowired
    private PatrolAssignmentRepository patrolAssignmentRepository;

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void updatePatrolAssignmentStatuses() {
        log.info("Running scheduled task to update patrol assignment statuses");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        processPatrolAssignmentsForDate(yesterday);
    }

    @Transactional
    public void manuallyUpdatePatrolStatuses(LocalDate date) {
        log.info("Manually updating patrol assignment statuses for date: {}", date);
        processPatrolAssignmentsForDate(date);
    }

    private void processPatrolAssignmentsForDate(LocalDate date) {
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);

        // 1. Mark assignments as MISSED if they were never started (PENDING/ACCEPTED but past end time)
        List<PatrolAssignment> missedAssignments = patrolAssignmentRepository
                .findByAssignmentDateAndStatusInAndEndTimeBefore(
                        date,
                        List.of(PatrolAssignment.PatrolAssignmentStatus.PENDING,
                                PatrolAssignment.PatrolAssignmentStatus.ACCEPTED),
                        endOfDay
                );

        log.info("Found {} missed patrol assignments for {}", missedAssignments.size(), date);

        missedAssignments.forEach(assignment -> {
            assignment.setStatus(PatrolAssignment.PatrolAssignmentStatus.MISSED);
            assignment.setNotes((assignment.getNotes() != null ? assignment.getNotes() + "\n" : "") +
                    "Automatically marked as MISSED by system on " + LocalDateTime.now());
            patrolAssignmentRepository.save(assignment);
            log.debug("Marked patrol assignment {} as MISSED", assignment.getId());
        });
        log.info("Marked {} patrol assignments as MISSED", missedAssignments.size());

        // 2. Mark assignments as INCOMPLETE if they were started but not finished
        List<PatrolAssignment> incompleteAssignments = patrolAssignmentRepository
                .findByAssignmentDateAndStatusAndEndTimeIsNullAndStartTimeBefore(
                        date,
                        PatrolAssignment.PatrolAssignmentStatus.IN_PROGRESS,
                        endOfDay
                );

        log.info("Found {} incomplete patrol assignments for {}", incompleteAssignments.size(), date);

        incompleteAssignments.forEach(assignment -> {
            assignment.setStatus(PatrolAssignment.PatrolAssignmentStatus.INCOMPLETE);
            assignment.setNotes((assignment.getNotes() != null ? assignment.getNotes() + "\n" : "") +
                    "Automatically marked as INCOMPLETE by system on " + LocalDateTime.now());
            patrolAssignmentRepository.save(assignment);
            log.debug("Marked patrol assignment {} as INCOMPLETE", assignment.getId());
        });
        log.info("Marked {} patrol assignments as INCOMPLETE", incompleteAssignments.size());
    }

    @Transactional
    public void manuallyUpdatePatrolStatusesForDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Manually updating patrol assignment statuses from {} to {}", startDate, endDate);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            manuallyUpdatePatrolStatuses(date);
        }

        log.info("Finished updating patrol assignment statuses for date range");
    }

    @Transactional
    public void manuallyUpdatePatrolStatusesForLastMonth() {
        log.info("Manually updating patrol assignment statuses for last month");

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());

        manuallyUpdatePatrolStatusesForDateRange(firstDayOfLastMonth, lastDayOfLastMonth);

        log.info("Finished updating patrol assignment statuses for {}", firstDayOfLastMonth.getMonth());
    }
}