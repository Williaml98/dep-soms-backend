package com.dep.soms.repository;

import com.dep.soms.model.PersonRegistration;
import com.dep.soms.model.TrainingAttendance;
import com.dep.soms.model.TrainingRecord;
import com.dep.soms.model.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingAttendanceRepository extends JpaRepository<TrainingAttendance, Long> {
    List<TrainingAttendance> findByTrainingSession(TrainingSession session);
    List<TrainingAttendance> findByTrainingRecord(TrainingRecord record);
    List<TrainingAttendance> findByPersonRegistration(PersonRegistration personRegistration);
    boolean existsByTrainingSessionAndPersonRegistration(TrainingSession session, PersonRegistration personRegistration);
    long countByTrainingSession(TrainingSession session);

    List<TrainingAttendance> findByTrainingSessionAndSessionDate(TrainingSession session, LocalDate date);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM TrainingAttendance a " +
            "WHERE a.trainingSession = ?1 AND a.personRegistration = ?2")

    Optional<TrainingAttendance> findByTrainingSessionAndPersonRegistrationAndSessionDate(
            TrainingSession session, PersonRegistration personRegistration, LocalDate sessionDate);

    @Query("SELECT ta FROM TrainingAttendance ta WHERE ta.trainingSession.id = :sessionId AND ta.sessionDate = :date")
    List<TrainingAttendance> findByTrainingSessionIdAndSessionDate(@Param("sessionId") Long sessionId, @Param("date") LocalDate date);



}
