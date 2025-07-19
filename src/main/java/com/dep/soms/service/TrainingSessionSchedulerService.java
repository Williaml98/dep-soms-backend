package com.dep.soms.service;

import com.dep.soms.model.TrainingSession;
import com.dep.soms.repository.TrainingSessionRepository;
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
public class TrainingSessionSchedulerService {

    private final TrainingSessionRepository trainingSessionRepository;

    @Autowired
    public TrainingSessionSchedulerService(TrainingSessionRepository trainingSessionRepository) {
        this.trainingSessionRepository = trainingSessionRepository;
    }

    /**
     * Run every hour to check for training sessions that need status updates
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at :00
    @Transactional
    public void updateTrainingSessionStatuses() {
        log.info("Running scheduled task to update training session statuses");
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Process sessions that should be in progress
        updateSessionsToInProgress(today, now);

        // Process sessions that should be completed
        updateSessionsToCompleted(today);
    }

    /**
     * Update sessions that have started but are still marked as SCHEDULED
     */
    private void updateSessionsToInProgress(LocalDate today, LocalTime now) {
        // Find sessions that:
        // 1. Start date is today or earlier
        // 2. End date is today or later
        // 3. Status is SCHEDULED
        // 4. Current time is after daily start time (if today is start date)
        List<TrainingSession> sessionsToStart = trainingSessionRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                        today, today, TrainingSession.TrainingStatus.SCHEDULED);

        log.info("Found {} training sessions that may need to be marked IN_PROGRESS", sessionsToStart.size());

        int updatedCount = 0;
        for (TrainingSession session : sessionsToStart) {
            // If today is the start date, check if current time is after daily start time
            if (session.getStartDate().isEqual(today)) {
                if (now.isBefore(session.getDailyStartTime())) {
                    continue; // Too early to start today
                }
            }

            session.setStatus(TrainingSession.TrainingStatus.IN_PROGRESS);
            trainingSessionRepository.save(session);
            updatedCount++;
            log.info("Updated training session {} to IN_PROGRESS status", session.getId());
        }

        log.info("Updated {} training sessions to IN_PROGRESS status", updatedCount);
    }

    /**
     * Update sessions that have ended but are still marked as IN_PROGRESS
     */
    private void updateSessionsToCompleted(LocalDate today) {
        // Find sessions that:
        // 1. End date is before today
        // 2. Status is IN_PROGRESS
        List<TrainingSession> sessionsToComplete = trainingSessionRepository
                .findByEndDateBeforeAndStatus(
                        today, TrainingSession.TrainingStatus.IN_PROGRESS);

        log.info("Found {} training sessions that need to be marked COMPLETED", sessionsToComplete.size());

        int updatedCount = 0;
        for (TrainingSession session : sessionsToComplete) {
            session.setStatus(TrainingSession.TrainingStatus.COMPLETED);
            trainingSessionRepository.save(session);
            updatedCount++;
            log.info("Updated training session {} to COMPLETED status", session.getId());
        }

        log.info("Updated {} training sessions to COMPLETED status", updatedCount);
    }

    /**
     * Manual trigger method for testing or administrative purposes
     */
    @Transactional
    public void manuallyUpdateSessionStatuses(LocalDate date) {
        log.info("Manually running training session status updates for date: {}", date);
        LocalTime time = LocalTime.now();
        updateSessionsToInProgress(date, time);
        updateSessionsToCompleted(date);
    }
}