package com.dep.soms.repository;

import com.dep.soms.model.TrainingSession;
import com.dep.soms.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    List<TrainingSession> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<TrainingSession> findByStatus(TrainingSession.TrainingStatus status);
    List<TrainingSession> findByTrainerId(Long trainerId);

    List<TrainingSession> findByStartTimeAfter(LocalDateTime startTime);
    List<TrainingSession> findByTrainer(User trainer);
    @EntityGraph(attributePaths = {"attendances", "certifications", "trainer"})
    List<TrainingSession> findAll();

    @EntityGraph(attributePaths = {"attendances", "certifications", "trainer"})
    Optional<TrainingSession> findById(Long id);
    List<TrainingSession> findByStartDateAfter(LocalDate date);
    List<TrainingSession> findByStartDateBeforeAndEndDateAfter(LocalDate startBefore, LocalDate endAfter);
}
